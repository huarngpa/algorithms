package com.github.huarngpa.dcp.arrays;

public class OtherElementsProducts {

  // Without division approach
  public static int[] products(int[] array) {
    if (array == null || array.length == 0) {
      return array;
    }

    int size = array.length;

    // Generate prefix products
    int[] prefixes = new int[size];
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        prefixes[i] = array[i] * prefixes[i - 1];
      } else {
        prefixes[i] = array[i];
      }
    }

    // Generate suffix products
    int[] suffixes = new int[size];
    int suffixStart = size - 1;
    for (int i = suffixStart; i >= 0; i--) {
      if (i < suffixStart) {
        suffixes[i] = array[i] * suffixes[i + 1];
      } else {
        suffixes[i] = array[i];
      }
    }

    int[] result = new int[size];
    for (int i = 0; i < size; i++) {
      int prefixIdx = i - 1;
      int suffixIdx = i + 1;
      if (prefixIdx < 0) {
        result[i] = suffixes[suffixIdx];
      } else if (suffixIdx >= size) {
        result[i] = prefixes[prefixIdx];
      } else {
        result[i] = suffixes[suffixIdx] * prefixes[prefixIdx];
      }
    }

    return result;
  }
}
