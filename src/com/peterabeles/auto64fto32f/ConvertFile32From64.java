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

    // file specified custom list of ignore tokens
    List<String> customIgnore = new ArrayList<>();
    List<Replacement> replacements = new ArrayList<>();
    List<Replacement> replaceStartsWith = new ArrayList<>();
    List<Replacement> replacementsAfter = new ArrayList<>();

    boolean skipFilterOnLine;

    /**
     * Constructor
     * @param addDefaultReplacements If true all of the defaults replacement patterns are applied.
     */
    public ConvertFile32From64( boolean addDefaultReplacements ) {
        if( addDefaultReplacements ) {
//            replacePattern("/\\*\\*/double", "FIXED_DOUBLE");
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
        scanForCustomization(inputFile);

        try {
            in = new FileInputStream(inputFile);
            out = new PrintStream(outputFile);

            int n;
            StringBuffer s = new StringBuffer(1024);
            boolean prevChar = false;

            State state = State.INITIALIZING;
            int totalTokens = 0;
            boolean insideBlockComments = false;
            boolean insideLineComment = false;

            int lineCharacterCount = 0;
            skipFilterOnLine = false;

            while ((n = in.read()) != -1) {
                if( n == '\n' || n == '\r' ) {
                    lineCharacterCount = 0;
                    if (insideLineComment ) {
                        insideLineComment = false;
                    }
                } else {
                    lineCharacterCount++;
                }

                if (Character.isWhitespace((char) n)) {
                    if (prevChar) {
                        boolean skip = false;
                        String token = s.toString();
                        if (insideBlockComments) {
                            if (token.startsWith("*/"))
                                insideBlockComments = false;
                        }
                        if (!(insideBlockComments || insideLineComment)) {
                            if (token.startsWith("/*"))
                                insideBlockComments = true;
                            else if (token.startsWith("//"))
                                insideLineComment = true;
                        }

                        if( insideLineComment && lineCharacterCount == token.length()+1 ) {
                            if( token.startsWith("//NOFILTER")) {
                                skipFilterOnLine = true;
                                skip = true;
                            }
                        }
                        if( !skip ) {
                            switch (state) {
                                case INITIALIZING:
                                    if (totalTokens == 0 && token.startsWith("/*")) {
                                        state = State.INSIDE_COPYRIGHT;
                                    } else if (!(insideBlockComments || insideLineComment) && token.compareTo("class") == 0) {
                                        state = State.BEFORE_CLASS_NAME;
                                    }
                                    handleToken(token);
                                    break;

                                case INSIDE_COPYRIGHT:
                                    if (token.compareTo("*/") == 0) {
                                        state = State.INITIALIZING;
                                    }
                                    out.print(token);
                                    break;

                                case BEFORE_CLASS_NAME: // for the class name to be the same as the output file
                                    state = State.MAIN;
                                    String name = outputFile.getName();
                                    out.print(name.substring(0, name.length() - 5));
                                    break;

                                case MAIN:
                                    handleToken(token);
                                    break;
                            }
                        }
                        s.delete(0, s.length());
                        prevChar = false;
                        totalTokens++;
                    }
                    out.write(n);
                    if( n == '\n' || n == '\r' ) {
                        skipFilterOnLine = false;
                    }
                } else {
                    prevChar = true;
                    s.append((char) n);
                }
            }

            if (prevChar) {
                handleToken(s.toString());
            }
        } catch( IOException e ) {
            throw e;
        } finally {
            out.close();
            in.close();
            // Crashes when run in travis-ci with no error message.  Maybe file descriptors
            // are running out because the GC isn't running?
            System.gc();
        }
    }

    public void scanForCustomization( File inputFile ) throws IOException {
        customIgnore.clear();

        BufferedReader in = new BufferedReader(new FileReader(inputFile));

        String line;
        while( (line = in.readLine()) != null ) {
            if( !line.startsWith("//CUSTOM"))
                continue;

            String words[] = line.substring(9,line.length()).split(" ");
            if( words[0].equals("ignore")) {
                customIgnore.add(words[1]);
            }
        }

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
        boolean ignore = false;
        for (int i = 0; i < customIgnore.size(); i++) {
            if( s.equals(customIgnore.get(i))) {
                ignore = true;
                break;
            }
        }

        if( !ignore && !skipFilterOnLine && !s.contains("/**/") ) {
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
        }

        out.print(s);
        out.flush();
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
