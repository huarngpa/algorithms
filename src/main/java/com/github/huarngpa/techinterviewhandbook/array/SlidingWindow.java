package com.github.huarngpa.techinterviewhandbook.array;

public class SlidingWindow {
  /** Find the maximum sum subarray of size `k` */
  public static int maxSumSubArray(int[] arr, int k) {
    int start = 0;
    int currentSum = 0;
    int maxSum = Integer.MIN_VALUE;
    for (int end = 0; end < arr.length; end++) {
      currentSum += arr[end];
      if (end - start + 1 == k) {
        maxSum = Math.max(maxSum, currentSum);
        currentSum -= arr[start];
        start++;
      }
    }
    return maxSum;
  }
}
