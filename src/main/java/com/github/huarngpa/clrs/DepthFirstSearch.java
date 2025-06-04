package com.github.huarngpa.clrs;

import java.util.*;

public class DepthFirstSearch {
  public static void dfs(Map<Integer, List<Integer>> graph, int start) {
    Set<Integer> visited = new HashSet<>();
    dfsRecursive(graph, start, visited);
  }

  private static void dfsRecursive(
      Map<Integer, List<Integer>> graph, int vertex, Set<Integer> visited) {
    visited.add(vertex);
    List<Integer> neighbors = graph.getOrDefault(vertex, new ArrayList<>());
    for (int neighbor : neighbors) {
      if (!visited.contains(neighbor)) {
        dfsRecursive(graph, neighbor, visited);
      }
    }
  }
}
