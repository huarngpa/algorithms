package com.github.huarngpa.leetcode.coinbase;

import java.util.List;

public class ZigzagIterator {

  int i = 0;
  int j = 0;
  int m;
  int n;
  List<Integer> v1;
  List<Integer> v2;

  public ZigzagIterator(List<Integer> v1, List<Integer> v2) {
    this.v1 = v1;
    this.v2 = v2;
    this.m = v1.size();
    this.n = v2.size();
  }

  public int next() {
    if (!hasNext()) {
      throw new IllegalStateException("Iterator does not have any more elements!");
    }
    if (i >= m) {
      return v2.get(j++);
    }
    if (j >= n) {
      return v1.get(i++);
    }
    if (i > j) {
      return v2.get(j++);
    } else {
      return v1.get(i++);
    }
  }

  public boolean hasNext() {
    return !(i >= m && j >= n);
  }
}
