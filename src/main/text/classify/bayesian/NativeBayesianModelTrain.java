package main.text.classify.bayesian;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import main.text.classify.common.Constants;
import main.text.classify.common.TrainModel;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NativeBayesianModelTrain {

	private static final Logger log = LoggerFactory.getLogger(NativeBayesianModelTrain.class);

	/**
	 * 统计某类训练样本中每个单词的出现次数
	 * 
	 * @param strDir 训练样本集目录
	 * @return Map<String, Map<String, Double>> cateWordsProb
	 * 用"<类目:<单词,个数>>"来索引的map
	 */
	public Map<String, Map<String, Double>> getCateWordsProb(String strDir) {
		Map<String, Map<String, Double>> cateWordsProb = new HashMap<String, Map<String, Double>>();
		File sampleFile = new File(strDir);
		File[] sampleDir = sampleFile.listFiles();
		String word;
		BufferedReader reader = null;
		try {
			for (int i = 0; i < sampleDir.length; i++) {
				String key = sampleDir[i].getName();
				cateWordsProb.put(key, new HashMap<String, Double>());
				File[] sample = sampleDir[i].listFiles();
				for (int j = 0; j < sample.length; j++) {
					Map<String, Double> wordsmMap = cateWordsProb.get(key);
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(sample[j]), "UTF-8"));
					while ((word = reader.readLine()) != null) {
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
		} finally {
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
	 * 
	 * @throws Exception
	 */
	public void saveModel() throws Exception {
		String mpath = Constants.ROOT_DIR + "classify.m";
		log.info("save train model to {}", mpath);
		Set<String> set = new HashSet<>();
		Map<String, Map<String, Double>> cateWordsProb = getCateWordsProb(Constants.DATA_SPECIAL_DIR);
		Map<String, Double> cateWordsNum = new HashMap<String, Double>();
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
		File file = new File(mpath);
		if (file.exists()) {
			file.delete();//目前用于循环训练，下一步增加合并参数进行判断
			//			model.setWords(set);
			//			model = mergeModel4DiffCategory(model);
		}
		//普通写入
//		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(mpath));
		//gzip压缩
		ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(
				new FileOutputStream(mpath))));
		out.writeObject(model);
		set.clear();
		out.flush();
		out.close();
		//		FileUtils.cleanDirectory(new File(path));
		//		FileUtils.cleanDirectory(new File(Constants.DATA_PRE_PROCESS_DIR));
	}

	/**
	 * 不同类别的训练集合并
	 * 
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public TrainModel mergeModel4DiffCategory(TrainModel m) throws Exception {
		String path = Constants.ROOT_DIR + "classify.m";
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(path));
			Object obj = in.readObject();
			TrainModel model = (TrainModel) obj;
			model.getCateWordsNum().putAll(m.getCateWordsNum());
			model.getCateWordsProb().putAll(m.getCateWordsProb());
			model.getWords().addAll(m.getWords());
			model.setTotalWordsNum(model.getWords().size());
			log.info("after merge : num of keywords for each class : {}", model.getCateWordsNum().toString());
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
	 * 
	 * @param m
	 * @return
	 * @throws Exception
	 */
	public TrainModel mergeModel4All(TrainModel m) throws Exception {
		return null;
	}

	public void readModel() {
		String path = Constants.ROOT_DIR + "classify.m";
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(path));//普通读出
//			in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(path))));//gzip解压
			Object obj = in.readObject();
			TrainModel model = (TrainModel) obj;
			log.info("after merge : num of keywords for each class : {}", model.getCateWordsNum().toString());
			log.info("after merge : no-repeat keywords total of trainsets : {}", model.getTotalWordsNum());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		DataPretreatment prev = new DataPretreatment();
		//		prev.process(Constants.DATA_PRE_PROCESS_DIR);

		NativeBayesianModelTrain train = new NativeBayesianModelTrain();
//		train.readModel();
				train.saveModel();
	}

	@Test
	public void testReadFile() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(
				new File("D:\\datamine\\featurewords\\C000001\\1.txt")));
		String word = "";
		while ((word = reader.readLine()) != null) {
			System.out.println(word);
		}
		reader.close();
	}
}
