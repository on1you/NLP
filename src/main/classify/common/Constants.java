package main.classify.common;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import main.treesplit.util.IOUtil;

import org.ansj.util.FilterModifWord;

public class Constants {
	public static final String COM_DIR = "d:/hotmine/";//固定测试目录
	public static final String ROOT_DIR = COM_DIR + "posnegmine/";//测试根目录
	public static final String DATA_PRE_PROCESS_DIR = ROOT_DIR + "trainset/";//源数据目录
	public static final String DATA_SPECIAL_DIR = ROOT_DIR + "featurewords/";//特征词提取结果目录
	public static final String DATA_CLUSTER_DIR = ROOT_DIR + "clusterTestSample/";//聚类测试集目录
	public static final String DATA_CLUSTER_RESULT = ROOT_DIR + "KmeansClusterResult/";//聚类结果存储目录
	public static final int VERIFY_LOOP = 1;//分类交叉验证次数
	public static Map<String, String> clsName = new HashMap<String, String>();//类别映射表
	public static Map<String, Double> weight = new HashMap<String, Double>();//词性权重
	
	static{
		try {
			weight.put("i", 0.6);
			weight.put("l", 0.6);
			weight.put("a", 0.5);
			weight.put("ad", 0.3);
			weight.put("an", 0.6);
			weight.put("j", 0.7);
			weight.put("v", 0.3);
			weight.put("vg", 0.2);
			weight.put("vd", 0.4);
			weight.put("vn", 0.6);
			weight.put("n", 0.8);
//			weight.put("nr", 0.8);
			weight.put("nz", 0.5);
			weight.put("nl", 0.5);
			weight.put("ng", 0.2);
			weight.put("nt", 0.6);
			weight.put("b", 0.4);
			//类别映射表
			BufferedReader reader1 = IOUtil.getReader(ROOT_DIR + "class.txt", IOUtil.GBK);
			String cls = null;
			while ((cls = reader1.readLine()) != null) {
				String[] clz = cls.split("\t");
				clsName.put(clz[0], clz[1]);
			}
			//停用词表
			HashMap<String, String> updateDic = new HashMap<String, String>();
			BufferedReader reader = IOUtil.getReader(COM_DIR + "stopwords.txt",IOUtil.UTF8);
			 String word = null;
			 while ((word = reader.readLine()) != null) {
				 updateDic.put(word, FilterModifWord._stop);
			 }
			 FilterModifWord.setUpdateDic(updateDic);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
