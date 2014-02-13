package main.algorithm;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.junit.Test;

/**
 * 位图算法
 * 使用场景：通常是用来存储数据，判断某个数据存不存在或者判断数组是否存在重复
 * 	1、在2.5亿整数中找出不重复的整数
 * 	2、给你一堆电话号码列表，数量大概在千万级，要求从中找出所有重复的电话号码
 * 	3、给40亿个不重复的unsinged int的整数，没排过序，然后再给出一个数，如何快速判断这个数是否在那40亿个数当中
 * 	4、海量数据排序问题：文件包含1千万条电话号码记录(10*7次方)，每条记录都是7位整数，没有重复的整数。要求对文件进行排序，注意大约只有1MB的内存空间可用
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Feb 12, 2014 4:38:10 PM
 */

public class TestBitMap {
	
	private static final int BITSPERWORD = 32; // 整数位数  
    private static final int SHIFT = 5;  
    private static final int MASK = 0x1F; // 5位遮蔽 0B11111  
    private static final int N = 10000000;  
    // 用int数组来模拟位数组，总计(1 + N / BITSPERWORD)*BITSPERWORD位，足以容纳N  
    private static int[] a = new int[(1 + N / BITSPERWORD)];
    
	public static void main(String[] args) {
		bitsort(new int[] { 1, 100, 2, 10000, 9999, 4567, 78902 });  
	}
	
	/**
	 * java 自带bitSet
	 */
	@Test
	public void bitSetSort(){
		List<Integer> list = Arrays.asList(1,4,6,3,1000,4,78,543,98,345,1000,65,32);
		BitSet bit = new BitSet(20);
		for (Integer i : list) {
			bit.set(i);
		}
		System.out.println(bit);//排序
		System.out.println(bit.get(7));//判断某元素是否存在
	}
	
	/**
	 * 自己实现的位排序（通过数组模拟）
	 * @param array
	 */
	public static void bitsort(int[] array) {  
//        for (int i = 0; i < N; i++)  
//            clr(i); // 位数组所有位清0  
        for (int i = 0; i < array.length; i++)  
            set(array[i]); // 阶段2  
        for (int i = 0; i < N; i++)  
            if (test(i))  
                System.out.println(i);  
    }  
  
    // 置a[i>>SHIFT]的第(i & MASK)位为1，也就是位数组的第i位为1  
    public static void set(int i) {  
        a[i >> SHIFT] |= (1 << (i & MASK));  
    }  
  
    // 置a[i>>SHIFT]的第(i & MASK)位为0,也就是位数组的第i位为0  
    public static void clr(int i) {  
        a[i >> SHIFT] &= ~(1 << (i & MASK));  
    }  
  
    // 测试位数组的第i位是否为1  
    public static boolean test(int i) {  
        return (a[i >> SHIFT] & (1 << (i & MASK))) == (1 << (i & MASK));  
    }  
	/**
	 * 两千万以内的素数
	 * 素数：只能被1和自身整除
	 */
	@Test
	public void prime(){
		int n = 20000000;  
        long start = System.currentTimeMillis();  
        BitSet b = new BitSet(n+1);  
        int count = 0;  
        int i;  
        for(i = 2; i <= n;i++){  
            b.set(i);  
        }  
        i = 2;  
        while(i*i <=n){  
            if(b.get(i)){  
                count++;  
                int k = 2 * i;  
                while(k <= n){  
                    b.clear(k);  
                    k +=i;  
                }  
            }  
            i++;  
        }  
        while(i <= n){  
            if(b.get(i)){  
                count++;  
            }  
            i++;  
        }  
        long end = System.currentTimeMillis();  
        System.out.println("count = " + count);  
        System.out.println((end-start) +" milliseconds"); 
	}
}
