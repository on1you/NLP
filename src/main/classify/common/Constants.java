package main.classify.common;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

import main.treesplit.util.IOUtil;

import org.ansj.util.FilterModifWord;

public class Constants {
	public static final String COM_DIR = "d:/hotmine/";
	public static final String ROOT_DIR = COM_DIR + "posnegmine/";
	public static final String DATA_PRE_PROCESS_DIR = ROOT_DIR + "trainset/";
	public static final String DATA_SPECIAL_DIR = ROOT_DIR + "featurewords/";
	public static final int VERIFY_LOOP = 1;
	public static Map<String, String> clsName = new HashMap<String, String>();
	static{
		try {
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
