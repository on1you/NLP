package main.algorithm;

import java.util.Arrays;

/**
 * Merge Sorted and Remove Duplicates from Array 
 * @author liuxueping
 * @mail liuxueping@benguo.cn Feb 27, 2014 11:25:04 AM
 */
public class MergeSortedArrays {

	public static void main(String[] args) {
		int[] a = { 10, 10, 10, 25, 25, 54, 74, 78, 89, 347 };
		int[] b = { 1, 2, 3, 4, 5, 6, 7 };
		int m = a.length;
		a = Arrays.copyOf(a,m + b.length);
		System.out.println(Arrays.toString(a));
		merge(a, m, b, b.length);
		System.out.println(Arrays.toString(a));
		a = Arrays.copyOf(a, removeDuplicates(a));
		System.out.println(Arrays.toString(a));
	}

	public static double findMedianSortedArrays(int A[], int B[]) {
		int m = A.length;
		int n = B.length;

		if ((m + n) % 2 != 0) // odd
			return (double) findKth(A, B, (m + n) / 2, 0, m - 1, 0, n - 1);
		else { // even
			return (findKth(A, B, (m + n) / 2, 0, m - 1, 0, n - 1) + findKth(A, B, (m + n) / 2 - 1, 0, m - 1, 0, n - 1)) * 0.5;
		}
	}

	public static int findKth(int A[], int B[], int k, int aStart, int aEnd, int bStart, int bEnd) {

		int aLen = aEnd - aStart + 1;
		int bLen = bEnd - bStart + 1;

		// Handle special cases
		if (aLen == 0)
			return B[bStart + k];
		if (bLen == 0)
			return A[aStart + k];
		if (k == 0)
			return A[aStart] < B[bStart] ? A[aStart] : B[bStart];

		int aMid = aLen * k / (aLen + bLen); // a's middle count
		int bMid = k - aMid - 1; // b's middle count

		// make aMid and bMid to be array index
		aMid = aMid + aStart;
		bMid = bMid + bStart;

		if (A[aMid] > B[bMid]) {
			k = k - (bMid - bStart + 1);
			aEnd = aMid;
			bStart = bMid + 1;
		} else {
			k = k - (aMid - aStart + 1);
			bEnd = bMid;
			aStart = aMid + 1;
		}

		return findKth(A, B, k, aStart, aEnd, bStart, bEnd);
	}

	/**
	 * Remove Duplicates from Sorted Array
	 * 
	 * @param A
	 * @return
	 */
	public static int removeDuplicates(int[] A) {
		if (A.length < 2)
			return A.length;

		int j = 0;
		int i = 1;

		while (i < A.length) {
			if (A[i] == A[j]) {
				i++;
			} else {
				A[++j] = A[i++];
			}
		}

		return j + 1;
	}

	
	/**
	 * Merge Sorted Array 
	 */
	public static void merge(int A[], int m, int B[], int n) {
		int i = m - 1;
		int j = n - 1;
		int k = m + n - 1;
		
		while (k >= 0) {
			if (j < 0 || (i >= 0 && A[i] > B[j]))
				A[k--] = A[i--];
			else
				A[k--] = B[j--];
		}
	}

}
