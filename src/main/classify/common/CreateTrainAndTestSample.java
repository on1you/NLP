package main.classify.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import main.classify.knn.ComputeWordsVector;

import org.apache.commons.io.FileUtils;

/**创建训练样例集合与测试样例集合
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class CreateTrainAndTestSample {
	
	//针对英文文档，需要进行词干还原
	public void filterSpecialWords() throws IOException {
		String word;
		ComputeWordsVector cwv = new ComputeWordsVector();
		String fileDir = Constants.ROOT_DIR+"processedSample_includeNotSpecial";
		SortedMap<String,Double> wordMap = new TreeMap<String,Double>();
		wordMap = cwv.countWords(fileDir, wordMap);
		cwv.printWordMap(wordMap);//把wordMap输出到文件
		File[] sampleDir = new File(fileDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			File[] sample = sampleDir[i].listFiles();
			String targetDir = Constants.ROOT_DIR+"processedSampleOnlySpecial/"+sampleDir[i].getName();
			if(!new File(Constants.ROOT_DIR+"processedSampleOnlySpecial").exists()){
				new File(Constants.ROOT_DIR+"processedSampleOnlySpecial").mkdir();
			}
			File targetDirFile = new File(targetDir);
			if(!targetDirFile.exists()){
				targetDirFile.mkdir();
			}
			for(int j = 0;j < sample.length; j++){	
				String fileShortName = sample[j].getName();
				if(fileShortName.contains("stemed")){
					targetDir = Constants.ROOT_DIR+"processedSampleOnlySpecial/"+sampleDir[i].getName()+"/"+fileShortName.substring(0,5);
					FileWriter tgWriter= new FileWriter(targetDir);
					FileReader samReader = new FileReader(sample[j]);
					BufferedReader samBR = new BufferedReader(samReader);
					while((word = samBR.readLine()) != null){
						if(wordMap.containsKey(word)){
							tgWriter.append(word + "\n");
						}
					}
					tgWriter.flush();
					tgWriter.close();
				}
			}
		}
	}
	
	public void createTestSamples(String fileDir, double trainSamplePercent,int indexOfSample,String classifyResultFile) throws IOException {
		String  targetDir;
		FileWriter crWriter = new FileWriter(classifyResultFile);//测试样例正确类目记录文件
		File[] sampleDir = new File(fileDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			File[] sample = sampleDir[i].listFiles();
			double testBeginIndex = indexOfSample*(sample.length * (1-trainSamplePercent));//测试样例的起始文件序号
			double testEndIndex = (indexOfSample+1)*(sample.length * (1-trainSamplePercent));//测试样例集的结束文件序号
			for(int j = 0;j < sample.length; j++){				
				if(j > testBeginIndex && j< testEndIndex){//序号在规定区间内的作为测试样本，需要为测试样本生成类别-序号文件，方便统计准确率
					targetDir = Constants.ROOT_DIR+"TestSample"+indexOfSample+"/"+sampleDir[i].getName();
					crWriter.append(sampleDir[i].getName()+"_"+sample[j].getName() + " " + sampleDir[i].getName()+"\n");
				}else{//其余作为训练样本
					targetDir = Constants.ROOT_DIR+"TrainSample"+indexOfSample+"/"+sampleDir[i].getName();
				}
				targetDir = targetDir.replace("\\","/");
				File trainSamFile = new File(targetDir);
				if(!trainSamFile.exists()){
					trainSamFile.mkdir();
				}
				FileUtils.copyFileToDirectory(sample[j], trainSamFile);
			}
		}
		crWriter.flush();
		crWriter.close();
	}
	
	public void process() throws Exception {
		CreateTrainAndTestSample ctt = new CreateTrainAndTestSample();
		for (int i = 0; i < Constants.VERIFY_LOOP; i++) {
			String TrainDir = Constants.ROOT_DIR + "TrainSample" + i;
			String TestDir = Constants.ROOT_DIR + "TestSample" + i;
			if (!new File(TrainDir).exists()) {
				new File(TrainDir).mkdir();
			}
			if (!new File(TestDir).exists()) {
				new File(TestDir).mkdir();
			}
			String classifyRightCate = Constants.ROOT_DIR + "classifyRightCate" + i + ".txt";
			ctt.createTestSamples(Constants.ROOT_DIR+"processedSampleOnlySpecial", 0.9, i,classifyRightCate);
		}
	}
}
