package com.github.huarngpa.leetcode.coinbase;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * You are given a nested list of integers nestedList. Each element is either an integer or a list
 * whose elements may also be integers or other lists. Implement an iterator to flatten it.
 *
 * <p>Implement the NestedIterator class:
 *
 * <p>NestedIterator(List<NestedInteger> nestedList) Initializes the iterator with the nested list
 * nestedList. int next() Returns the next integer in the nested list. boolean hasNext() Returns
 * true if there are still some integers in the nested list and false otherwise. Your code will be
 * tested with the following pseudocode:
 *
 * <p>initialize iterator with nestedList res = [] while iterator.hasNext() append iterator.next()
 * to the end of res return res If res matches the expected flattened list, then your code will be
 * judged as correct.
 */
public class NestedIterator implements Iterator<Integer> {

  interface NestedInteger {
    boolean isInteger();

    Integer getInteger();

    List<NestedInteger> getList();
  }

  private Stack<NestedInteger> stack = new Stack<>();

  public NestedIterator(List<NestedInteger> nestedList) {
    // Push elements in reverse order into the stack
    for (int i = nestedList.size() - 1; i >= 0; i--) {
      stack.push(nestedList.get(i));
    }
  }

  @Override
  public boolean hasNext() {
    // Process stack until top is an integer or stack is empty
    while (!stack.isEmpty() && !stack.peek().isInteger()) {
      NestedInteger top = stack.pop();
      // Push list elements in reverse order
      for (int i = top.getList().size() - 1; i >= 0; i--) {
        stack.push(top.getList().get(i));
      }
    }
    return !stack.isEmpty();
  }

  @Override
  public Integer next() {
    // Assumes hasNext() is `true`
    return stack.pop().getInteger();
  }
}
