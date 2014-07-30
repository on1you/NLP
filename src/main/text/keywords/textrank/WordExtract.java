package main.text.keywords.textrank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import main.text.words.seg.ansj.WordSeg;


/**
 * 基于TextRank的关键词抽取
 * 
 * @author liu_xueping
 * 
 */
class WDataSet {
	Graph graph = new Graph();
	ArrayList<Double> w = new ArrayList<Double>();
	ArrayList<Double> wBack = new ArrayList<Double>();
	List<String> list = new ArrayList<String>();
	ArrayList<String> subList = new ArrayList<String>();
}

public class WordExtract extends AbstractExtractor {

	public WordExtract() {
		precision = 1.0;
		dN = 0.85;
	}

	public WordExtract(boolean isSeg) throws Exception {
		this();
		if (isSeg) {
			tag = new WordSeg();
		}
	}

	private WDataSet getWord(String[] words) {
		Set<String> set = new TreeSet<String>();
		WDataSet wds = new WDataSet();

		wds.list = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() > 0)
				wds.list.add(words[i]);
		}

		for (int i = 0; i < wds.list.size(); i++) {
			String temp = wds.list.get(i);
			set.add(temp);
		}

		Iterator<String> ii = set.iterator();
		while (ii.hasNext()) {
			String str = ii.next();
			wds.subList.add(str);
		}
		return wds;
	}

	private WDataSet mapInit(int window, WDataSet wds) {
		TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
		Iterator<String> ii = wds.subList.iterator();
		int nnn = 0;
		while (ii.hasNext()) {
			String s = ii.next();
			Vertex vertex = new Vertex(s);
			wds.graph.addVertex(vertex);
			wds.w.add(1.0);
			wds.wBack.add(1.0);
			treeMap.put(s, nnn);
			nnn++;
		}

		String id1, id2;
		int index1, index2;

		int length = wds.list.size();
		while (true) {
			if (window > length)
				window /= 2;
			else if (window <= length || window <= 3)
				break;
		}

		for (int i = 0; i < wds.list.size() - window; i++) {
			id1 = wds.list.get(i);
			index1 = treeMap.get(id1);
			for (int j = i + 1; j < i + window; j++) {
				id2 = wds.list.get(j);
				index2 = treeMap.get(id2);
				wds.graph.addEdge(index2, index1);
			}
		}
		return wds;
	}

	boolean isCover(WDataSet wds) {
		int i;
		double result = 0.0;

		for (i = 0; i < wds.graph.getNVerts(); i++) {
			result += Math.abs(wds.w.get(i) - wds.wBack.get(i));
		}

		if (result < precision)
			return true;
		else
			return false;
	}

	public void toBackW(WDataSet wds) {
		int i;

		for (i = 0; i < wds.graph.getNVerts(); i++) {
			wds.wBack.set(i, wds.w.get(i));
		}
	}

	public WDataSet cal(WDataSet wds) {
		int i, j, forwardCount, times = 0;
		double sumWBackLink, newW;
		ArrayList<Vertex> nextList;
		ArrayList<Integer> nextWList;
		Vertex vertex;

		while (true) {
			times++;
			for (i = 0; i < wds.graph.getNVerts(); i++) {
				vertex = wds.graph.getVertex(i);
				nextList = vertex.getNext();
				nextWList = vertex.getWNext();
				if (nextList != null) {
					sumWBackLink = 0;
					for (j = 0; j < nextWList.size(); j++) {
						vertex = nextList.get(j);
						int ww = nextWList.get(j);
						int temp = vertex.index;
						forwardCount = vertex.getForwardCount();
						if (forwardCount != 0)
							sumWBackLink += wds.wBack.get(temp) * ww / forwardCount;
					}
					newW = (1 - dN) + dN * sumWBackLink;
					wds.w.set(i, newW);
				}
			}
			if (isCover(wds) == true) {
//				System.out.println("Iteration Times: " + times);
				break;
			}
			toBackW(wds);
		}
		return wds;
	}

	public ArrayList<Integer> normalized(WDataSet wds) {
		ArrayList<Integer> wNormalized = new ArrayList<Integer>();
		double max, min, wNDouble;
		int i, wNormalInt;
		double wNormal;
		max = Collections.max(wds.w);
		min = Collections.min(wds.w);

		if (max != min)
			for (i = 0; i < wds.graph.getNVerts(); i++) {
				wNDouble = wds.w.get(i);
				wNormal = (wNDouble - min) / (max - min);
				wNormalInt = (int) (100 * wNormal);
				wds.w.set(i, wNormal);
				wNormalized.add(wNormalInt);
			}
		else
			for (i = 0; i < wds.graph.getNVerts(); i++)
				wNormalized.add(100);
		return wNormalized;
	}

	public LinkedHashMap<String, Integer> selectTop(int selectCount, WDataSet wds) {
		int i, j, index;
		double max;
		LinkedHashMap<String, Integer> mapList = new LinkedHashMap<String, Integer>();

		if (wds.graph.getNVerts() == 0)
			return mapList;

		ArrayList<Integer> wNormalized = normalized(wds);
		toBackW(wds);

		int temp = wds.subList.size();
		if (selectCount > temp)
			selectCount = temp;

		for (j = 0; j < selectCount; j++) {
			max = -1.0;
			index = -1;
			for (i = 0; i < wds.graph.getNVerts(); i++) {
				if (wds.wBack.get(i) > max) {
					max = wds.wBack.get(i);
					index = i;
				}
			}
			if (index != -1) {
				mapList.put(wds.graph.getVertex(index).getId(), wNormalized.get(index));
				wds.wBack.set(index, -2.0);
			}
		}
		return mapList;
	}

	public WDataSet proceed(String[] words) {
		WDataSet wds1, wds2;
		wds1 = getWord(words);
		//		long time1 = System.currentTimeMillis();
		//		System.out.println("InitGraph...");
		wds2 = mapInit(windowN, wds1);
		//		System.out.println("Succeed In InitGraph!");
		//		System.out.println("Now Calculate the TextRank Value...");
		wds1 = cal(wds2);
		//		double time = (System.currentTimeMillis() - time1) / 1000.0;
		//		System.out.println("Time using: " + time + "s");
		//		System.out.println("TextRank Value Has Been Calculated!");
		return wds1;
	}

	public Map<String, Integer> extract(String str, int num) {
		String[] words;
		if (tag != null)
			words = tag.words2Array(str);
		else
			words = str.split("\\s+");
		WDataSet wds = proceed(words);
		LinkedHashMap<String, Integer> mapList = selectTop(num, wds);
		return mapList;
	}

	@Override
	public String extract(String str, int num, boolean isWeighted) {
		return extract(str, num).toString();
	}

	public static void main(String[] args)  throws Exception{
		String file = "D:/LDA/data/LdaOriginalDocs/1.txt";
//		String content = "1988年 12月 30日， 原告 陈震 、 陈春 等为 与 被告 日本 海运 株式会社 ( 现为 商船 三井 株式会社 ) 定期 租船合同 欠款 及 侵权 赔偿 纠纷 一案 向 上海 海事 法院 提起 诉讼 ， 追索 “顺丰” 轮、“新太平” 轮船 舶 租金 ”轮租金";
//		String content = readFile(file);
		String content = "日专家:解放军可用100枚导弹把东京打成火海";
		AbstractExtractor key = new WordExtract(true);
		Map<String, Integer> words = key.extract(content, 3);

		for (Map.Entry<String, Integer> entry : words.entrySet()) {
			String keymap = entry.getKey().toString();
			String value = entry.getValue().toString();
			System.out.println("key=" + keymap + " value=" + value);
		}
	}
	
	private static String readFile(String file) throws FileNotFoundException, IOException {  
        StringBuffer sb = new StringBuffer();  
        InputStreamReader is = new InputStreamReader(new FileInputStream(file), "gbk");  
        BufferedReader br = new BufferedReader(is);  
        String line = br.readLine();  
        while (line != null) {  
            sb.append(line).append("\r\n");  
            line = br.readLine();  
        }  
        br.close();  
        return sb.toString();  
    }  
    
}
