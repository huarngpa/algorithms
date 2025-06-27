package com.github.huarngpa.leetcode.coinbase;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 981. Time Based Key-Value Store Design a time-based key-value data structure that can store
 * multiple values for the same key at different time stamps and retrieve the key's value at a
 * certain timestamp. Implement the TimeMap class: TimeMap() Initializes the object of the data
 * structure. void set(String key, String value, int timestamp) Stores the key key with the value
 * value at the given time timestamp. String get(String key, int timestamp) Returns a value such
 * that set was called previously, with timestamp_prev <= timestamp. If there are multiple such
 * values, it returns the value associated with the largest timestamp_prev. If there are no values,
 * it returns "".
 */
public class TimeMap {

  private final Map<String, TreeMap<Integer, String>> map;

  public TimeMap() {
    map = new HashMap<>();
  }

  public void set(String key, String value, int timestamp) {
    // Get or create TreeMap for the key
    map.computeIfAbsent(key, k -> new TreeMap<>()).put(timestamp, value);
  }

  public String get(String key, int timestamp) {
    // Check if the key exists
    TreeMap<Integer, String> treeMap = map.get(key);
    if (treeMap == null) {
      return "";
    }
    // Find largest timestamp <= given timestamp
    Integer floorKey = treeMap.floorKey(timestamp);
    if (floorKey == null) {
      return "";
    }
    return treeMap.get(floorKey);
  }

  // Extend the TimeMap class to support a delete(String key, int timestamp) method that removes the
  // value associated with the given key and timestamp. Ensure subsequent get operations reflect the
  // deletion. If the timestamp doesn’t exist, do nothing. How would you handle deleting all values
  // for a key up to a given timestamp?
  public void delete(String key, int timestamp) {
    TreeMap<Integer, String> treeMap = map.get(key);
    if (treeMap != null) {
      treeMap.remove(timestamp);
      if (treeMap.isEmpty()) {
        map.remove(key); // Optional bit of clean up, this is nice
      }
    }
  }

  public void deleteUpTo(String Key, int timestamp) {
    TreeMap<Integer, String> treeMap = map.get(Key);
    if (treeMap != null) {
      // Remove all entries with timestamps <= timestamp
      NavigableMap<Integer, String> toRemove = treeMap.headMap(timestamp, true);
      toRemove.clear();
      if (treeMap.isEmpty()) {
        map.remove(Key);
      }
    }
  }
}
