package main.classify.common;

import java.io.Serializable;
import java.util.Map;

public class TrainModel implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public double totalWordsNum;//记录所有训练集的总词数
	Map<String, Double> cateWordsNum;//保存训练集每个类别的总词数
	Map<String, Double> cateWordsProb;//保存训练样本每个类别中每个属性词的出现词数
	
	public TrainModel(){}
	public TrainModel(double total, Map<String, Double> num,  Map<String, Double> prop){
		this.totalWordsNum = total;
		this.cateWordsNum = num;
		this.cateWordsProb = prop;
	}
	public double getTotalWordsNum() {
		return totalWordsNum;
	}
	public void setTotalWordsNum(double totalWordsNum) {
		this.totalWordsNum = totalWordsNum;
	}
	public Map<String, Double> getCateWordsNum() {
		return cateWordsNum;
	}
	public void setCateWordsNum(Map<String, Double> cateWordsNum) {
		this.cateWordsNum = cateWordsNum;
	}
	public Map<String, Double> getCateWordsProb() {
		return cateWordsProb;
	}
	public void setCateWordsProb(Map<String, Double> cateWordsProb) {
		this.cateWordsProb = cateWordsProb;
	}
	
	
}
