package main.algorithm;

import org.junit.Test;

/**
 * 定义字符串的左旋转操作：把字符串前面的若干个字符移动到字符串的尾部，
 * 如把字符串 abcdef 左旋转 2 位得到字符串 cdefab。请实现字符串左旋转的函数，
 * 要求对长度为n的字符串操作的时间复杂度为O(n)， 空间复杂度为O(1)
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Dec 10, 2013 9:17:20 AM
 */
public class InvertString {

	/**
	 * 三步翻转法
	 * 
	 * 1、首先分为俩部分，X:abc，Y:def；
	 * 2、X->X^T，abc->cba， Y->Y^T，def->fed。
	 * 3、(X^TY^T)^T=YX，cbafed->defabc，即整个翻转。
	 */
	private String invert(String s,int start, int end){
		char temp;
		while(start < end){
			temp = s.charAt(start);
//			s.charAt(start) = s.charAt(end);
//			s.charAt(end-1) = temp;
			start ++;
			end --;
		}
		return s;
	}
	
	@Test
	public void rotate(){
		String str = "abcdef";int position = 3;
		invert(str,0,2);
		invert(str,3,str.length());
		invert(str,0,str.length());
	}
}
