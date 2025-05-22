package com.github.huarngpa.clrs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KadanesAlgorithmTest {

  @Test
  void testCLRSFigure4_1() {
    // Test case from CLRS Figure 4.1
    double[] A = {13, -3, -25, 20, -3, -16, -23, 18, 20, -7, 12, -5, -22, 15, -4, 7};
    KadanesAlgorithm.Result result = KadanesAlgorithm.findMaximumSubarray(A);
    assertEquals(7, result.low, "Low index should be 7");
    assertEquals(10, result.high, "High index should be 10");
    assertEquals(43.0, result.sum, "Sum should be 43.0");
  }

  @Test
  void testCLRSFigure4_2() {
    // Test case from CLRS Figure 4.2
    double[] A = {1, -4, 3, -4};
    KadanesAlgorithm.Result result = KadanesAlgorithm.findMaximumSubarray(A);
    assertEquals(2, result.low, "Low index should be 2");
    assertEquals(2, result.high, "High index should be 2");
    assertEquals(3.0, result.sum, "Sum should be 3.0");
  }

  @Test
  void testSingleElementArray() {
    // Test case with a single element
    double[] A = {5.0};
    KadanesAlgorithm.Result result = KadanesAlgorithm.findMaximumSubarray(A);
    assertEquals(0, result.low, "Low index should be 0");
    assertEquals(0, result.high, "High index should be 0");
    assertEquals(5.0, result.sum, "Sum should be 5.0");
  }

  @Test
  void testAllNegativeNumbers() {
    // Test case with all negative numbers
    double[] A = {-2, -5, -1, -3, -4};
    KadanesAlgorithm.Result result = KadanesAlgorithm.findMaximumSubarray(A);
    assertEquals(2, result.low, "Low index should be 2");
    assertEquals(2, result.high, "High index should be 2");
    assertEquals(-1.0, result.sum, "Sum should be -1.0");
  }

  @Test
  void testAllPositiveNumbers() {
    // Test case with all positive numbers
    double[] A = {1, 2, 3, 4, 5};
    KadanesAlgorithm.Result result = KadanesAlgorithm.findMaximumSubarray(A);
    assertEquals(0, result.low, "Low index should be 0");
    assertEquals(4, result.high, "High index should be 4");
    assertEquals(15.0, result.sum, "Sum should be 15.0");
  }

  @Test
  void testEmptyArray() {
    // Test case with empty array should throw an exception
    double[] A = {};
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          KadanesAlgorithm.findMaximumSubarray(A);
        },
        "Should throw IllegalArgumentException for empty array");
  }

  @Test
  void testNullArray() {
    // Test case with null array should throw an exception
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          KadanesAlgorithm.findMaximumSubarray(null);
        },
        "Should throw IllegalArgumentException for null array");
  }
}
