package com.github.huarngpa.clrs;

public class BinarySearch {
  // Iterative Binary Search
  public static int binarySearch(int[] arr, int target) {
    int left = 0;
    int right = arr.length - 1;
    while (left <= right) {
      int mid = left + (right - left) / 2; // Avoids overflow vs (left + right) / 2
      if (arr[mid] == target) {
        return mid; // Found target
      } else if (arr[mid] < target) {
        left = mid + 1;
      } else {
        right = mid - 1;
      }
    }
    return -1; // Target not found
  }

  // Recursive Binary Search
  public static int binarySearchRecursive(int[] arr, int target, int left, int right) {
    if (left > right) {
      return -1; // Base case: not found
    }
    int mid = left + (right - left) / 2;
    if (arr[mid] == target) {
      return mid;
    } else if (arr[mid] < target) {
      return binarySearchRecursive(arr, target, mid + 1, right);
    } else {
      return binarySearchRecursive(arr, target, left, mid - 1);
    }
  }
}
