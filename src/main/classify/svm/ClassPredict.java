package main.classify.svm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类预测(可利用多模型联合预判)
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn Jan 21, 2014 3:33:42 PM
 */
public class ClassPredict {
	private List<TmsModel> models;

	/**
	 * 构造函数，需要给出模型所在的路径以及配置文件的名称
	 * 
	 * @param paths
	 * @param configNames
	 */
	public ClassPredict(String[] paths, String[] configNames) {
		models = new ArrayList<TmsModel>();
		for (int i = 0; i < paths.length; i++) {
			models.add(new TmsModel(paths[i], configNames[i]));
		}
	}

	/**
	 * 构造函数，需要给出模型所在的路径。配置文件名称默认为tms.config
	 * 
	 * @param paths
	 */

	public ClassPredict(String[] paths) {
		models = new ArrayList<TmsModel>();
		for (int i = 0; i < paths.length; i++) {
			models.add(new TmsModel(paths[i], "tms.config"));
		}
	}

	/**
	 * 利用所有的模型进行预测分数
	 * 
	 * @param line 读入的文本
	 * @return SvmResult
	 */
	public SvmResult[] calScore(String line, String str_splitTag) {
		int k = models.size();
		SvmResult[] result = new SvmResult[k];
		for (int i = 0; i < k; i++) {
			TmsModel tmsModel = models.get(i);
			List<String> words = new ArrayList<String>();
			for (String s : line.split(str_splitTag))
				words.add(s);
			result[i] = calSigleScore(words, tmsModel);
		}
		return result;

	}

	/**
	 * 计算SVM模型的分数.支持多模型
	 * 
	 * @param text 输入的文本
	 * @param model 模型
	 * @return
	 */
	public SvmResult calSigleScore(List<String> text, TmsModel tmsModel) {
		SvmModel svmModel = tmsModel.getModel();
		int label;
		double score;
		String descr;
		int nr_class = svmModel.getNrClass();
		double[] des_values = new double[nr_class * (nr_class - 1) / 2];
		SvmNode[] nodeList = consProForSVM(text, tmsModel);
		if (nodeList == null) { //如果该文本的词在词典 中没有出现过，则返回一个很小的值。
			return new SvmResult(0, 0, "该样本太短");
		}
		label = SvmModel.predictValues(svmModel, nodeList, des_values);
		score = svmModel.sumPreValue(des_values);
		descr = tmsModel.getLabelDescr(label);
		return new SvmResult(label, score, descr);

	}

	/**
	 * 利用输入的文本，以及读入的词典，构造SVM模型(libsvm,liblinear)的特定输入
	 * 该函数的目的就是构造文本的特征向量，并进行归一化处理。但是此处为了提高效率，利用Map代替Vector，只存储非0值。
	 * 
	 * @param text 里面存储的为一个个的词
	 * @param tmsModel 模型
	 * @return 返回的是SVM特定的输入结构,TmsNode[]
	 */

	public SvmNode[] consProForSVM(List<String> text, TmsModel tmsModel) {
		Map<Integer, Double> feature_map = new HashMap<Integer, Double>();
		Map<String, Integer> dic = tmsModel.getDic();
		//计算文本中每个词对应与词典的位置，以及相应的词频。
		for (int i = 0; i < text.size(); i++) {
			String term = (text.get(i).toString()).trim();
			if (dic.containsKey(term)) { //查询dic中是否包含该词
				int index = dic.get(term); //如果包含，则在feature_map中相应位置加1
				if (feature_map.containsKey(index)) { //针对两种情况，一种是该词已经在词典中，
					double count = feature_map.get(index);
					feature_map.put(index, count + 1.0);
				} else
					//另一种是该词未在词典中
					feature_map.put(index, 1.0);
			}
		}
		//根据特征权重的公式重新计算特征向量中的权重
		//依据的是局部公式和全局因子
		Object[] keys = feature_map.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			feature_map.put((Integer) keys[i], tmsModel.getLocalFun().Fun(feature_map.get(keys[i])) * tmsModel.getGlobalWeight().get(keys[i]));
		}

		//计算该文档特征向量的模
		double vec_sum = 0.0;

		for (int i = 0; i < keys.length; i++) {
			vec_sum += feature_map.get(keys[i]) * feature_map.get(keys[i]);
		}
		double vec_length = Math.sqrt(vec_sum);

		//归一化并构造SVM模型的输入
		SvmNode[] x = null;
		Arrays.sort(keys); //对feature_map中的key进行排序。主要是为了保证输入的SVM格式中Index是升序排列。
		if (vec_length > 0) {
			int m = keys.length;
			x = new SvmNode[m]; //SVM模型的输入格式
			/** 此处为构造SVM输入格式的句子 **/
			//计算文本中的词出现的词频数
			for (int j = 0; j < m; j++) {
				x[j] = new SvmNode();
				x[j].index = (Integer) keys[j];
				x[j].value = (double) (feature_map.get(keys[j]) / vec_length); //此处要进行归一化
			}
		}

		return x;
	}

	public static void main(String[] args) {
		String[] paths = { "D:\\svm\\result\\model" };
		ClassPredict libsvm = new ClassPredict(paths);

		String str_splitTag = "\\^"; //标题和内容经过分词后，各个词的分割符号

		String line = "紫怡^愿觉^：^“^箰在^似乮^是^什么^股票^都^有^可能^成为^黑马^Ｌ^可以^昮显^愿觉^到^Ｌ^一些^绩差股^乿^开始^出箰^补涨^Ｌ^听时^大盘^媠逿^个衬^的^态媿^乿^比较^昮显^Ｌ^象^这样^媠逿^到底";//1
		line = "朱广沪^圈定^了^25^人^集训^名单^Ｌ^其中^山东鲁能^成为^第一^国脚^大户^Ｌ^共有^6^名^球员^入选^Ｌ^苑^维^箮^首次^作为^国家阿^阿员^出箰^在^朱^家^军^名单^中";//-1
		SvmResult[] post_sc = libsvm.calScore(line, str_splitTag);
		for (SvmResult score : post_sc)
			System.out.println(score.getLabel() + "|" + score.getDescr() + "\t" + score.getScore());
	}
}
