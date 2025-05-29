package com.github.huarngpa.dcp.hashtables;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class LruCacheTest {
  @Test
  void testBasicOperations() {
    LruCache cache = new LruCache(2);
    cache.set("key1", 1);
    cache.set("key2", 2);
    assertEquals(1, cache.get("key1"), "Should return value for key1");
    assertEquals(2, cache.get("key2"), "Should return value for key2");
    cache.set("key3", 3); // Evicts key1
    assertNull(cache.get("key1"), "key1 should be evicted");
    assertEquals(2, cache.get("key2"), "key2 should still be present");
    assertEquals(3, cache.get("key3"), "key3 should be present");
  }

  @Test
  void testUpdateExistingKey() {
    LruCache cache = new LruCache(2);
    cache.set("key1", 1);
    cache.set("key2", 2);
    cache.set("key1", 10); // Update key1
    assertEquals(10, cache.get("key1"), "Should return updated value for key1");
    cache.set("key3", 3); // Evicts key2
    assertNull(cache.get("key2"), "key2 should be evicted");
    assertEquals(10, cache.get("key1"), "key1 should still be present");
  }

  @Test
  void testSingleCapacity() {
    LruCache cache = new LruCache(1);
    cache.set("key1", 1);
    assertEquals(1, cache.get("key1"), "Should return value for key1");
    cache.set("key2", 2); // Evicts key1
    assertNull(cache.get("key1"), "key1 should be evicted");
    assertEquals(2, cache.get("key2"), "key2 should be present");
  }

  @Test
  void testMissingKey() {
    LruCache cache = new LruCache(2);
    assertNull(cache.get("key1"), "Should return null for missing key");
    cache.set("key1", 1);
    assertEquals(1, cache.get("key1"), "Should return value for key1");
    assertNull(cache.get("key2"), "Should return null for missing key");
  }

  @Disabled
  @Test
  void testNullKey() {
    LruCache cache = new LruCache(2);
    assertThrows(
        NullPointerException.class,
        () -> {
          cache.set(null, 1);
        },
        "Should throw exception for null key in set");
    assertThrows(
        NullPointerException.class,
        () -> {
          cache.get(null);
        },
        "Should throw exception for null key in get");
  }

  @Test
  void testZeroCapacity() {
    LruCache cache = new LruCache(0);
    cache.set("key1", 1);
    assertNull(cache.get("key1"), "Should not store items with zero capacity");
  }

  @Test
  void testFrequentAccess() {
    LruCache cache = new LruCache(3);
    cache.set("key1", 1);
    cache.set("key2", 2);
    cache.set("key3", 3);
    cache.get("key1"); // Makes key1 most recent
    cache.set("key4", 4); // Evicts key2
    assertNull(cache.get("key2"), "key2 should be evicted");
    assertEquals(1, cache.get("key1"), "key1 should be present");
    assertEquals(3, cache.get("key3"), "key3 should be present");
    assertEquals(4, cache.get("key4"), "key4 should be present");
  }

  @Test
  void testLargeCapacity() {
    LruCache cache = new LruCache(5);
    cache.set("key1", 1);
    cache.set("key2", 2);
    cache.set("key3", 3);
    cache.set("key4", 4);
    cache.set("key5", 5);
    assertEquals(1, cache.get("key1"), "key1 should be present");
    assertEquals(2, cache.get("key2"), "key2 should be present");
    cache.set("key6", 6); // Evicts key3
    assertNull(cache.get("key3"), "key3 should be evicted");
  }
}
