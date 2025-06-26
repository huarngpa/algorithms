package com.github.huarngpa.java;

import java.util.Arrays;

public class JavaStudyGuide {

  public static void stringManipulations() {
    // Splitting
    System.out.println("Splitting: " + Arrays.toString("This is split".split(" ")));
    // String builder
    StringBuilder builder = new StringBuilder();
    builder.append("Adding").append(" a ").append("string.");
    System.out.println("Builder: " + builder);
    // Other basic operations
    System.out.println("Substring: " + "something".substring(4));
    System.out.println("Trim: " + "      trim    ".trim());
  }

  public static void main(String[] args) {
    stringManipulations();
  }
}
