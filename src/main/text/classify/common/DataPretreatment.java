package main.text.classify.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import love.cq.util.StringUtil;
import main.treesplit.util.IOUtil;

import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.FilterModifWord;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 针对中文文本的数据预处理
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn Dec 10, 2013 4:43:57 PM
 */
public class DataPretreatment {
	private static final Logger log = LoggerFactory.getLogger(DataPretreatment.class);

	/**
	 * SVM训练样本预处理
	 * @param strDir
	 * @throws Exception
	 */
	public void read2file(String strDir,File destFile) throws Exception {
		File fileDir = new File(strDir);
//		File destFile = new File("D:\\svm\\svm_trainset\\trade_seged.txt");
		if (!fileDir.exists()) {
			log.error("File not exist:" + strDir);
			return;
		}
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile, true), "UTF-8"));
		File[] srcFiles = fileDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			String fileFullName = srcFiles[i].getCanonicalPath();
			String cls = srcFiles[i].getParentFile().getName();
			log.info("parse text in {}", fileFullName);
			if (!new File(fileFullName).isDirectory()) {//确认子文件名不是目录如果是可以再次递归调用
				List<Term> tlList = new ArrayList<>();
				BufferedReader srcFileBR = IOUtil.getReader(fileFullName, IOUtil.GBK);
				String line;
				while ((line = srcFileBR.readLine()) != null) {
					if (StringUtil.isBlank(line.trim())) {
						continue;
					}
					List<Term> terms = NlpAnalysis.parse(line.replaceAll("\\s|\n|　", ""), Constants.tool);
					terms = FilterModifWord.modifResult(terms);//停止词过滤
					tlList.addAll(terms);
				}
				bw.write(cls + "\t" + tlList.toString().replaceAll("\\[|]|,|\\s", ""));
				bw.newLine();
				bw.flush();
			} else {
				fileFullName = fileFullName.replace("\\", "/");
				read2file(fileFullName,destFile);
			}
		}
		bw.close();
	}

	/**
	 * NativeBayesian 训练样本预处理
	 * @param strDir
	 * @throws Exception
	 */
	public void process(String strDir) throws Exception {
		File fileDir = new File(strDir);
		if (!fileDir.exists()) {
			log.error("File not exist:" + strDir);
			return;
		}
		String subStrDir = strDir.substring(strDir.lastIndexOf("/"));//trainset
		String dirTarget = Constants.DATA_SPECIAL_DIR + subStrDir;
		if (!new File(Constants.DATA_SPECIAL_DIR).exists()) {
			new File(Constants.DATA_SPECIAL_DIR).mkdir();
		}
		File fileTarget = new File(dirTarget);
		if (!fileTarget.exists()) {
			fileTarget.mkdir();
		}
		File[] srcFiles = fileDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			String fileFullName = srcFiles[i].getCanonicalPath();
			String fileShortName = srcFiles[i].getName();
			log.info("parse text in {}", fileFullName);
			if (!new File(fileFullName).isDirectory()) {//确认子文件名不是目录如果是可以再次递归调用
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(dirTarget + "/" + fileShortName);
				extractWords(fileFullName, stringBuilder.toString());
			} else {
				fileFullName = fileFullName.replace("\\", "/");
				process(fileFullName);
			}
		}
	}

	/**
	 * 提取特征词
	 * 
	 * @param srcDir 源文件文件目录的绝对路径
	 * @param targetDir 生成的目标文件的绝对路径
	 * @throws IOException
	 */
	private static void extractWords(String srcDir, String destDir) throws IOException {
		BufferedReader srcFileBR = IOUtil.getReader(srcDir, IOUtil.UTF8);
		List<String> words = new ArrayList<String>();
		String line;
		while ((line = srcFileBR.readLine()) != null) {
			if (StringUtil.isBlank(line.trim())) {
				continue;
			}
			List<String> lineWords = wordSeg(line.replaceAll("\\s|\n|　", ""));
			words.addAll(lineWords);
		}
		if (words.size() > 3) {
			FileUtils.writeLines(new File(destDir), IOUtil.UTF8, words);
		}
		srcFileBR.close();
	}

	/**
	 * 分词 特征抽取（屏蔽地名及人名，关注事件本身） 特别说明：进行分类时，分词和特征词筛选规则需与此保持一致，否则结果会有较大差异
	 * 
	 * @param content
	 * @return
	 */
	private static List<String> wordSeg(String content) {
		List<String> words = new ArrayList<String>();
		//		List<Term> terms = ToAnalysis.parse(content);
		List<Term> terms = NlpAnalysis.parse(content, Constants.tool);
		terms = FilterModifWord.modifResult(terms);//停止词过滤
		for (Term term : terms) {
			String txt = term.getName();
			String natrue = term.getNatrue().natureStr;
			if (StringUtil.isBlank(txt.trim()) || txt.trim().length() == 1 || natrue.matches("^n(s|r|ull)$")) {//剔除地名及人名
				continue;
			}
			if (natrue.matches("^(n|a|v(n|i|l|g)|b|i|j|l).*$")) {//特征词筛选规则
				//			if (Constants.weight.containsKey(natrue)) {//根据词性及权重进行筛选
				words.add(txt);
			}
		}
		return words;
	}

	public static void main(String[] args) throws Exception {
		DataPretreatment prev = new DataPretreatment();
//		prev.read2file("d:/hotmine/trade/trainset/",new File("D:\\svm\\svm_trainset\\trade_seged.txt"));//行业多分类语料
		prev.process(Constants.DATA_PRE_PROCESS_DIR);//独立性分类语料
	}
}
