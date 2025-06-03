package com.github.huarngpa.leetcode.coinbase;

import java.util.*;

/**
 * Design a data structure that simulates an in-memory file system.
 *
 * <p>Implement the FileSystem class:
 *
 * <p>FileSystem() Initializes the object of the system. List<String> ls(String path) If path is a
 * file path, returns a list that only contains this file's name. If path is a directory path,
 * returns the list of file and directory names in this directory. The answer should in
 * lexicographic order. void mkdir(String path) Makes a new directory according to the given path.
 * The given directory path does not exist. If the middle directories in the path do not exist, you
 * should create them as well. void addContentToFile(String filePath, String content) If filePath
 * does not exist, creates that file containing given content. If filePath already exists, appends
 * the given content to original content. String readContentFromFile(String filePath) Returns the
 * content in the file at filePath.
 */
public class FileSystem {

  class Dir {
    HashMap<String, Dir> dirs = new HashMap<>();
    HashMap<String, String> files = new HashMap<>();
  }

  Dir root;

  public FileSystem() {
    this.root = new Dir();
  }

  public List<String> ls(String path) {
    Dir t = root;
    List<String> files = new ArrayList<>();
    if (!path.equals("/")) {
      String[] d = path.split("/");
      for (int i = 1; i < d.length - 1; i++) {
        t = t.dirs.get(d[i]);
      }
      // End of path resolution
      if (t.files.containsKey(d[d.length - 1])) {
        files.add(d[d.length - 1]);
        return files;
      } else {
        t = t.dirs.get(d[d.length - 1]);
      }
    }
    files.addAll(t.dirs.keySet());
    files.addAll(t.files.keySet());
    Collections.sort(files);
    return files;
  }

  public void mkdir(String path) {
    Dir t = root;
    String[] d = path.split("/");
    for (int i = 1; i < d.length; i++) {
      t = t.dirs.computeIfAbsent(d[i], ignored -> new Dir());
    }
  }

  public void addContentToFile(String filePath, String content) {
    Dir t = root;
    String[] d = filePath.split("/");
    for (int i = 1; i < d.length - 1; i++) {
      t = t.dirs.get(d[i]);
    }
    t.files.put(d[d.length - 1], t.files.getOrDefault(d[d.length - 1], "") + content);
  }

  public String readContentFromFile(String filePath) {
    Dir t = root;
    String[] d = filePath.split("/");
    for (int i = 1; i < d.length - 1; i++) {
      t = t.dirs.get(d[i]);
    }
    return t.files.get(d[d.length - 1]);
  }
}
