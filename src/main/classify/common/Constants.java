package main.classify.common;

import java.io.BufferedReader;
import java.util.HashMap;

import main.treesplit.util.IOUtil;

import org.ansj.util.FilterModifWord;

public class Constants {
	public static final String ROOT_DIR = "d:/testminezh/";
	public static final String DATA_PRE_PROCESS_DIR = ROOT_DIR + "trainset" + "/";
	public static double totalWordsNum;
	public static final int VERIFY_LOOP = 5;
	
	static{
		try {
			HashMap<String, String> updateDic = new HashMap<String, String>();
			BufferedReader reader = IOUtil.getReader("D:/testminezh/stopwords.txt",IOUtil.UTF8);
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
