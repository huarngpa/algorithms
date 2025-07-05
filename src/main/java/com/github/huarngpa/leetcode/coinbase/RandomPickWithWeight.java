package com.github.huarngpa.leetcode.coinbase;

/**
 * You are given a 0-indexed array of positive integers w where w[i] describes the weight of the ith
 * index.
 *
 * <p>You need to implement the function pickIndex(), which randomly picks an index in the range [0,
 * w.length - 1] (inclusive) and returns it. The probability of picking an index i is w[i] / sum(w).
 *
 * <p>For example, if w = [1, 3], the probability of picking index 0 is 1 / (1 + 3) = 0.25 (i.e.,
 * 25%), and the probability of picking index 1 is 3 / (1 + 3) = 0.75 (i.e., 75%).
 */
public class RandomPickWithWeight {

  private int[] prefixSums;
  private int totalSum;

  // The key to cracking this problem is prefix sums
  RandomPickWithWeight(int[] weights) {
    int sum = 0;
    prefixSums = new int[weights.length];
    for (int i = 0; i < weights.length; i++) {
      sum += weights[i];
      prefixSums[i] = sum;
    }
    totalSum = sum;
  }

  // Then using a modified binary search to find the right zone
  public int pickIndex() {
    double target = this.totalSum * Math.random();
    // run a binary search to find the target zone
    int low = 0, high = this.prefixSums.length;
    while (low < high) {
      // better to avoid the overflow
      int mid = low + (high - low) / 2;
      if (target > this.prefixSums[mid]) low = mid + 1;
      else high = mid;
    }
    return low;
  }
}
