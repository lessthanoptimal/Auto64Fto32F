/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peterabeles.auto64fto32f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Converts a file written for 64bit numbers into 32bit numbers by replacing keywords.
 *
 * @author Peter Abeles
 */
public class ConvertFile32From64 {

    InputStream in;
    PrintStream out;

    List<Replacement> replacements = new ArrayList<>();
    List<Replacement> replaceStartsWith = new ArrayList<>();
    List<Replacement> replacementsAfter = new ArrayList<>();

    /**
     * Constructor
     * @param addDefaultReplacements If true all of the defaults replacement patterns are applied.
     */
    public ConvertFile32From64( boolean addDefaultReplacements ) {
        if( addDefaultReplacements ) {
            replacePattern("/\\*\\*/double", "FIXED_DOUBLE");
            replacePattern("double", "float");
            replacePattern("Double", "Float");
            replacePattern("_F64", "_F32");

            replaceStartsWith("Math.", "(float)Math.");
            replaceStartsWith("-Math.", "(float)-Math.");

            replacePatternAfter("FIXED_DOUBLE", "/\\*\\*/double");
        }
    }

    /**
     * Applies the specified keyword replacements to the input file and saves the results to the output file
     * @param inputFile File that is to be transformed. Unmodified.
     * @param outputFile Where results of the transformation are written to.  Modified
     * @throws IOException If something goes wrong this is thrown.
     */
    public void process(File inputFile, File outputFile ) throws IOException {
        in = new FileInputStream(inputFile);
        out = new PrintStream(outputFile);

        int n;
        StringBuffer s = new StringBuffer(1024);
        boolean prevChar = false;

        // copyright is a special case and don't wont it turning Apache 2.0 info 2.0f
        copyTheCopyRight(s);

        while ((n = in.read()) != -1) {
            if (Character.isWhitespace((char) n)) {
                if (prevChar) {
                    handleToken(s.toString());
                    s.delete(0, s.length());
                    prevChar = false;
                }
                out.write(n);
            } else {
                prevChar = true;
                s.append((char) n);
            }
        }

        if (prevChar) {
            handleToken(s.toString());
        }

        out.close();
        in.close();

    }

    private void copyTheCopyRight(StringBuffer s) throws IOException {
        int n;

        while ((n = in.read()) != -1) {
            char c = (char) n;
            s.append(c);

            if (c == '\n') {
                out.print(s);
                boolean finished = s.length() == 4 && s.charAt(2) == '/';
                s.delete(0, s.length());
                if (finished)
                    return;
            }

        }
    }

    /**
     * Adds a text replacement rule.  These will be run in the first pass
     *
     * @param pattern REGEX pattern that is searched for inside the word
     * @param replacement The string that the matching portion will be replaced with
     */
    public void replacePattern(String pattern, String replacement) {
        replacements.add(new Replacement(pattern, replacement));
    }

    /**
     * If a world starts with the pattern (just a string not a REGEX) then the matching text will be replaced
     * by the replacement string.
     *
     * @param pattern Regular text string
     * @param replacement The string that the matching portion will be replaced with
     */
    public void replaceStartsWith(String pattern, String replacement) {
        replaceStartsWith.add(new Replacement(pattern, replacement));
    }

    /**
     * Adds a text replacement rule.  These will be run in the final pass
     *
     * @param pattern REGEX pattern that is searched for inside the word
     * @param replacement The string that the matching portion will be replaced with
     */
    public void replacePatternAfter(String pattern, String replacement) {
        replacementsAfter.add(new Replacement(pattern, replacement));
    }

    private void handleToken(String s) {
        for (int i = 0; i < replacements.size(); i++) {
            Replacement r = replacements.get(i);
            s = s.replaceAll(r.pattern, r.replacement);
        }

        for (int i = 0; i < replaceStartsWith.size(); i++) {
            Replacement r = replaceStartsWith.get(i);
            s = replaceStartString(s, r.pattern, r.replacement);
        }

        s = handleFloats(s);

        for (int i = 0; i < replacementsAfter.size(); i++) {
            Replacement r = replacementsAfter.get(i);
            s = s.replaceAll(r.pattern, r.replacement);
        }

        out.print(s);
    }

    /**
     * Looks for a floating point constant number and tacks on a 'f' to the end
     * to make it into a float and not a double.
     */
    private String handleFloats(String input) {
        String regex = "\\d+\\.+\\d+([eE][-+]?\\d+)?";

        return input.replaceAll(regex, "$0f");
    }

    private String replaceStartString(String input, String from, String to) {

        if (input.startsWith(from)) {
            return to + input.substring(from.length());
        } else {
            return input;
        }
    }

    private static class Replacement {
        public String pattern;
        public String replacement;

        public Replacement(String pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
    }
}
