package com.github.huarngpa.clrs;

import java.util.*;

public class BreadthFirstSearch {

  public static void bfs(Map<Integer, List<Integer>> graph, int start) {
    Set<Integer> visited = new HashSet<>();
    Queue<Integer> queue = new LinkedList<>();

    visited.add(start);
    queue.offer(start);

    while (!queue.isEmpty()) {
      int vertex = queue.poll();
      System.out.println(vertex + " "); // Process vertex

      List<Integer> neighbors = graph.getOrDefault(vertex, new ArrayList<>());
      for (int neighbor : neighbors) {
        if (!visited.contains(neighbor)) {
          visited.add(neighbor);
          queue.offer(neighbor);
        }
      }
    }
  }
}
