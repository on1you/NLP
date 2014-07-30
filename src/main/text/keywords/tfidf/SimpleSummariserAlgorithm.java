package main.text.keywords.tfidf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

/**
 * 
 * <p>Title:</p> <p>Description: SimpleSummariser </p>
 * 
 * @createDate：2013-8-26
 * @author snow
 * @version 1.0
 */
public class SimpleSummariserAlgorithm {

	/**
	 * 对给定文本提取摘要 1、提取文本关键词（tf-idf） 2、根据关键词出现频度提取关键句子 3、根据提取句子拼接成摘要
	 * 
	 * @param input
	 * @param numSentences
	 * @return
	 */
	public static String summarise(String input, int numSentences) {
		// get the frequency of each word in the input
		Map<String, Integer> wordFrequencies = segStr(input);

		// now create a set of the X most frequent words
		Set<String> mostFrequentWords = getMostFrequentWords(100, wordFrequencies).keySet();

		// break the input up into sentences
		// workingSentences is used for the analysis, but
		// actualSentences is used in the results so that the 
		// capitalisation will be correct.
		String[] workingSentences = getSentences(input);
		String[] actualSentences = workingSentences.clone();

		// iterate over the most frequent words, and add the first sentence 
		// that includes each word to the result
		Set<String> outputSentences = new LinkedHashSet<String>();
		Iterator<String> it = mostFrequentWords.iterator();
		while (it.hasNext()) {
			String word = (String) it.next();
			for (int i = 0; i < workingSentences.length; i++) {
				if (workingSentences[i].indexOf(word) >= 0) {
					outputSentences.add(actualSentences[i]);
					break;
				}
				if (outputSentences.size() >= numSentences) {
					break;
				}
			}
			if (outputSentences.size() >= numSentences) {
				break;
			}

		}

		List<String> reorderedOutputSentences = reorderSentences(outputSentences, input);

		StringBuffer result = new StringBuffer("");
		it = reorderedOutputSentences.iterator();
		while (it.hasNext()) {
			String sentence = (String) it.next();
			result.append(sentence);
			result.append("."); // This isn't always correct - perhaps it should be whatever symbol the sentence finished with
			if (it.hasNext()) {
				result.append(" ");
			}
		}

		return result.toString();
	}

	/**
	 * 根据关键词提取摘要 1、根据关键词出现频度提取关键句子 2、根据提取句子拼接成摘要
	 * 
	 * @param input
	 * @param keywords
	 * @return
	 */
	public static String summarise(String input, String keywords) {
		Set<String> mostFrequentWords = new HashSet<String>();
		String[] ks = keywords.split(" ");
		for (String s : ks) {
			mostFrequentWords.add(s);
		}
		String[] workingSentences = getSentences(input);
		String[] actualSentences = workingSentences.clone();

		// iterate over the most frequent words, and add the first sentence 
		// that includes each word to the result
		Set<String> outputSentences = new LinkedHashSet<String>();
		Iterator<String> it = mostFrequentWords.iterator();
		while (it.hasNext()) {
			String word = (String) it.next();
			for (int i = 0; i < workingSentences.length; i++) {
				if (workingSentences[i].indexOf(word) >= 0) {
					outputSentences.add(actualSentences[i]);
					break;
				}
				if (outputSentences.size() >= 1) {
					break;
				}
			}
			if (outputSentences.size() >= 1) {
				break;
			}
		}

		List<String> reorderedOutputSentences = reorderSentences(outputSentences, input);

		StringBuffer result = new StringBuffer("");
		it = reorderedOutputSentences.iterator();
		while (it.hasNext()) {
			String sentence = (String) it.next();
			result.append(sentence);
			result.append("."); // This isn't always correct - perhaps it should be whatever symbol the sentence finished with
			if (it.hasNext()) {
				result.append(" ");
			}
		}

		return result.toString();
	}

	/**
	 * 
	 * @Title: reorderSentences
	 * @Description: 将句子按顺序输出
	 * @param @param outputSentences
	 * @param @param input
	 * @param @return
	 * @return List<String>
	 * @throws
	 */
	private static List<String> reorderSentences(Set<String> outputSentences, final String input) {
		// reorder the sentences to the order they were in the 
		// original text
		ArrayList<String> result = new ArrayList<String>(outputSentences);

		Collections.sort(result, new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				String sentence1 = (String) arg0;
				String sentence2 = (String) arg1;

				int indexOfSentence1 = input.indexOf(sentence1.trim());
				int indexOfSentence2 = input.indexOf(sentence2.trim());
				int result = indexOfSentence1 - indexOfSentence2;

				return result;
			}

		});
		return result;
	}

	/**
	 * 
	 * @Title: getMostFrequentWords
	 * @Description: 对分词进行按数量排序,取出前num个
	 * @param @param num
	 * @param @param words
	 * @param @return
	 * @return Map<String,Integer>
	 * @throws
	 */
	public static Map<String, Integer> getMostFrequentWords(int num, Map<String, Integer> words) {

		Map<String, Integer> keywords = new LinkedHashMap<String, Integer>();
		int count = 0;
		// 词频统计
		List<Map.Entry<String, Integer>> info = new ArrayList<Map.Entry<String, Integer>>(words.entrySet());
		Collections.sort(info, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
				return obj2.getValue() - obj1.getValue();
			}
		});

		// 高频词输出
		for (int j = 0; j < info.size(); j++) {
			// 词-->频
			if (info.get(j).getKey().length() > 1) {
				if (num > count) {
					keywords.put(info.get(j).getKey(), info.get(j).getValue());
					count++;
				} else {
					break;
				}
			}
		}
		return keywords;
	}

	/**
	 * 
	 * @Title: segStr
	 * @Description: 返回LinkedHashMap的分词
	 * @param @param content
	 * @param @return
	 * @return Map<String,Integer>
	 * @throws
	 */
	public static Map<String, Integer> segStr(String content) {
		// 分词
		List<Term> terms = ToAnalysis.parse(content);
		Map<String, Integer> words = new LinkedHashMap<String, Integer>();
		for (Term term : terms) {
			if (words.containsKey(term.getName())) {
				words.put(term.getName(), words.get(term.getName()) + 1);
			} else {
				words.put(term.getName(), 1);
			}
		}
		return words;
	}

	/**
	 * 
	 * @Title: getSentences
	 * @Description: 把段落按. ! ?分隔成句组
	 * @param @param input
	 * @param @return
	 * @return String[]
	 * @throws
	 */
	public static String[] getSentences(String input) {
		if (input == null) {
			return new String[0];
		} else {
			// split on a ".", a "!", a "?" followed by a space or EOL
			//"(\\.|!|\\?)+(\\s|\\z)"
			return input.split("(\\.|，|。|——|!|\\?)");
		}

	}

	public static void main(String[] args) {
		String s = "内蒙奈曼旗公安局副局长女儿婚礼收礼金12余万被免职——北京东方剪报舆情监... - 人民网北京11月12日电 据中纪委监察部网站消息，继9月通报6起违反八项规定和自治区相关规定典型问题后，内蒙古自治区纪委、监察厅日前再次通报7起违反八项规定和自治区相关规定典型问";
		System.out.println(summarise(s, "副局长 免职"));
	}
}
