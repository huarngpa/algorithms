package com.github.huarngpa.leetcode.coinbase;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TextJustification {

  /**
   * Given an array of strings words and a width maxWidth, format the text such that each line has
   * exactly maxWidth characters and is fully (left and right) justified.
   *
   * <p>You should pack your words in a greedy approach; that is, pack as many words as you can in
   * each line. Pad extra spaces ' ' when necessary so that each line has exactly maxWidth
   * characters.
   *
   * <p>Extra spaces between words should be distributed as evenly as possible. If the number of
   * spaces on a line does not divide evenly between words, the empty slots on the left will be
   * assigned more spaces than the slots on the right.
   *
   * <p>For the last line of text, it should be left-justified, and no extra space is inserted
   * between words.
   *
   * <p>Note:
   *
   * <p>A word is defined as a character sequence consisting of non-space characters only. Each
   * word's length is guaranteed to be greater than 0 and not exceed maxWidth. The input array words
   * contains at least one word.
   */
  public List<String> fullJustify(String[] words, int maxWidth) {
    List<String> result = new ArrayList<>();
    ArrayDeque<String> line = new ArrayDeque<>();
    int wordCharCount = 0;
    for (String word : words) {
      if ((wordCharCount + word.length() + line.size() - 1) <= maxWidth) {
        line.add(word);
        wordCharCount += word.length();
      } else if (line.size() == 1) {
        // Delegate to another method to build line
        buildSingleWordLine(result, line.poll(), maxWidth);
        // Reset
        line.clear();
        line.add(word);
        wordCharCount = word.length();
      } else {
        // Delegate to another method to build line
        buildLine(result, line, maxWidth - wordCharCount);
        // Reset
        line.clear();
        line.add(word);
        wordCharCount = word.length();
      }
    }
    if (!line.isEmpty()) {
      if (line.size() == 1) {
        buildSingleWordLine(result, line.poll(), maxWidth);
      } else {
        buildLine(result, line, maxWidth - wordCharCount);
      }
    }
    return result;
  }

  private void buildSingleWordLine(List<String> result, String word, int maxWidth) {
    StringBuilder builder = new StringBuilder();
    builder.append(word);
    if (maxWidth - word.length() > 0) {
      builder.append(" ".repeat(maxWidth - word.length()));
    }
    result.add(builder.toString());
  }

  private void buildLine(List<String> result, ArrayDeque<String> line, int totalSpacing) {
    // Figure out spacing
    // int totalSpacing = maxWidth - wordCharCount;
    int spacing = totalSpacing / (line.size() - 1);
    int mod = totalSpacing % (line.size() - 1);
    // Build string
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < mod; i++) {
      builder.append(line.poll()).append(" ".repeat(spacing + 1));
    }
    while (line.size() > 1) {
      builder.append(line.poll()).append(" ".repeat(spacing));
    }
    builder.append(line.poll());
    result.add(builder.toString());
  }
}
