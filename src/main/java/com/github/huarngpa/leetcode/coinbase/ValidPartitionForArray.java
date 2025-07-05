package com.github.huarngpa.leetcode.coinbase;

import java.util.HashMap;
import java.util.Map;

/**
 * You are given a 0-indexed integer array nums. You have to partition the array into one or more
 * contiguous subarrays.
 *
 * <p>We call a partition of the array valid if each of the obtained subarrays satisfies one of the
 * following conditions:
 *
 * <p>The subarray consists of exactly 2, equal elements. For example, the subarray [2,2] is good.
 * The subarray consists of exactly 3, equal elements. For example, the subarray [4,4,4] is good.
 * The subarray consists of exactly 3 consecutive increasing elements, that is, the difference
 * between adjacent elements is 1. For example, the subarray [3,4,5] is good, but the subarray
 * [1,3,5] is not. Return true if the array has at least one valid partition. Otherwise, return
 * false.
 */
public class ValidPartitionForArray {

  Map<Integer, Boolean> memo = new HashMap<>();

  public boolean prefixIsValid(int[] nums, int i) {
    // Base case
    if (i < 0) {
      return true;
    }
    // Check memoized results
    if (memo.containsKey(i)) {
      return memo.get(i);
    }
    boolean result = false;
    // Check the three possibilities
    if (i > 0 && nums[i] == nums[i - 1]) {
      result |= prefixIsValid(nums, i - 2);
    }
    if (i > 1 && nums[i] == nums[i - 1] && nums[i - 1] == nums[i - 2]) {
      result |= prefixIsValid(nums, i - 3);
    }
    if (i > 1 && nums[i] == nums[i - 1] + 1 && nums[i - 1] == nums[i - 2] + 1) {
      result |= prefixIsValid(nums, i - 3);
    }
    return result;
  }

  public boolean validPartitionTopDown(int[] nums) {
    int n = nums.length;
    memo.put(-1, true);
    return prefixIsValid(nums, n - 1);
  }

  public boolean validPartitionBottomUp(int[] nums) {
    int n = nums.length;
    boolean[] dp = new boolean[n + 1];
    dp[0] = true;
    // Determine if the prefix array nums[0 ~ i] has a valid partition
    for (int i = 0; i < n; i++) {
      int dpIndex = i + 1;
      if (i > 0 && nums[i] == nums[i - 1]) {
        dp[dpIndex] |= dp[dpIndex - 2];
      }
      if (i > 1 && nums[i] == nums[i - 1] && nums[i - 1] == nums[i - 2]) {
        dp[dpIndex] |= dp[dpIndex - 3];
      }
      if (i > 1 && nums[i] == nums[i - 1] + 1 && nums[i - 1] == nums[i - 2] + 1) {
        dp[dpIndex] |= dp[dpIndex - 3];
      }
    }
    return dp[n];
  }
}
