package com.github.huarngpa.clrs;

import java.util.Random;

public class QuickSort {

  private static final Random random = new Random();

  // QuickSort
  public static void quickSort(int[] arr, int low, int high) {
    if (low < high) {
      int pi = randomizedPartition(arr, low, high);
      quickSort(arr, low, pi - 1);
      quickSort(arr, pi + 1, high);
    }
  }

  // QuickSelect
  public static int quickSelect(int[] arr, int low, int high, int k) {
    if (low == high) {
      return arr[low];
    }
    int pi = randomizedPartition(arr, low, high);
    int pivotIndex = pi - low + 1; // Rank of pivot
    if (k == pivotIndex) {
      return arr[pi];
    } else if (k < pivotIndex) {
      return quickSelect(arr, low, pi - 1, k);
    } else {
      return quickSelect(arr, pi + 1, high, k - pivotIndex);
    }
  }

  private static int randomizedPartition(int[] arr, int low, int high) {
    // Choose random pivot
    int pivotIndex = low + random.nextInt(high - low + 1);
    swap(arr, pivotIndex, high);
    return partition(arr, low, high);
  }

  private static int partition(int[] arr, int low, int high) {
    int pivot = arr[high];
    int i = low - 1; // Index of smaller element
    for (int j = low; j < high; j++) {
      if (arr[j] <= pivot) {
        i++;
        swap(arr, i, j);
      }
    }
    swap(arr, i + 1, high); // Place pivot
    return i + 1;
  }

  private static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }
}
