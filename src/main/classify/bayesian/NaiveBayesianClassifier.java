package main.classify.bayesian;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.ansj.util.FilterModifWord;
import org.apache.commons.io.FileUtils;

import love.cq.util.StringUtil;
import main.classify.ClassifierMain;
import main.classify.common.Constants;
import main.classify.common.TrainModel;
import main.classify.common.CreateTrainAndTestSample;
import main.treesplit.util.IOUtil;

/**
 * 利用朴素贝叶斯算法对newsgroup文档集做分类，采用十组交叉测试取平均值
 * 采用多项式模型,stanford信息检索导论课件上面言多项式模型比伯努利模型准确度高 类条件概率P(tk|c)=(类c 下单词tk
 * 在各个文档中出现过的次数之和+1)/(类c下单词总数+|V|)
 * 
 * @author yangliu
 * @qq 772330184
 * @mail yang.liu@pku.edu.cn
 * 
 */
public class NaiveBayesianClassifier {
	private static final DecimalFormat DF = new DecimalFormat("##.###");
	private static int NUM_OF_CLS = 2;//分类个数
	private static Map<String, String> clsName = new HashMap<String, String>();
	private static TrainModel model = readModel("");
	/**
	 * 用贝叶斯法对测试文档集分类
	 * 
	 * @param trainDir
	 *            训练文档集目录
	 * @param testDir
	 *            测试文档集目录
	 * @param classifyResultFileNew
	 *            分类结果文件路径
	 * @throws Exception
	 */
	private void doProcess(String trainDir, String testDir, String classifyResultFileNew) throws Exception {
		//下面开始读取测试样例做分类
		Vector<String> testFileWords = new Vector<String>();
		String word;
		File[] testDirFiles = new File(testDir).listFiles();
		FileWriter crWriter = new FileWriter(classifyResultFileNew);
		for (int i = 0; i < testDirFiles.length; i++) {
			File[] testSample = testDirFiles[i].listFiles();
			for (int j = 0; j < testSample.length; j++) {
				testFileWords.clear();
				//分词提取特征词
				FileReader spReader = new FileReader(testSample[j]);
				BufferedReader spBR = new BufferedReader(spReader);
				while ((word = spBR.readLine()) != null) {
					testFileWords.add(word);
				}
				//下面分别计算该测试样例相对于各类别的概率
				File[] trainDirFiles = new File(trainDir).listFiles();
				BigDecimal maxP = new BigDecimal(0);
				String bestCate = null;
				for (int k = 0; k < trainDirFiles.length; k++) {
					BigDecimal p = computeCateProb(trainDirFiles[k], testFileWords, model);
					if (k == 0) {
						maxP = p;
						bestCate = trainDirFiles[k].getName();
						continue;
					}
					if (p.compareTo(maxP) == 1) {
						maxP = p;
						bestCate = trainDirFiles[k].getName();
					}
				}
				crWriter.append(testDirFiles[i].getName() + "_" + testSample[j].getName() + " " + bestCate + "\n");
				crWriter.flush();
			}
		}
		crWriter.close();
	}

