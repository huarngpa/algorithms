package com.github.huarngpa.leetcode.coinbase;

import java.util.Random;

/**
 * Given an integer array nums with possible duplicates, randomly output the index of a given target
 * number. You can assume that the given target number must exist in the array.
 *
 * <p>Implement the Solution class:
 *
 * <p>Solution(int[] nums) Initializes the object with the array nums. int pick(int target) Picks a
 * random index i from nums where nums[i] == target. If there are multiple valid i's, then each
 * index should have an equal probability of returning.
 */
public class RandomPickIndex {

  private int[] nums;
  private Random rand = new Random();

  public RandomPickIndex(int[] nums) {
    this.nums = nums;
  }

  public int pick(int target) {
    int n = this.nums.length;
    int count = 0;
    int idx = 0;
    for (int i = 0; i < n; i++) {
      // if nums[i] is equal to target, i is a potential candidate which needs to be chosen
      // uniformly at random
      if (this.nums[i] == target) {
        count++;
        // we pick the current number with probability 1 / count (reservoir sampling)
        if (rand.nextInt(count) == 0) {
          idx = i;
        }
      }
    }
    return idx;
  }
}
