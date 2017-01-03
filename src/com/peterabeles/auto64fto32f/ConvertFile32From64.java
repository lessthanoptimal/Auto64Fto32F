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

        State state = State.INITIALIZING;
        int totalTokens = 0;
        boolean insideBlockComments = false;
        boolean insideLineComment = false;

        while ((n = in.read()) != -1) {
            if( insideLineComment && (n == '\n' || n == '\r')) {
                insideLineComment = false;
            }
            if (Character.isWhitespace((char) n)) {
                if (prevChar) {
                    String token = s.toString();
                    if( insideBlockComments ) {
                        if( token.startsWith("*/") )
                            insideBlockComments = false;
                    }
                    if( !(insideBlockComments||insideLineComment) ){
                        if( token.startsWith("/*") )
                            insideBlockComments = true;
                        else if( token.startsWith("//"))
                            insideLineComment = true;
                    }
                    switch( state ) {
                        case INITIALIZING:
                            if(totalTokens==0 && token.startsWith("/*") ) {
                                state = State.INSIDE_COPYRIGHT;
                            } else if( !(insideBlockComments||insideLineComment) && token.compareTo("class") == 0 ) {
                                state = State.BEFORE_CLASS_NAME;
                            }
                            handleToken(token);
                            break;

                        case INSIDE_COPYRIGHT:
                            if( token.compareTo("*/") == 0 ) {
                                state = State.INITIALIZING;
                            }
                            out.print(token);
                            break;

                        case BEFORE_CLASS_NAME: // for the class name to be the same as the output file
                            state = State.MAIN;
                            String name = outputFile.getName();
                            out.print(name.substring(0,name.length()-5));
                            break;

                        case MAIN:
                            handleToken(token);
                            break;
                    }
                    s.delete(0, s.length());
                    prevChar = false;
                    totalTokens++;
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

    private enum State {
        INITIALIZING,
        INSIDE_COPYRIGHT,
        BEFORE_CLASS_NAME,
        MAIN
    }
}
