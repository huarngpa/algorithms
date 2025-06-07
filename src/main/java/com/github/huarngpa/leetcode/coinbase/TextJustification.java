package com.github.huarngpa.leetcode.coinbase;

import java.util.ArrayList;
import java.util.LinkedList;
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
    List<String> result = new LinkedList<>();
    int i = 0;
    while (i < words.length) {
      List<String> line = getWords(i, words, maxWidth);
      i += line.size();
      result.add(createLine(line, i, words, maxWidth));
    }
    return result;
  }

  private List<String> getWords(int i, String[] words, int maxWidth) {
    List<String> currentLine = new ArrayList<>();
    int currLength = 0;
    while (i < words.length && currLength + words[i].length() <= maxWidth) {
      currentLine.add(words[i]);
      currLength += words[i].length() + 1;
      i++;
    }
    return currentLine;
  }

  private String createLine(List<String> line, int i, String[] words, int maxWidth) {
    int baseLength = -1;
    for (String word : line) {
      baseLength += word.length() + 1;
    }
    int extraSpaces = maxWidth - baseLength;
    if (line.size() == 1 || i == words.length) {
      return String.join(" ", line) + " ".repeat(extraSpaces);
    }
    int wordCount = line.size() - 1;
    int spacesPerWord = extraSpaces / wordCount;
    int neededExtraSpace = extraSpaces % wordCount;
    for (int j = 0; j < neededExtraSpace; j++) {
      line.set(j, line.get(j) + " ");
    }
    for (int j = 0; j < wordCount; j++) {
      line.set(j, line.get(j) + " ".repeat(spacesPerWord));
    }
    return String.join(" ", line);
  }
}
