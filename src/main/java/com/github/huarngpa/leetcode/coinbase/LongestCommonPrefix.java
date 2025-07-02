package com.github.huarngpa.leetcode.coinbase;

import java.util.HashSet;

public class LongestCommonPrefix {
  /**
   * You are given two arrays with positive integers arr1 and arr2.
   *
   * <p>A prefix of a positive integer is an integer formed by one or more of its digits, starting
   * from its leftmost digit. For example, 123 is a prefix of the integer 12345, while 234 is not.
   *
   * <p>A common prefix of two integers a and b is an integer c, such that c is a prefix of both a
   * and b. For example, 5655359 and 56554 have common prefixes 565 and 5655 while 1223 and 43456 do
   * not have a common prefix.
   *
   * <p>You need to find the length of the longest common prefix between all pairs of integers (x,
   * y) such that x belongs to arr1 and y belongs to arr2.
   *
   * <p>Return the length of the longest common prefix among all pairs. If no common prefix exists
   * among them, return 0.
   */
  public int longestCommonPrefix(int[] left, int[] right) {
    // Store and build all prefixes from left
    HashSet<Integer> leftPrefixes = new HashSet<>();
    for (int val : left) {
      while (!leftPrefixes.contains(val) && val > 0) {
        leftPrefixes.add(val);
        // Generate the next shorter prefix
        val /= 10;
      }
    }
    // Calculate longest prefix
    int longestPrefix = 0;
    for (int val : right) {
      while (!leftPrefixes.contains(val) && val > 0) {
        // Reduce val by removing last digit
        val /= 10;
      }
      if (val > 0) {
        longestPrefix = Math.max(longestPrefix, (int) Math.log10(val) + 1);
      }
    }
    return longestPrefix;
  }
}
