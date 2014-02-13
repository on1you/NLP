package main.algorithm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 无界优先级队列（实现堆排序）
 * 节约空间及时间，算法复杂度：N*logK，普通排序的复杂度：NlgN
 * 适用范围：1、寻找最大(小)值  2、top k
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Feb 12, 2014 2:54:59 PM
 */
public class TestPriorityQueue {

	public static void main(String[] args) {
		
//		topK(5);
//		max();
		min();
	}
	
	/**
	 * 找出最大的5个数
	 * 最小值始终保持在对首，比对首大则入队，同时移除对首
	 * 
	 * 默认自然排序：升序
	 */
	static void topK(int k){
		PriorityQueue<Integer> queue = new PriorityQueue<>(k);
		List<Integer> list = Arrays.asList(1,4,6,3,1000,4,78,543,98,345,1000,65,32);
		for (Integer i : list) {
			if (queue.size() < k) {//初始化填充队列
				queue.add(i);
			}else if (i > queue.peek()) {
				queue.add(i);
				queue.poll();//移除最小值
			}
		}
		System.out.println(queue);
	}
	
	/**
	 * 求最大值
	 * 
	 * 队列的对象需倒序，最大值始终保持在对首
	 * 若内存不足，为节省空间，可设队列容量为1，大于对首则替换
	 */
	static void max(){
		//初始化传入比较器
		PriorityQueue<Integer> queue = new PriorityQueue<>(11,new IntegerComparetor());
		queue.addAll(Arrays.asList(1,4,6,3,1000,4,78,543,98,345,56,65,32));
		System.out.println("max: " + queue.peek());
	}
	
	static void min(){
		PriorityQueue<Integer> queue = new PriorityQueue<>(11);
		queue.addAll(Arrays.asList(1,4,6,3,1000,4,78,543,98,345,56,65,32));
		System.out.println("max: " + queue.peek());
	}
}

/**
 * 整型倒序比较器
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Feb 12, 2014 3:16:53 PM
 */
class IntegerComparetor implements Comparator<Integer>{
	@Override
	public int compare(Integer o1, Integer o2) {
		return o2 - o1;
	}
}