	/**
	 * 统计某类训练样本中每个单词的出现次数
	 * 
	 * @param strDir
	 *            训练样本集目录
	 * @return Map<String,Double> cateWordsProb
	 *         用"类目_单词"对来索引的map,保存的val就是该类目下该单词的出现次数
	 * @throws IOException
	 */
	public Map<String, Double> getCateWordsProb(String strDir){
		Map<String, Double> cateWordsProb = new TreeMap<String, Double>();
		File sampleFile = new File(strDir);
		File[] sampleDir = sampleFile.listFiles();
		String word;BufferedReader reader = null;
		try {
			for (int i = 0; i < sampleDir.length; i++) {
				File[] sample = sampleDir[i].listFiles();
				for (int j = 0; j < sample.length; j++) {
					reader = new BufferedReader(new FileReader(sample[j]));
//				BufferedReader reader = IOUtil.getReader(sample[j].getAbsolutePath(), IOUtil.GBK);
					while ((word = reader.readLine()) != null) {
						String key = sampleDir[i].getName() + "_" + word;
						if (cateWordsProb.containsKey(key)) {
							double count = cateWordsProb.get(key) + 1.0;
							cateWordsProb.put(key, count);
						} else {
							cateWordsProb.put(key, 1.0);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cateWordsProb;
	}

	/**
	 * 计算某一个测试样本属于某个类别的概率
	 * 
	 * @param Map
	 *            <String, Double> cateWordsProb 记录每个目录中出现的单词及次数
	 * @param File
	 *            trainFile 该类别所有的训练样本所在目录
	 * @param Vector
	 *            <String> testFileWords 该测试样本中的所有词构成的容器
	 * @param double totalWordsNum 记录所有训练样本的单词总数
	 * @param Map
	 *            <String, Double> cateWordsNum 记录每个类别的单词总数
	 * @return BigDecimal 返回该测试样本在该类别中的概率
	 * @throws Exception
	 * @throws IOException
	 */
	private BigDecimal computeCateProb(File trainFile, Vector<String> testFileWords, TrainModel mode) throws Exception {
		BigDecimal probability = new BigDecimal(1);
		double wordNumInCate = mode.getCateWordsNum().get(trainFile.getName());
		BigDecimal wordNumInCateBD = new BigDecimal(wordNumInCate);
		BigDecimal totalWordsNumBD = new BigDecimal(mode.getTotalWordsNum());
		Map<String, Double> cateWordsProb = mode.getCateWordsProb();
		for (Iterator<String> it = testFileWords.iterator(); it.hasNext();) {
			String me = it.next();
			String key = trainFile.getName() + "_" + me;
			double testFileWordNumInCate = 0.0;
			if (cateWordsProb.containsKey(key)) {
				testFileWordNumInCate = cateWordsProb.get(key);
			}
			BigDecimal testFileWordNumInCateBD = new BigDecimal(testFileWordNumInCate);
			//多元分布模型( multinomial model )  –以单词为粒度
			//原始计算：类条件概率P(tk|c)=(类c下单词tk在各个文档中出现过的次数之和+1)/（类c下单词总数+训练样本中不重复特征词总数）
			//优化改进：类条件概率P(tk|c)=(类c下单词tk在各个文档中出现过的次数之和+0.001)/（类c下主题词总数+训练样本中不重复特征词总数）
			BigDecimal xcProb = (testFileWordNumInCateBD.add(new BigDecimal(0.001))).divide(totalWordsNumBD.add(wordNumInCateBD), 10, BigDecimal.ROUND_CEILING);
			probability = probability.multiply(xcProb);
		}
		//先验概率P(c)=类c下的单词总数/整个训练样本的单词总数
		BigDecimal pc = wordNumInCateBD.divide(totalWordsNumBD, 10, BigDecimal.ROUND_CEILING);
		//P(tk|c)*P(c)
		BigDecimal res = probability.multiply(pc);
		return res;
	}


	/**
	 * 根据正确类目文件和分类结果文件统计出准确率
	 * 
	 * @param classifyResultFile
	 *            正确类目文件
	 * @param classifyResultFileNew
	 *            分类结果文件
	 * @return double 分类的准确率
	 * @throws IOException
	 */
	double computeAccuracy(String classifyResultFile, String classifyResultFileNew) throws IOException {
		// TODO Auto-generated method stub
		Map<String, String> rightCate = getMapFromResultFile(classifyResultFile);
		Map<String, String> resultCate = getMapFromResultFile(classifyResultFileNew);
		double rightCount = 0.0;
		for (Map.Entry<String, String> entry : resultCate.entrySet()) {
			if (entry.getValue().equals(rightCate.get(entry.getKey()))) {
				rightCount++;
			}
		}
		computerConfusionMatrix(rightCate, resultCate);
		return rightCount / resultCate.size();
	}

	/**
	 * 根据正确类目文件和分类结果文计算混淆矩阵并且输出
	 * 
	 * @param rightCate
	 *            正确类目对应map
	 * @param resultCate
	 *            分类结果对应map
	 * @return double 分类的准确率
	 * @throws IOException
	 */
	private void computerConfusionMatrix(Map<String, String> rightCate, Map<String, String> resultCate) {
		SortedSet<String> cateNames = new TreeSet<String>();
		Set<Map.Entry<String, String>> rightCateSet = rightCate.entrySet();
		for (Map.Entry<String, String> entry : rightCateSet) {
			cateNames.add(entry.getValue().split("_")[0]);
		}
		String[] cateNamesArray = cateNames.toArray(new String[0]);
		NUM_OF_CLS = cateNamesArray.length;
		int[][] confusionMatrix = new int[NUM_OF_CLS][NUM_OF_CLS];//首先求出类目对应的数组索引
		Map<String, Integer> cateNamesToIndex = new TreeMap<String, Integer>();
		for (int i = 0; i < NUM_OF_CLS; i++) {
			cateNamesToIndex.put(cateNamesArray[i], i);
		}
		for (Map.Entry<String, String> entry : rightCateSet) {
			confusionMatrix[cateNamesToIndex.get(entry.getValue())][cateNamesToIndex.get(resultCate.get(entry.getKey()))]++;
		}
		//输出混淆矩阵
		double[] hangSum = new double[NUM_OF_CLS];
		System.out.print(format(""));
		for (int i = 0; i < NUM_OF_CLS; i++) {
			System.out.print(format(clsName.get(cateNamesArray[i])));
		}
		System.out.print(format("hit(%)"));
		System.out.println(format("+"));
		for (int i = 0; i < NUM_OF_CLS; i++) {
			System.out.print(format(clsName.get(cateNamesArray[i])));
			for (int j = 0; j < NUM_OF_CLS; j++) {
				System.out.print(format(confusionMatrix[i][j]));
				hangSum[i] += confusionMatrix[i][j];
			}
			System.out.print(DF.format(confusionMatrix[i][i] / hangSum[i]));
			System.out.println(format("+"));
		}
	}

	/**
	 * 从分类结果文件中读取map
	 * 
	 * @param classifyResultFileNew
	 *            类目文件
	 * @return Map<String, String> 由<文件名，类目名>保存的map
	 * @throws IOException
	 */
	private Map<String, String> getMapFromResultFile(String classifyResultFileNew) throws IOException {
		// TODO Auto-generated method stub
		File crFile = new File(classifyResultFileNew);
		FileReader crReader = new FileReader(crFile);
		BufferedReader crBR = new BufferedReader(crReader);
		Map<String, String> res = new TreeMap<String, String>();
		String[] s;
		String line;
		while ((line = crBR.readLine()) != null) {
			s = line.split(" ");
			res.put(s[0], s[1]);
		}
		return res;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public void NaiveBayesianClassifierMain(String[] args) throws Exception {
		int verifyLoop = Constants.VERIFY_LOOP;
		//首先创建训练集和测试集
		BufferedReader reader = IOUtil.getReader("D:/testminezh/class.txt", IOUtil.GBK);
		String cls = null;
		while ((cls = reader.readLine()) != null) {
			String[] clz = cls.split("\t");
			clsName.put(clz[0], clz[1]);
		}
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		double[] accuracyOfEveryExp = new double[10];
		double accuracyAvg, sum = 0;
		for (int i = 0; i < verifyLoop; i++) {//用交叉验证法做十次分类实验，对准确率取平均值	
			String TrainDir = Constants.ROOT_DIR + "TrainSample" + i;
			String TestDir = Constants.ROOT_DIR + "TestSample" + i;
			String classifyRightCate = Constants.ROOT_DIR + "classifyRightCate" + i + ".txt";
			String classifyResultFileNew = Constants.ROOT_DIR + "classifyResultNew" + i + ".txt";
			nbClassifier.doProcess(TrainDir, TestDir, classifyResultFileNew);
			accuracyOfEveryExp[i] = nbClassifier.computeAccuracy(classifyRightCate, classifyResultFileNew);
			System.out.println("The accuracy for Naive Bayesian Classifier in " + (i + 1) + "th Exp is :" + DF.format(accuracyOfEveryExp[i]) + "\n");
		}
		for (int i = 0; i < verifyLoop; i++) {
			sum += accuracyOfEveryExp[i];
		}
		accuracyAvg = sum / verifyLoop;
		System.out.println("The average accuracy for Naive Bayesian Classifier in all Exps is :" + DF.format(accuracyAvg));

	}

	/**
	 * 解析模型
	 * @param modelPath
	 * @return
	 */
	static TrainModel readModel(String modelPath) {
		if (StringUtil.isBlank(modelPath)) {
			modelPath = "d:/clsmode";
		}
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(modelPath));
			Object obj = in.readObject();
			TrainModel model = (TrainModel) obj;
			return model;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	String format(Object o) {
		int length = 10;
		if ("+".equals(o.toString())) {
			StringBuffer buf = new StringBuffer("\n-----------");
			for (int i = 0; i < NUM_OF_CLS; i++) {
				buf.append("-----------");
			}
			return buf.toString();
		}
		StringBuffer buf = new StringBuffer(o.toString());
		for (int i = 0; i < length - o.toString().length(); i++) {
			buf.append(" ");
		}
		return buf.toString();
	}

	public static void main(String[] args) throws Exception {
		readModel("");
	}
}
