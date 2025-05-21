package com.github.huarngpa.clrs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MaximumSubarrayTest {

  // Helper method to create a Result object for expected values
  private MaximumSubarray.Result createResult(int low, int high, double sum) {
    return new MaximumSubarray.Result(low, high, sum);
  }

  @Test
  void testCLRSFigure4_1() {
    // Test case from CLRS Figure 4.1
    double[] A = {13, -3, -25, 20, -3, -16, -23, 18, 20, -7, 12, -5, -22, 15, -4, 7};
    MaximumSubarray.Result result = MaximumSubarray.findMaximumSubarray(A, 0, A.length - 1);
    assertEquals(7, result.low, "Low index should be 7");
    assertEquals(10, result.high, "High index should be 10");
    assertEquals(43.0, result.sum, "Sum should be 43.0");
  }

  @Test
  void testCLRSFigure4_2() {
    // Test case from CLRS Figure 4.2
    double[] A = {1, -4, 3, -4};
    MaximumSubarray.Result result = MaximumSubarray.findMaximumSubarray(A, 0, A.length - 1);
    assertEquals(2, result.low, "Low index should be 2");
    assertEquals(2, result.high, "High index should be 2");
    assertEquals(3.0, result.sum, "Sum should be 3.0");
  }

  @Test
  void testSingleElementArray() {
    // Test case with a single element
    double[] A = {5.0};
    MaximumSubarray.Result result = MaximumSubarray.findMaximumSubarray(A, 0, A.length - 1);
    assertEquals(0, result.low, "Low index should be 0");
    assertEquals(0, result.high, "High index should be 0");
    assertEquals(5.0, result.sum, "Sum should be 5.0");
  }

  @Test
  void testAllNegativeNumbers() {
    // Test case with all negative numbers
    double[] A = {-2, -5, -1, -3, -4};
    MaximumSubarray.Result result = MaximumSubarray.findMaximumSubarray(A, 0, A.length - 1);
    assertEquals(2, result.low, "Low index should be 2");
    assertEquals(2, result.high, "High index should be 2");
    assertEquals(-1.0, result.sum, "Sum should be -1.0");
  }

  @Test
  void testAllPositiveNumbers() {
    // Test case with all positive numbers
    double[] A = {1, 2, 3, 4, 5};
    MaximumSubarray.Result result = MaximumSubarray.findMaximumSubarray(A, 0, A.length - 1);
    assertEquals(0, result.low, "Low index should be 0");
    assertEquals(4, result.high, "High index should be 4");
    assertEquals(15.0, result.sum, "Sum should be 15.0");
  }

  @Test
  void testArrayWithZeros() {
    // Test case with zeros and mixed numbers
    double[] A = {-2, 0, 3, -1, 0, 2};
    MaximumSubarray.Result result = MaximumSubarray.findMaximumSubarray(A, 0, A.length - 1);
    assertEquals(2, result.low, "Low index should be 2");
    assertEquals(5, result.high, "High index should be 5");
    assertEquals(4.0, result.sum, "Sum should be 4.0");
  }
}
