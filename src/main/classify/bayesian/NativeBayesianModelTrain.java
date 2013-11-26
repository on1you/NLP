package main.classify.bayesian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import love.cq.util.StringUtil;
import main.classify.common.Constants;
import main.classify.common.TrainModel;
import main.treesplit.util.IOUtil;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.FilterModifWord;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeBayesianModelTrain {
	private static final Logger log = LoggerFactory.getLogger(NativeBayesianModelTrain.class);
	
	public void dataPreProcess(String strDir) throws Exception{
		File fileDir = new File(strDir);
		if(!fileDir.exists()){
			log.error("File not exist:" + strDir);
			return;
		}
		String subStrDir = strDir.substring(strDir.lastIndexOf("/"));//trainset
		String dirTarget = Constants.ROOT_DIR+"processedSampleOnlySpecial"+subStrDir;
		if(!new File(Constants.ROOT_DIR+"processedSampleOnlySpecial").exists()){
			new File(Constants.ROOT_DIR+"processedSampleOnlySpecial").mkdir();
		}
		File fileTarget = new File(dirTarget);
		if(!fileTarget.exists()){
			fileTarget.mkdir();
		}
		File[] srcFiles = fileDir.listFiles();
		for(int i = 0; i < srcFiles.length; i++){
			String fileFullName = srcFiles[i].getCanonicalPath();
			String fileShortName = srcFiles[i].getName();
			log.info("parse text in {}",fileFullName);
			if(!new File(fileFullName).isDirectory()){//确认子文件名不是目录如果是可以再次递归调用
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(dirTarget + "/" + fileShortName);
				extractWords(fileFullName, stringBuilder.toString());
			}else {
				fileFullName = fileFullName.replace("\\","/");
				dataPreProcess(fileFullName);
			}
		}
	}
	
	/**提取特征词
	 * @param srcDir 源文件文件目录的绝对路径
	 * @param targetDir 生成的目标文件的绝对路径
	 * @throws IOException 
	 */
	private static void extractWords(String srcDir, String destDir) throws IOException{
		BufferedReader srcFileBR = IOUtil.getReader(srcDir,IOUtil.GBK);
		List<String> words = new ArrayList<String>();
		String line;
		while((line = srcFileBR.readLine()) != null){
			if (StringUtil.isBlank(line.trim())) {
				continue;
			}
			List<String> lineWords = wordSeg(line);
			words.addAll(lineWords);
		}
		FileUtils.writeLines(new File(destDir), IOUtil.GBK, words);
		srcFileBR.close();
	}
	
	/**
	 * 分词
	 * @param content
	 * @return
	 */
	public static List<String> wordSeg(String content){
		List<String> words = new ArrayList<String>();
		List<Term> terms = ToAnalysis.parse(content);
		terms = FilterModifWord.modifResult(terms);//停止词过滤
		for (Term term : terms) {
			String txt = term.getName();
			String natrue = term.getNatrue().natureStr;
			if (StringUtil.isBlank(txt.trim()) || txt.trim().length() == 1) {
				continue;
			}
			if (natrue.matches("^(n|a|v(n|i|l|g)|b|i|j|l).*$")) {
				words.add(txt);
			}
		}
		return words;
	}
	/**
	 * 获得每个类目下的单词总数
	 * 
	 * @param trainDir 训练文档集目录
	 * @return Map<String, Double> <目录名，单词总数>的map
	 */
	private Map<String, Double> getCateWordsNum(String trainDir){
		Map<String, Double> cateWordsNum = new HashMap<String, Double>();
		BufferedReader reader = null;
		File[] sampleDir = new File(trainDir).listFiles();
		try {
			for (int i = 0; i < sampleDir.length; i++) {
				double count = 0;
				File[] sample = sampleDir[i].listFiles();
				for (int j = 0; j < sample.length; j++) {
					reader = new BufferedReader(new FileReader(sample[j]));
					while (reader.readLine() != null) {
						count++;
					}
				}
				cateWordsNum.put(sampleDir[i].getName(), count);
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
		return cateWordsNum;
	}
	
	/**
	 * 统计某类训练样本中每个单词的出现次数
	 * 
	 * @param strDir 训练样本集目录
	 * @return Map<String,Double> cateWordsProb
	 *         用"类目_单词"对来索引的map,保存的val就是该类目下该单词的出现次数
	 */
	public Map<String, Map<String, Double>> getCateWordsProb(String strDir){
//		Map<String, Double> cateWordsProb = new HashMap<String, Double>();
		Map<String, Map<String, Double>> cateWordsProb = new HashMap<String, Map<String,Double>>();
		File sampleFile = new File(strDir);
		File[] sampleDir = sampleFile.listFiles();
		String word;BufferedReader reader = null;
		try {
			for (int i = 0; i < sampleDir.length; i++) {
				String key = sampleDir[i].getName();
				cateWordsProb.put(key, new HashMap<String,Double>());
				File[] sample = sampleDir[i].listFiles();
				for (int j = 0; j < sample.length; j++) {
					Map<String,Double> wordsmMap = cateWordsProb.get(key);
					reader = new BufferedReader(new FileReader(sample[j]));
					while ((word = reader.readLine()) != null) {
//						String key = sampleDir[i].getName() + "_" + word;
						if (wordsmMap.containsKey(word)) {
							double count = wordsmMap.get(word) + 1.0;
							wordsmMap.put(word, count);
						} else {
							wordsmMap.put(word, 1.0);
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
	 * 保存分类模型
	 * @throws Exception
	 */
	public void saveModel() throws Exception {
		String mpath = "d:/classify.mod";
		log.info("save train model to {}",mpath);
		Set<String> set = new HashSet<>();
		String path = Constants.ROOT_DIR + "processedSampleOnlySpecial";
		
		Map<String, Map<String, Double>> cateWordsProb = getCateWordsProb(path);//保存训练样本每个类别中每个属性词的出现词数
		Map<String, Double> cateWordsNum = new HashMap<String, Double>();//保存训练集每个类别的总词数
//		Map<String, Double> cateWordsNum = getCateWordsNum(path);//保存训练集每个类别的总词数
		for (Map.Entry<String, Map<String, Double>> entry : cateWordsProb.entrySet()) {
			set.addAll(entry.getValue().keySet());
			double wordCount = 0.0;
			for (Double d : entry.getValue().values()) {
				wordCount += d;
			}
			cateWordsNum.put(entry.getKey(), wordCount);
		}
		log.info("before merge : num of keywords for each class : {}", cateWordsNum.toString());
		log.info("before merge : no-repeat keywords total of trainsets : {}", set.size());
		TrainModel model = new TrainModel(set.size(), cateWordsNum, cateWordsProb);
		model.setWords(set);
		TrainModel m = mergeModel4DiffCategory(model);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(mpath));
		out.writeObject(m);
		set.clear();
		out.flush();
		out.close();
		FileUtils.cleanDirectory(new File(path));
		FileUtils.cleanDirectory(new File(Constants.DATA_PRE_PROCESS_DIR));
	}
	/**
	 * 不同类别的训练集合并
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public TrainModel mergeModel4DiffCategory(TrainModel m) throws Exception {
		String path = "d:/classify.mod";
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(path));
			Object obj = in.readObject();
			TrainModel model = (TrainModel) obj;
			model.getCateWordsNum().putAll(m.getCateWordsNum());
			model.getCateWordsProb().putAll(m.getCateWordsProb());
			model.getWords().addAll(m.getWords());
			model.setTotalWordsNum(model.getWords().size());
			log.info("after merge : num of keywords for each class : {}",  model.getCateWordsNum().toString());
			log.info("after merge : no-repeat keywords total of trainsets : {}", model.getWords().size());
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
	
	/**
	 * 自主学习的相同训练集补充
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public TrainModel mergeModel4All(TrainModel m) throws Exception {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		NativeBayesianModelTrain train = new NativeBayesianModelTrain();
		train.dataPreProcess(Constants.DATA_PRE_PROCESS_DIR);
		train.saveModel();
	}
}
