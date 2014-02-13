package main.algorithm;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 双端队列的实现ArrayDeque,LinkedList,LinkedBlockingDeque(支持多线程)
 * 首尾都可操作的队列
 * 
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Feb 13, 2014 1:55:53 PM
 */
public class TestDeque {
    public static void main(String[] args) {  
    	SimuStack<Integer> stack = new SimuStack<>();  
        for (int i = 0; i < 5; i++) {  
            stack.push(i);  
        }  
        System.out.println(stack);  
        System.out.println("After pushing 5 elements: " + stack);  
        int m = stack.pop();  
        System.out.println("Popped element = " + m);  
        System.out.println("After popping 1 element : " + stack);  
        int n = stack.peek();  
        System.out.println("Peeked element = " + n);  
        System.out.println("After peeking 1 element : " + stack);  
    }  
}

/**
 * 通过双端队列模拟栈的实现
 * @author liuxueping
 * @mail liuxueping@benguo.cn
 * Feb 13, 2014 9:26:49 AM
 */
class SimuStack<T>{
	private Deque<T> data = new ArrayDeque<T>();  
	  
    public void push(T element) {  
        data.addFirst(element);  
    }  
  
    public T pop() {  
        return data.removeFirst();  
    }  
  
    public T peek() {  
        return data.peekFirst();  
    }  
  
    public String toString() {  
        return data.toString();  
    }  
}