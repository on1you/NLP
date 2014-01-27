package main.classify.svm;

public class SvmNode {
	public int index;
	public double value;
	
	public String toString(){
		return String.valueOf(index)+"\t"+String.valueOf(value);
	}
}
