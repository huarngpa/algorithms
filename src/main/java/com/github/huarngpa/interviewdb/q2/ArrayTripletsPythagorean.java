package com.github.huarngpa.interviewdb.q2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArrayTripletsPythagorean {
  /**
   * Likely Problem Type: Array traversal, mathematical computation.Description: This problem likely
   * asks for finding triplets (a, b, c) in an array where a² + b² = c², or generating all
   * Pythagorean triplets up to a given limit. A GeeksforGeeks post describes generating Pythagorean
   * triplets efficiently using the formula: a = m² - n², b = 2mn, c = m² + n², where m > n > 0. For
   * a Q2 problem, you might need to check if such triplets exist in a given array or generate them
   * within constraints.
   */
  public static int[][] pythagoreanTriplets(int[] array) {
      if (array == null || array.length < 3) {
          return new int[0][];
      }
    Arrays.sort(array);
    // Cache squared value to value
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < array.length; i++) {
      map.put(array[i] * array[i], i);
    }
    // Two pointer technique
    Map<String, int[]> triplets = new HashMap<>();
    for (int i = 0; i < array.length - 1; i++) {
      for (int j = i + 1; j < array.length; j++) {
        int a = array[i];
        int b = array[j];
        Integer c = map.get((a * a) + (b * b));
        if (c != null) {
          triplets.put(String.format("%d-%d-%d", a, b, c), new int[] {a, b, c});
        }
      }
    }
    // Output the results
    int[][] results = new int[triplets.size()][3];
    int i = 0;
    for (int[] triplet : triplets.values()) {
      results[i] = triplet;
      i++;
    }
    return results;
  }
}
