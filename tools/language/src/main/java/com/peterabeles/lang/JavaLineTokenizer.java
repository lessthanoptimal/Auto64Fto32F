/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Breaks a line up into tokens for easier processing.
 *
 * @author Peter Abeles
 */
public class JavaLineTokenizer {
    // All that's needed now are these string tokens. In the future it could be broken up into enums
    List<String> stringTokens = new ArrayList<>();

    StringBuilder sb = new StringBuilder(200);

    public JavaLineTokenizer parse(String line) {
        stringTokens.clear();

        // Remove and use white space to split into initial set of works
        List<String> words = stringAwareSplit(line.trim());

        // Find the words which will describe each token
        for (int i = 0; i < words.size(); i++) {
            splitIntoTokens(words.get(i), stringTokens);
        }

        return this;
    }

    static void splitIntoTokens(String word, List<String> tokens) {
        // if it's a string token just add it as is
        if (word.charAt(0) == '"') {
            tokens.add(word);
            return;
        }
        int start = 0;
        boolean identifier = Character.isJavaIdentifierPart(word.charAt(0));

        for (int i = 0; i < word.length(); i++) {
            if (identifier != Character.isJavaIdentifierPart(word.charAt(i))) {
                identifier = !identifier;
                if (identifier) {
                    tokens.addAll(splitWordNotIdentifier(word.substring(start, i)));
                } else {
                    tokens.add(word.substring(start, i));
                }
                start = i;
            }
        }
        tokens.add(word.substring(start));
    }

    /**
     * Split a chain of symbols into multiple symbols
     * "=()" -> "", "(", ")"
     * "!=(" -> "!=", "("
     */
    static List<String> splitWordNotIdentifier(String word) {
        var found = new ArrayList<String>();

        // Look at pairs of non-identifier characters and determine if they are a single symbol or not
        for (int i = 0; i < word.length(); i++) {
            if (i + 1 == word.length()) {
                found.add(word.charAt(i) + "");
                continue;
            }
            char a = word.charAt(i);
            char b = word.charAt(i + 1);

            boolean split = true;
            if (b == '=')
                split = false;
            else if (a == '+' && b == '+')
                split = false;
            else if (a == '-' && b == '-')
                split = false;
            else if (a == '>' && b == '>')
                split = false;
            else if (a == '<' && b == '<')
                split = false;
            // TODO handle <<= and >>=

            if (split) {
                found.add(a + "");
            } else {
                found.add(a + "" + b);
                i++;
            }
        }

        return found;
    }


    List<String> stringAwareSplit(String line) {
        List<String> words = new ArrayList<>();

        boolean insideString = false;
        boolean escape = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (insideString) {
                if (!escape && c == '\\')
                    escape = true;
                else {
                    sb.append(c);
                    if (escape) {
                        escape = false;
                    } else {
                        if (c == '"') {
                            addWord(words);
                            insideString = false;
                        }
                    }
                }
            } else {
                if (Character.isWhitespace(c)) {
                    if (sb.length() > 0) {
                        addWord(words);
                    }
                } else if (c == '"') {
                    if (sb.length() > 0) {
                        addWord(words);
                    }
                    sb.append(c);
                    insideString = true;
                    escape = false;
                } else {
                    sb.append(c);
                }
            }
        }
        if (sb.length() > 0)
            addWord(words);
        return words;
    }

    void addWord(List<String> words) {
        words.add(sb.toString());
        sb.delete(0, sb.length());
    }
}
