package com.github.huarngpa.leetcode.coinbase;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

  // Extended, see `update` method
  private List<NestedInteger> nestedList; // Store original list
  private int processedIndex = 0;

  // Extended, see `stream` method
  private final Object lock = new Object();

  public NestedIterator(List<NestedInteger> nestedList) {
    // Push elements in reverse order into the stack
    for (int i = nestedList.size() - 1; i >= 0; i--) {
      stack.push(nestedList.get(i));
    }
    // Extended
    this.nestedList = nestedList;
  }

  @Override
  public boolean hasNext() {
    synchronized (lock) {
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
  }

  @Override
  public Integer next() {
    synchronized (lock) {
      // Extended
      processedIndex++;
      // Assumes hasNext() is `true`
      return stack.pop().getInteger();
    }
  }

  /**
   * Extend the NestedIterator to support an update(List<NestedInteger> newList, int index) method
   * that replaces the nested list at a given index in the original input list. Ensure subsequent
   * hasNext and next calls reflect the updated list. For example, if the original list is
   * [[1,1],2], calling update([3,4], 0) changes it to [[3,4],2], and the iterator should yield
   * 3,4,2. How would you handle frequent updates efficiently?
   */
  public void update(List<NestedInteger> nestedList, int index) {
    if (index < 0 || index >= nestedList.size()) {
      throw new IndexOutOfBoundsException();
    }
    nestedList.set(
        index,
        new NestedInteger() {
          @Override
          public boolean isInteger() {
            return false;
          }

          @Override
          public Integer getInteger() {
            return null;
          }

          @Override
          public List<NestedInteger> getList() {
            return nestedList;
          }
        });
    // Rebuild stack for unprocessed elements
    if (index >= processedIndex) {
      stack.clear();
      for (int i = nestedList.size() - 1; i >= processedIndex; i--) {
        stack.push(nestedList.get(i));
      }
    }
  }

  /**
   * Make the NestedIterator thread-safe for concurrent access by multiple threads calling hasNext
   * and next. Add a stream() method that returns an Iterator<Integer> for asynchronous streaming,
   * simulating a blockchain data stream. How would you ensure thread safety and handle large
   * datasets efficiently?
   */
  public Iterator<Integer> stream() {
    // This is a bit unnecessary, don't bother studying this part!
    BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(100);
    new Thread(
            () -> {
              while (hasNext()) {
                try {
                  queue.put(next());
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
            })
        .start();
    return new Iterator<Integer>() {

      @Override
      public boolean hasNext() {
        return !queue.isEmpty();
      }

      @Override
      public Integer next() {
        return queue.poll();
      }
    };
  }
}
