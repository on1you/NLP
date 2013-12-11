package main.classify.kmeans;

import java.io.IOException;
import java.text.SimpleDateFormat;

import main.classify.common.Constants;
import main.classify.common.DataPretreatment;

/**聚类器主类，提供主函数入口
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class KmeansClusterDriver {
 
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataPretreatment prev = new DataPretreatment();
		ComputeWordsVector computeV = new ComputeWordsVector();
		//KmeansSVDCluster kmeansCluster1 = new KmeansSVDCluster();
		KmeansCluster kmeansCluster2 = new KmeansCluster();
		prev.process(Constants.DATA_PRE_PROCESS_DIR);//数据预处理
		//下面创建聚类算法的测试样例集合
		String srcDir = "D:/hotmine/testmine/processedSample_includeNotSpecial/";
		String destDir = "D:/hotmine/testmine/clusterTestSample/";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String beginTime = sdf.format(new java.util.Date());  
		System.out.println("程序开始执行时间:"+beginTime);  
		String[] terms = computeV.createTestSamples(srcDir, destDir);
//		//kmeansCluster1.KmeansClusterMain(destDir, terms);
		kmeansCluster2.KmeansClusterMain(destDir);
		String endTime = sdf.format(new java.util.Date());
		System.out.println("程序结束执行时间:"+endTime);
	}
}
