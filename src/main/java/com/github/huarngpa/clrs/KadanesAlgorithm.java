package com.github.huarngpa.clrs;

public class KadanesAlgorithm {

  // Class to store the result: low index, high index, and sum
  static class Result {
    int low;
    int high;
    double sum;

    Result(int low, int high, double sum) {
      this.low = low;
      this.high = high;
      this.sum = sum;
    }
  }

  /**
   * Finds the maximum subarray in the given array using Kadane's algorithm.
   *
   * <p><b>Problem Description</b>: Given an array of numbers representing daily stock price
   * changes, find the contiguous subarray with the largest sum, which corresponds to the maximum
   * profit from buying and selling a stock. This method uses Kadane's algorithm to achieve O(n)
   * time complexity.
   *
   * <p><b>Key Insights</b>: - Uses a single pass through the array, maintaining the maximum sum of
   * a subarray ending at each index. - At each index, decides whether to start a new subarray or
   * extend the current one. - If the current sum becomes negative, it’s discarded, as it won’t
   * contribute to a larger future sum. - Tracks start and end indices to return the subarray
   * boundaries. - Time complexity is O(n), space complexity is O(1).
   *
   * @param A the input array of price changes
   * @return a Result object containing the starting index, ending index, and sum of the maximum
   *     subarray
   * @throws IllegalArgumentException if the array is empty
   */
  public static Result findMaximumSubarray(double[] A) {
    if (A == null || A.length == 0) {
      throw new IllegalArgumentException("Array cannot be null or empty");
    }

    double currentSum = 0;
    double maxSum = Double.NEGATIVE_INFINITY;
    int maxStart = 0;
    int maxEnd = 0;
    int tempStart = 0;

    for (int i = 0; i < A.length; i++) {
      // Decide whether to start a new subarray or extend the current one
      if (currentSum + A[i] < A[i]) {
        currentSum = A[i];
        tempStart = i;
      } else {
        currentSum += A[i];
      }

      // Update the maximum sum and indices if the current sum is larger
      if (currentSum > maxSum) {
        maxSum = currentSum;
        maxStart = tempStart;
        maxEnd = i;
      }
    }

    return new Result(maxStart, maxEnd, maxSum);
  }
}
