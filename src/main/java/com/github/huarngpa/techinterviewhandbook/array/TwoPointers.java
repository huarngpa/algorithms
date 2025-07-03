package com.github.huarngpa.techinterviewhandbook.array;

public class TwoPointers {
  /** Given a sorted array, find a pair of elements that sum up to a given number */
  public static int[] twoSumSorted(int[] arr, int target) {
    int start = 0;
    int end = arr.length - 1;
    while (start < end) {
      int currentSum = arr[start] + arr[end];
      if (currentSum == target) {
        return new int[] {start, end};
      } else if (currentSum < target) {
        start++;
      } else {
        end--;
      }
    }
    throw new RuntimeException("Not implemented");
  }
}
