package com.github.huarngpa.dcp.arrays;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class OtherElementsProductsTest {
  @Test
  void testStandardCase() {
    int[] input = {1, 2, 3, 4};
    int[] expected = {24, 12, 8, 6};
    assertArrayEquals(
        expected,
        OtherElementsProducts.products(input),
        "Should compute products of all elements except self");
  }

  @Test
  void testArrayWithZeros() {
    int[] input = {1, 0, 3, 4};
    int[] expected = {0, 12, 0, 0};
    assertArrayEquals(
        expected,
        OtherElementsProducts.products(input),
        "Should handle array with one zero correctly");
  }

  @Test
  void testMultipleZeros() {
    int[] input = {0, 0, 3, 4};
    int[] expected = {0, 0, 0, 0};
    assertArrayEquals(
        expected,
        OtherElementsProducts.products(input),
        "Should handle array with multiple zeros correctly");
  }

  @Test
  void testNegativeNumbers() {
    int[] input = {-1, 2, -3, 4};
    int[] expected = {-24, 12, -8, 6};
    assertArrayEquals(
        expected,
        OtherElementsProducts.products(input),
        "Should handle negative numbers correctly");
  }

  @Test
  void testAllOnes() {
    int[] input = {1, 1, 1, 1};
    int[] expected = {1, 1, 1, 1};
    assertArrayEquals(
        expected, OtherElementsProducts.products(input), "Should handle array of all ones");
  }

  @Disabled
  @Test
  void testSingleElement() {
    // Problem-specific: Often, single-element case returns [1] or throws an exception
    int[] input = {5};
    int[] expected = {1}; // Assuming problem expects [1] for single element
    assertArrayEquals(
        expected, OtherElementsProducts.products(input), "Should handle single-element array");
  }

  @Test
  void testLargeNumbers() {
    int[] input = {100, 200, 300};
    int[] expected = {60000, 30000, 20000};
    assertArrayEquals(
        expected, OtherElementsProducts.products(input), "Should handle large numbers correctly");
  }
}
