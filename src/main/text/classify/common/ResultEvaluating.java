package main.text.classify.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 分类结果准度评估
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Dec 9, 2013 10:54:13 AM
 */
public class ResultEvaluating {
	
	private static final DecimalFormat DF = new DecimalFormat("##.###");
	private static int NUM_OF_CLS = 2;//分类个数
	
	/**
	 * 根据正确类目文件和分类结果文件统计出准确率
	 * 
	 * @param classifyResultFile 正确类目文件
	 * @param classifyResultFileNew 分类结果文件
	 * @return double 分类的准确率
	 * @throws IOException
	 */
	public double computeAccuracy(File classifyResultFile, File classifyResultFileNew) throws IOException {
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
	 * 从分类结果文件中读取map
	 * 
	 * @param classifyResultFileNew  类目文件
	 * @return Map<String, String> 由<文件名，类目名>保存的map
	 * @throws IOException
	 */
	public Map<String, String> getMapFromResultFile(File crFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(crFile));
		Map<String, String> res = new TreeMap<String, String>();
		String[] s;
		String line;
		while ((line = reader.readLine()) != null) {
			s = line.split(" ");
			res.put(s[0], s[1]);
		}
		reader.close();
		return res;
	}
	
	/**
	 * 根据正确类目文件和分类结果文计算混淆矩阵并且输出
	 * 
	 * @param rightCate  正确类目对应map
	 * @param resultCate 分类结果对应map
	 * @return double 分类的准确率
	 * @throws IOException
	 */
	private void computerConfusionMatrix(Map<String, String> rightCate, Map<String, String> resultCate) {
		SortedSet<String> cateNames = new TreeSet<String>();
		cateNames.addAll(Constants.clsName.keySet());
		String[] cateNamesArray = cateNames.toArray(new String[0]);
		NUM_OF_CLS = cateNamesArray.length;
		int[][] confusionMatrix = new int[NUM_OF_CLS][NUM_OF_CLS];//首先求出类目对应的数组索引
		Map<String, Integer> cateNamesToIndex = new TreeMap<String, Integer>();
		for (int i = 0; i < NUM_OF_CLS; i++) {
			cateNamesToIndex.put(cateNamesArray[i], i);
		}
		for (Map.Entry<String, String> entry : rightCate.entrySet()) {
			confusionMatrix[cateNamesToIndex.get(entry.getValue())][cateNamesToIndex.get(resultCate.get(entry.getKey()))]++;
		}
		//输出混淆矩阵
		double[] hangSum = new double[NUM_OF_CLS];
		System.out.print(format(""));
		for (int i = 0; i < NUM_OF_CLS; i++) {
			System.out.print(format(Constants.clsName.get(cateNamesArray[i])));
		}
		System.out.print(format("hit(%)"));
		System.out.println(format("+"));
		for (int i = 0; i < NUM_OF_CLS; i++) {
			System.out.print(format(Constants.clsName.get(cateNamesArray[i])));
			for (int j = 0; j < NUM_OF_CLS; j++) {
				System.out.print(format(confusionMatrix[i][j]));
				hangSum[i] += confusionMatrix[i][j];
			}
			System.out.print(DF.format(confusionMatrix[i][i] / hangSum[i]));
			System.out.println(format("+"));
		}
	}
	
	/**
	 * 格式化输出
	 * @param o
	 * @return
	 */
	private String format(Object o) {
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
}
