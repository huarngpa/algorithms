package com.github.huarngpa.clrs;

import java.util.*;

public class TopologicalSort {

  public static List<Integer> topologicalSort(Map<Integer, List<Integer>> graph) {
    Set<Integer> visited = new HashSet<>();
    Stack<Integer> stack = new Stack<>();
    // Run DFS on all unvisited nodes
    for (int vertex : graph.keySet()) {
      if (!visited.contains(vertex)) {
        dfsTopological(graph, vertex, visited, stack);
      }
    }
    // Reverse stack to get topological order
    List<Integer> result = new ArrayList<>();
    while (!stack.isEmpty()) {
      result.add(stack.pop());
    }
    return result;
  }

  private static void dfsTopological(
      Map<Integer, List<Integer>> graph, int vertex, Set<Integer> visited, Stack<Integer> stack) {
    visited.add(vertex);
    List<Integer> neighbors = graph.getOrDefault(vertex, new ArrayList<>());
    for (int neighbor : neighbors) {
      if (!visited.contains(neighbor)) {
        dfsTopological(graph, neighbor, visited, stack);
      }
    }
    stack.push(vertex);
  }
}
