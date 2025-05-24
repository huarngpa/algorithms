package com.github.huarngpa.dcp.linkedlists;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReverseLinkedListTest {
  // Helper method to create a linked list from an array
  private ReverseLinkedList.ListNode createList(int[] values) {
    if (values == null || values.length == 0) {
      return null;
    }
    ReverseLinkedList.ListNode dummy = new ReverseLinkedList.ListNode(0);
    ReverseLinkedList.ListNode current = dummy;
    for (int val : values) {
      current.next = new ReverseLinkedList.ListNode(val);
      current = current.next;
    }
    return dummy.next;
  }

  // Helper method to convert a linked list to an array for comparison
  private int[] listToArray(ReverseLinkedList.ListNode head) {
    int length = 0;
    ReverseLinkedList.ListNode current = head;
    while (current != null) {
      length++;
      current = current.next;
    }
    int[] result = new int[length];
    current = head;
    for (int i = 0; i < length; i++) {
      result[i] = current.val;
      current = current.next;
    }
    return result;
  }

  // Helper method to test both iterative and recursive solutions
  private void testReverse(int[] input, int[] expected, String message) {
    // Test iterative solution
    ReverseLinkedList.ListNode headIterative = createList(input);
    headIterative = ReverseLinkedList.reverse(headIterative);
    assertArrayEquals(expected, listToArray(headIterative), message + " (Iterative)");

    // Test recursive solution
    ReverseLinkedList.ListNode headRecursive = createList(input);
    headRecursive = ReverseLinkedList.reverseRecursively(headRecursive);
    assertArrayEquals(expected, listToArray(headRecursive), message + " (Recursive)");
  }

  @Test
  void testEmptyList() {
    testReverse(new int[] {}, new int[] {}, "Empty list should remain empty");
  }

  @Test
  void testSingleNode() {
    testReverse(new int[] {1}, new int[] {1}, "Single node should remain unchanged");
  }

  @Test
  void testTwoNodes() {
    testReverse(new int[] {1, 2}, new int[] {2, 1}, "Two nodes should be reversed");
  }

  @Test
  void testMultipleNodes() {
    testReverse(
        new int[] {1, 2, 3, 4, 5}, new int[] {5, 4, 3, 2, 1}, "Multiple nodes should be reversed");
  }

  @Test
  void testAllIdenticalValues() {
    testReverse(
        new int[] {2, 2, 2}, new int[] {2, 2, 2}, "List with identical values should be reversed");
  }

  @Test
  void testNegativeValues() {
    testReverse(
        new int[] {-1, 0, -2, 3},
        new int[] {3, -2, 0, -1},
        "List with negative values should be reversed");
  }

  @Test
  void testLongList() {
    int[] input = new int[100];
    int[] expected = new int[100];
    for (int i = 0; i < 100; i++) {
      input[i] = i + 1;
      expected[99 - i] = i + 1;
    }
    testReverse(input, expected, "Long list should be reversed");
  }
}
