package com.github.huarngpa.interviewdb.q2;

public class ArrayMutation {
  /**
   * Likely Problem Type: Array manipulation, likely involving modifying elements based on a rule or
   * condition.Description: Based on the title and CodeSignal’s General Coding Assessment patterns,
   * this could involve transforming an array by applying operations (e.g., incrementing,
   * decrementing, or swapping elements) based on specific rules, possibly handling edge cases like
   * empty arrays or boundary elements. A typical Q2 (medium difficulty) might require iterating
   * through the array and applying a transformation while ensuring correctness for corner cases.
   * For example, a problem like “sum each element with its neighbors” requires handling the first
   * and last elements differently.
   */
  public static int[] sumNeighbors(int[] array) {
    int n = array.length;
    int[] result = new int[n];
    for (int i = 0; i < n; i++) {
      int left = i > 0 ? array[i - 1] : 0;
      int right = i < (n - 1) ? array[i + 1] : 0;
      result[i] = left + array[i] + right;
    }
    return result;
  }
}
