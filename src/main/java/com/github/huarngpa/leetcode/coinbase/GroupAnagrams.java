package com.github.huarngpa.leetcode.coinbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GroupAnagrams {
  /**
   * Given an array of strings strs, group the anagrams together. You can return the answer in any
   * order.
   */
  public List<List<String>> groupAnagrams(String[] strs) {
    HashMap<String, List<String>> map = new HashMap<>();
    for (String str : strs) {
      char[] arr = str.toCharArray();
      Arrays.sort(arr);
      String sorted = new String(arr);
      map.computeIfAbsent(sorted, ignored -> new ArrayList<>()).add(str);
    }
    return new ArrayList<>(map.values());
  }
}
