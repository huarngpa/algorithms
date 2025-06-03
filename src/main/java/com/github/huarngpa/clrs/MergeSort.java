package com.github.huarngpa.clrs;

public class MergeSort {

  public static void mergeSort(int[] arr, int left, int right) {
    // Todo
    if (left < right) {
      int mid = left + (right - left) / 2;
      // Recursively sort left-half
      mergeSort(arr, left, mid);
      // Recursively sort right-half
      mergeSort(arr, mid + 1, right);
      // Merge sorted halves
      merge(arr, left, mid, right);
    }
  }

  public static void merge(int[] arr, int left, int mid, int right) {
    // Sizes of the two subarrays
    int m = mid - left + 1;
    int n = right - mid;
    // Temporary arrays
    int[] leftArr = new int[m];
    int[] rightArr = new int[n];
    // Copy data to temp arrays
    for (int i = 0; i < m; i++) {
      leftArr[i] = arr[left + i];
    }
    for (int j = 0; j < n; j++) {
      rightArr[j] = arr[mid + 1 + j];
    }
    // Merge temp arrays back into arr
    int i = 0, j = 0, k = left;
    while (i < m && j < n) {
      if (leftArr[i] < rightArr[j]) {
        arr[k++] = leftArr[i++];
      } else {
        arr[k++] = rightArr[j++];
      }
    }
    // Copy remaining elements of leftArr
    while (i < m) {
      arr[k++] = leftArr[i++];
    }
    // Copy remaining elements of rightArr
    while (j < n) {
      arr[k++] = leftArr[j++];
    }
  }
}
