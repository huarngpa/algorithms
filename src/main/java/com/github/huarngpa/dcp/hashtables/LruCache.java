package com.github.huarngpa.dcp.hashtables;

import java.util.HashMap;
import java.util.Map;

public class LruCache {
  public static class LinkedList {

    public static class ListNode {

      String key;
      Integer val;
      ListNode prev;
      ListNode next;

      public ListNode(String key, Integer val) {
        this.key = key;
        this.val = val;
        this.prev = null;
        this.next = null;
      }
    }

    ListNode head;
    ListNode tail;

    public LinkedList() {
      head = new ListNode(null, null);
      tail = new ListNode(null, null);
      head.next = tail;
      tail.prev = head;
    }

    public ListNode getHead() {
      return head.next;
    }

    public ListNode getTail() {
      return tail.prev;
    }

    public void add(ListNode node) {
      ListNode prev = tail.prev;
      prev.next = node;
      node.prev = prev;
      node.next = tail;
      tail.prev = node;
    }

    public void remove(ListNode node) {
      ListNode prev = node.prev;
      ListNode next = node.next;
      prev.next = next;
      next.prev = prev;
    }
  }

  int n;
  Map<String, LinkedList.ListNode> map = new HashMap<>();
  LinkedList list;

  public LruCache(int n) {
    this.n = n;
    list = new LinkedList();
  }

  // Interesting, the trick to this problem is about the storage of node in the map and the
  // exploitation of object refs and list pointers
  public void set(String key, Integer val) {
    if (map.containsKey(key)) {
      LinkedList.ListNode node = map.remove(key);
      list.remove(node);
    }
    LinkedList.ListNode node = new LinkedList.ListNode(key, val);
    list.add(node);
    map.put(key, node);
    if (map.size() > n) {
      LinkedList.ListNode head = list.getHead();
      list.remove(head);
      map.remove(head.key);
    }
  }

  public Integer get(String key) {
    LinkedList.ListNode node = map.get(key);
    if (node == null) {
      return null;
    }
    list.remove(node);
    list.add(node);
    return node.val;
  }
}
