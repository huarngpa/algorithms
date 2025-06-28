package com.github.huarngpa.leetcode.coinbase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
public class TimeMapEnhanced {

  private final Map<String, TreeMap<Integer, String>> map;

  public TimeMapEnhanced() {
    map = new ConcurrentHashMap<>();
  }

  public void set(String key, String value, int timestamp) {
    // Get or create TreeMap for the key
    // We can use `compute` instead of `put` to do more interesting things
    map.computeIfAbsent(key, k -> new TreeMap<>()).compute(timestamp, (k, v) -> value);
  }

  public String get(String key, int timestamp) {
    TreeMap<Integer, String> treeMap = map.get(key);
    if (treeMap == null) {
      return "";
    }
    // Note the `synchronized` here, discuss implications and trade-off or alternatives: e.g.
    // ConcurrentSkipListMap
    synchronized (treeMap) {
      Integer floorKey = treeMap.floorKey(timestamp);
      if (floorKey == null) {
        return "";
      }
      return treeMap.get(floorKey);
    }
  }

  // Extend the TimeMap class to support a delete(String key, int timestamp) method that removes the
  // value associated with the given key and timestamp. Ensure subsequent get operations reflect the
  // deletion. If the timestamp doesn’t exist, do nothing. How would you handle deleting all values
  // for a key up to a given timestamp?
  public void delete(String key, int timestamp) {
    TreeMap<Integer, String> treeMap = map.get(key);
    if (treeMap != null) {
      synchronized (treeMap) {
        treeMap.remove(timestamp);
        if (treeMap.isEmpty()) {
          map.remove(key); // Optional bit of clean up, this is nice
        }
      }
    }
  }

  public void deleteUpTo(String key, int timestamp) {
    TreeMap<Integer, String> treeMap = map.get(key);
    if (treeMap != null) {
      // Remove all entries with timestamps <= timestamp
      NavigableMap<Integer, String> toRemove = treeMap.headMap(timestamp, true);
      toRemove.clear();
      if (treeMap.isEmpty()) {
        map.remove(key);
      }
    }
  }

  // Extend the TimeMap class to support a getRange(String key, int startTimestamp, int
  // endTimestamp) method that returns all values for a key with timestamps in [startTimestamp,
  // endTimestamp]. Additionally, implement a prune(int maxAge) method to remove all entries with
  // timestamps older than maxAge across all keys to manage memory. How would you optimize these for
  // high-frequency calls?
  public List<String> getRange(String key, int startTimestamp, int endTimestamp) {
    TreeMap<Integer, String> treeMap = map.get(key);
    if (treeMap != null) {
      synchronized (treeMap) {
        NavigableMap<Integer, String> range =
            treeMap.subMap(startTimestamp, true, endTimestamp, true);
        return range.values().stream().toList();
      }
    }
    return Collections.emptyList();
  }

  public void prune(int maxAge) {
    for (String key : map.keySet()) {
      TreeMap<Integer, String> treeMap = map.get(key);
      if (treeMap != null) {
        synchronized (treeMap) {
          NavigableMap<Integer, String> toRemove = treeMap.headMap(maxAge, false);
          toRemove.clear();
          if (treeMap.isEmpty()) {
            map.remove(key);
          }
        }
      }
    }
  }
}
