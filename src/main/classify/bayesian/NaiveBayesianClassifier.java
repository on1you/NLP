package main.classify.bayesian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Vector;

import love.cq.util.StringUtil;
import main.classify.common.Constants;
import main.classify.common.ResultEvaluating;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benguo.datam.bean.TrainModel;

/**
 * 利用朴素贝叶斯算法新闻文档集做分类，采用十组交叉测试取平均值
 * 采用多项式模型,stanford信息检索导论上面言多项式模型比伯努利模型准确度高
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * 
 */
public class NaiveBayesianClassifier {
	private static final Logger log = LoggerFactory.getLogger(NaiveBayesianClassifier.class);
	private static final DecimalFormat DF = new DecimalFormat("##.###");
	private static TrainModel model = readModel("");
	
	/**
	 * 用贝叶斯法对测试文档集分类
	 * 
	 * @param trainDir 训练文档集目录
	 * @param testDir 测试文档集目录
	 * @param classifyResultFileNew 分类结果文件路径
	 * @throws Exception
	 */
	private void doProcess(String testDir, String classifyResultFileNew) throws Exception {
		//下面开始读取测试样例做分类
		Vector<String> testFileWords = new Vector<String>();
		String word;BufferedReader reader = null;
		File[] testDirFiles = new File(testDir).listFiles();
		FileWriter crWriter = new FileWriter(classifyResultFileNew);
		log.info("Compute probability for '{}' and Autocorrection ",testDir);
		for (int i = 0; i < testDirFiles.length; i++) {
			File[] testSample = testDirFiles[i].listFiles();
			for (int j = 0; j < testSample.length; j++) {
				testFileWords.clear();
				//分词提取特征词
				reader = new BufferedReader(new FileReader(testSample[j]));
//				reader = IOUtil.getReader(testSample[j].getAbsolutePath(), IOUtil.UTF8);
				while ((word = reader.readLine()) != null) {
					testFileWords.add(word);
				}
				//下面分别计算该测试样例相对于各类别的概率
				BigDecimal maxP = new BigDecimal(0);
				String bestCate = null;int loop = -1;
				for (String key : Constants.clsName.keySet()) {
					loop ++;
					BigDecimal p = computeCateProb(key, testFileWords, model);
					if (loop == 0) {
						maxP = p;
						bestCate = key;
						continue;
					}
					if (p.compareTo(maxP) == 1) {
						maxP = p;
						bestCate = key;
					}
				}
				//训练集校正
				if (!testDirFiles[i].getName().equals(bestCate)) {
					File addFile = new File(Constants.DATA_SPECIAL_DIR + bestCate + "/" + testSample[j].getName());
					File delFile = new File(Constants.DATA_SPECIAL_DIR + testDirFiles[i].getName() + "/" + testSample[j].getName());
					if (addFile.exists()) {
						addFile = new File(Constants.DATA_SPECIAL_DIR + bestCate + "/m" + testSample[j].getName());
					}
					if (delFile.exists()) {
						FileUtils.copyFile(delFile, addFile);
						delFile.delete();
						log.info("moving file '{}' to '{}'",delFile.getAbsolutePath(),addFile.getAbsolutePath());
					}
				}
				crWriter.append(testDirFiles[i].getName() + "_" + testSample[j].getName() + " " + bestCate + "\n");
			}
		}
		crWriter.flush();
		reader.close();
		crWriter.close();
	}


	/**
	 * 计算某一个测试样本属于某个类别的概率
	 * 
	 * @param Map <String, Double> cateWordsProb 记录每个目录中出现的单词及次数
	 * @param File trainFile 该类别所有的训练样本所在目录
	 * @param Vector <String> testFileWords 该测试样本中的所有词构成的容器
	 * @param double totalWordsNum 记录所有训练样本的单词总数
	 * @param Map <String, Double> cateWordsNum 记录每个类别的单词总数
	 * @return BigDecimal 返回该测试样本在该类别中的概率
	 * @throws Exception
	 * @throws IOException
	 */
	private BigDecimal computeCateProb(String clsName, Vector<String> testFileWords, TrainModel mode) throws Exception {
		BigDecimal probability = new BigDecimal(1);
		double wordNumInCate = mode.getCateWordsNum().get(clsName);
		BigDecimal wordNumInCateBD = new BigDecimal(wordNumInCate);
		BigDecimal totalWordsNumBD = new BigDecimal(mode.getTotalWordsNum());
		Map<String, Map<String, Double>> cateWordsProb = mode.getCateWordsProb();
		for (String me : testFileWords) {
			double testFileWordNumInCate = 0.0;
			if (cateWordsProb.containsKey(clsName) && cateWordsProb.get(clsName).containsKey(me)) {
				testFileWordNumInCate = cateWordsProb.get(clsName).get(me);
			}
			BigDecimal testFileWordNumInCateBD = new BigDecimal(testFileWordNumInCate);
			//多元分布模型( multinomial model )  –以单词为粒度
			//原始计算：类条件概率P(tk|c)=(类c下单词tk在各个文档中出现过的次数之和+1)/（类c下单词总数+训练样本中单词总数）
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
	 * @param args
	 * @throws Exception
	 */
	public void NaiveBayesianClassifierMain(String[] args) throws Exception {
		int verifyLoop = Constants.VERIFY_LOOP;
		//首先创建训练集和测试集
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		ResultEvaluating eva = new ResultEvaluating();
		double[] accuracyOfEveryExp = new double[verifyLoop];
		double accuracyAvg, sum = 0;
		for (int i = 0; i < verifyLoop; i++) {//用交叉验证法做十次分类实验，对准确率取平均值	
			String testDir = Constants.ROOT_DIR + "TestSample" + i;
			String newClassify = Constants.ROOT_DIR + "classifyResultNew" + i + ".txt";
			File classifyRightCate = new File(Constants.ROOT_DIR + "classifyRightCate" + i + ".txt");
			File classifyResultFileNew = new File(newClassify);
			if (classifyResultFileNew.exists()) {
				classifyResultFileNew.delete();
			}
			nbClassifier.doProcess(testDir, newClassify);
			accuracyOfEveryExp[i] = eva.computeAccuracy(classifyRightCate, classifyResultFileNew);
			System.out.println("The accuracy for Naive Bayesian Classifier in " + (i + 1) + "th Exp is :" + DF.format(accuracyOfEveryExp[i]) + "\n");
		}
		for (int i = 0; i < verifyLoop; i++) {
			sum += accuracyOfEveryExp[i];
		}
		accuracyAvg = sum / verifyLoop;
		System.out.println("The avg(accuracy) for Naive Bayesian Classifier in all Exps is :" + DF.format(accuracyAvg));
		if (accuracyAvg == 1) {
			System.exit(0);
		}

	}

	/**
	 * 解析模型
	 * @param modelPath
	 * @return
	 */
	static TrainModel readModel(String modelPath) {
		if (StringUtil.isBlank(modelPath)) {
			modelPath = Constants.ROOT_DIR + "classify.mod";
		}
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(modelPath));
			Object obj = in.readObject();
			TrainModel model = (TrainModel) obj;
			log.info("num of keywords for each class : {}",model.getCateWordsNum());
			log.info("no-repeat keywords total of trainsets : {}", model.getTotalWordsNum());
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

	

	public static void main(String[] args) throws Exception {
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		nbClassifier.NaiveBayesianClassifierMain(args);
	}
}
