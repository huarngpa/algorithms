package com.github.huarngpa.dcp.linkedlists;

public class ReverseLinkedList {

  public static class ListNode {
    int val;
    ListNode next;

    ListNode(int val) {
      this.val = val;
    }
  }

  private static class ListNodePair {
    ListNode head;
    ListNode tail;

    ListNodePair(ListNode head, ListNode tail) {
      this.head = head;
      this.tail = tail;
    }
  }

  // Solution that uses O(n) space, because of recursive stack
  public static ListNode reverseRecursively(ListNode node) {
    ListNodePair pair = reverseRecursivelyHelper(node);
    return pair.head;
  }

  private static ListNodePair reverseRecursivelyHelper(ListNode node) {
    if (node == null) {
      return new ListNodePair(null, null);
    }
    if (node.next == null) {
      return new ListNodePair(node, node);
    }
    // Reverse the rest of linked list and move node to after tail.
    ListNodePair pair = reverseRecursivelyHelper(node.next);
    ListNode head = pair.head;
    ListNode tail = pair.tail;
    node.next = null;
    tail.next = node;
    return new ListNodePair(head, node);
  }

  // A better solution, that uses constant space
  public static ListNode reverse(ListNode head) {
    ListNode prev = null;
    ListNode current = head;
    while (current != null) {
      // Make current node point to prev and move both forward one
      ListNode tmp = current.next;
      current.next = prev;
      prev = current;
      current = tmp;
    }
    return prev;
  }
}
