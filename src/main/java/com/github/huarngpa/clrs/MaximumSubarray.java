package com.github.huarngpa.clrs;

public class MaximumSubarray {

  // Class to store the result: low index, high index, and sum
  static class Result {
    int low;
    int high;
    double sum; // Using double to handle potential negative and large numbers

    Result(int low, int high, double sum) {
      this.low = low;
      this.high = high;
      this.sum = sum;
    }
  }

  /**
   * Finds the maximum subarray that crosses the midpoint of the given array.
   *
   * <p><b>Problem Description</b>: Given an array of numbers representing daily stock price
   * changes, this method finds the maximum subarray that spans the midpoint, i.e., includes
   * elements from both A[low..mid] and A[mid+1..high]. The goal is to maximize the sum of the
   * subarray, which corresponds to the maximum profit from buying and selling a stock across the
   * midpoint.
   *
   * <p><b>Key Insights</b>: - The maximum crossing subarray must include the last element of the
   * left half (A[mid]) and the first element of the right half (A[mid+1]). - We scan left from mid
   * to low to find the maximum sum of a subarray ending at mid. - We scan right from mid+1 to high
   * to find the maximum sum of a subarray starting at mid+1. - The total sum is the combination of
   * the maximum left and right subarrays. - Time complexity is O(n), where n = high - low + 1, due
   * to two linear scans.
   *
   * @param A the input array of price changes
   * @param low the starting index of the subarray
   * @param mid the midpoint index
   * @param high the ending index of the subarray
   * @return a Result object containing the starting index (maxLeft), ending index (maxRight), and
   *     the sum of the maximum crossing subarray
   */
  public static Result findMaxCrossingSubarray(double[] A, int low, int mid, int high) {
    // Initialize left sum to negative infinity
    double leftSum = Double.NEGATIVE_INFINITY;
    double sum = 0;
    int maxLeft = mid;

    // Scan left half from mid to low
    for (int i = mid; i >= low; i--) {
      sum += A[i];
      if (sum > leftSum) {
        leftSum = sum;
        maxLeft = i;
      }
    }

    // Initialize right sum to negative infinity
    double rightSum = Double.NEGATIVE_INFINITY;
    sum = 0;
    int maxRight = mid + 1;

    // Scan right half from mid+1 to high
    for (int j = mid + 1; j <= high; j++) {
      sum += A[j];
      if (sum > rightSum) {
        rightSum = sum;
        maxRight = j;
      }
    }

    // Return the indices and the sum of the crossing subarray
    return new Result(maxLeft, maxRight, leftSum + rightSum);
  }

  /**
   * Finds the maximum subarray in the given array using a divide-and-conquer approach.
   *
   * <p><b>Problem Description</b>: Given an array of numbers representing daily stock price
   * changes, find the contiguous subarray with the largest sum, which corresponds to the maximum
   * profit from buying and selling a stock. The subarray represents the price changes between the
   * buy and sell days. This method uses divide-and-conquer to achieve O(n log n) time complexity.
   *
   * <p><b>Key Insights</b>: - The maximum subarray must lie entirely in the left half
   * (A[low..mid]), entirely in the right half (A[mid+1..high]), or cross the midpoint. - The
   * problem is divided into two recursive subproblems (left and right halves). - The crossing
   * subarray is handled by findMaxCrossingSubarray. - The base case (single element) is trivial,
   * returning the element itself. - The algorithm compares the sums of the left, right, and
   * crossing subarrays to return the maximum. - Time complexity is O(n log n) due to the recurrence
   * T(n) = 2T(n/2) + O(n).
   *
   * @param A the input array of price changes
   * @param low the starting index of the subarray
   * @param high the ending index of the subarray
   * @return a Result object containing the starting index, ending index, and sum of the maximum
   *     subarray
   */
  public static Result findMaximumSubarray(double[] A, int low, int high) {
    // Base case: single element
    if (low == high) {
      return new Result(low, high, A[low]);
    }

    // Divide: compute the midpoint
    int mid = (low + high) / 2;

    // Conquer: recursively find maximum subarrays in left and right halves
    Result leftResult = findMaximumSubarray(A, low, mid);
    Result rightResult = findMaximumSubarray(A, mid + 1, high);
    Result crossResult = findMaxCrossingSubarray(A, low, mid, high);

    // Combine: return the maximum of the three subarrays
    if (leftResult.sum >= rightResult.sum && leftResult.sum >= crossResult.sum) {
      return leftResult;
    } else if (rightResult.sum >= leftResult.sum && rightResult.sum >= crossResult.sum) {
      return rightResult;
    } else {
      return crossResult;
    }
  }
}
