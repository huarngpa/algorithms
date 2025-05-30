package com.github.huarngpa.interviewdb.q2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ArrayTripletsPythagoreanTest {

  @Test
  void testBasicCase() {
    int[] input = {3, 4, 5, 6, 8, 10};
    int[][] expected = {{3, 4, 5}, {6, 8, 10}};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Basic case with two triplets failed");
  }

  @Test
  void testNoTriplets() {
    int[] input = {1, 2, 3, 4};
    int[][] expected = {};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "No triplets case failed");
  }

  @Test
  void testMultipleTripletsWithDuplicates() {
    int[] input = {3, 4, 5, 3, 4, 5, 12, 13};
    int[][] expected = {{3, 4, 5}, {5, 12, 13}};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Multiple triplets with duplicates failed");
  }

  @Test
  void testNullArray() {
    int[] input = null;
    int[][] expected = {};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Null array case failed");
  }

  @Test
  void testEmptyArray() {
    int[] input = {};
    int[][] expected = {};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Empty array case failed");
  }

  @Test
  void testSmallArray() {
    int[] input = {1, 2};
    int[][] expected = {};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Small array case failed");
  }

  @Disabled
  @Test
  void testNegativeAndZeroValues() {
    int[] input = {-3, 0, 4, 5, -6};
    int[][] expected = {{3, 4, 5}}; // Assuming negatives/zeros are filtered
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Negative and zero values case failed");
  }

  @Test
  void testSingleTriplet() {
    int[] input = {5, 12, 13};
    int[][] expected = {{5, 12, 13}};
    int[][] result = ArrayTripletsPythagorean.pythagoreanTriplets(input);
    assertTrue(arraysEqual(result, expected), "Single triplet case failed");
  }

  /**
   * Helper method to compare two 2D arrays, ignoring order of triplets. Sorts arrays by their
   * elements to ensure consistent comparison.
   */
  private boolean arraysEqual(int[][] arr1, int[][] arr2) {
    if (arr1.length != arr2.length) return false;

    // Sort triplets for comparison
    int[][] sorted1 =
        Arrays.stream(arr1)
            .map(int[]::clone)
            .sorted(Comparator.comparingInt(a -> a[0] * 10000 + a[1] * 100 + a[2]))
            .toArray(int[][]::new);
    int[][] sorted2 =
        Arrays.stream(arr2)
            .map(int[]::clone)
            .sorted(Comparator.comparingInt(a -> a[0] * 10000 + a[1] * 100 + a[2]))
            .toArray(int[][]::new);

    return Arrays.deepEquals(sorted1, sorted2);
  }
}
