package com.github.huarngpa.clrs;

import java.util.Arrays;

public class MaxHeap {

  private int[] heap;
  private int size;
  private int capacity;

  public MaxHeap(int capacity) {
    this.capacity = capacity;
    this.size = 0;
    this.heap = new int[capacity];
  }

  public MaxHeap(int[] arr) {
    this.capacity = arr.length;
    this.size = arr.length;
    this.heap = Arrays.copyOf(arr, arr.length);
    buildMaxHeap();
  }

  private int parent(int i) {
    return (i - 1) / 2;
  }

  private int left(int i) {
    return 2 * i + 1;
  }

  private int right(int i) {
    return 2 * i + 2;
  }

  private void swap(int i, int j) {
    int temp = heap[i];
    heap[i] = heap[j];
    heap[j] = temp;
  }

  private void resize() {
    capacity *= 2;
    heap = Arrays.copyOf(heap, capacity);
  }

  public void insert(int value) {
    if (size >= capacity) {
      resize(); // Double the capacity if full
    }
    // Place new element at the end
    heap[size] = value;
    size++;
    // Fix heap property by bubbling up
    bubbleUp(size - 1);
  }

  private void bubbleUp(int i) {
    while (i > 0 && heap[i] > heap[parent(i)]) {
      swap(i, parent(i));
      i = parent(i);
    }
  }

  // Extract and return the maximum element (root)
  public int extractMax() {
    if (size == 0) {
      throw new IllegalStateException("Heap is empty");
    }
    int max = heap[0]; // Root is the maximum
    heap[0] = heap[size - 1];
    size--;
    if (size > 0) {
      maxHeapify(0); // Fix heap property
    }
    return max;
  }

  // Heapify subtree rooted at index i
  private void maxHeapify(int i) {
    int largest = i;
    int left = left(i);
    int right = right(i);
    if (left < size && heap[left] > heap[largest]) {
      largest = left;
    }
    if (right < size && heap[right] > heap[largest]) {
      largest = right;
    }
    if (largest != i) {
      swap(i, largest);
      maxHeapify(largest); // Recurse on affected subtree
    }
  }

  // Build max-heap from the array
  private void buildMaxHeap() {
    for (int i = size / 2 - 1; i >= 0; i--) {
      maxHeapify(i);
    }
  }

  // Heap sort: Sort the array in ascending order
  public void heapSort() {
    int originalSize = size;
    buildMaxHeap();
    for (int i = size - 1; i > 0; i--) {
      swap(0, i); // Move root to end
      size--; // Reduce heap size
      maxHeapify(0); // Fix heap property
    }
    size = originalSize; // Restore size for further heap operations
  }

  // Peek at the maximum element without removing it
  public int getMax() {
    if (size == 0) {
      throw new IllegalStateException("Heap is empty");
    }
    return heap[0];
  }

  public boolean isMaxHeap() {
    for (int i = 0; i <= (size - 2) / 2; i++) {
      int left = left(i);
      int right = right(i);
      if (left < size && heap[i] < heap[left]) {
        return false;
      }
      if (right < size && heap[i] < heap[right]) {
        return false;
      }
    }
    return true;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public void printHeap() {
    System.out.println("Heap: ");
    for (int i = 0; i < size; i++) {
      System.out.println(heap[i] + " ");
    }
    System.out.println();
  }

  // Main method for testing
  public static void main(String[] args) {
    // Test heap operations
    MaxHeap heap = new MaxHeap(10);
    int[] values = {4, 10, 3, 5, 1};
    for (int value : values) {
      heap.insert(value);
      heap.printHeap();
      System.out.println("Is Max-Heap: " + heap.isMaxHeap());
    }

    System.out.println("Max element: " + heap.getMax());
    System.out.println("Extracted max: " + heap.extractMax());
    heap.printHeap();
    System.out.println("Is Max-Heap: " + heap.isMaxHeap());

    // Test heap sort
    int[] arr = {12, 11, 13, 5, 6, 7};
    MaxHeap heapForSort = new MaxHeap(arr);
    System.out.println("Original array: " + Arrays.toString(arr));
    heapForSort.heapSort();
    System.out.println("Sorted array: " + Arrays.toString(heapForSort.heap));
  }
}
