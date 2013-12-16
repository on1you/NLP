package main.classify.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import love.cq.util.StringUtil;
import main.treesplit.util.IOUtil;

import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.FilterModifWord;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 针对中文文本的数据预处理
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Dec 10, 2013 4:43:57 PM
 */
public class DataPretreatment {
	private static final Logger log = LoggerFactory.getLogger(DataPretreatment.class);
	
	public void process(String strDir) throws Exception{
		File fileDir = new File(strDir);
		if(!fileDir.exists()){
			log.error("File not exist:" + strDir);
			return;
		}
		String subStrDir = strDir.substring(strDir.lastIndexOf("/"));//trainset
		String dirTarget = Constants.DATA_SPECIAL_DIR + subStrDir;
		if(!new File(Constants.DATA_SPECIAL_DIR).exists()){
			new File(Constants.DATA_SPECIAL_DIR).mkdir();
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
				process(fileFullName);
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
			List<String> lineWords = wordSeg(line.replaceAll("\\s|\n|　", ""));
			words.addAll(lineWords);
		}
		FileUtils.writeLines(new File(destDir), IOUtil.UTF8, words);
		srcFileBR.close();
	}
	
	/**
	 * 分词
	 * @param content
	 * @return
	 */
	private static List<String> wordSeg(String content){
		List<String> words = new ArrayList<String>();
		List<Term> terms = ToAnalysis.parse(content);
		terms = FilterModifWord.modifResult(terms);//停止词过滤
		new NatureRecognition(terms).recognition() ;
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
	
	public static void main(String[] args) throws Exception {
		DataPretreatment prev = new DataPretreatment();
		prev.process(Constants.DATA_PRE_PROCESS_DIR);
	}
}
