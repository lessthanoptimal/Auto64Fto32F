/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterebeles.autocode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generates concurrent implementations of a class using hints provided in comments throughout the code.
 *
 * <ul>
 *     <li>//CONCURRENT_CLASS_NAME TEXT override the default class name. Can be anywhere.</li>
 *     <li>//CONCURRENT_INLINE TEXT  will remove the comment in insert the text</li>
 *     <li>//CONCURRENT_ABOVE TEXT  will replace the line above with the text</li>
 *     <li>//CONCURRENT_BELOW TEXT  will replace the line below with the text</li>
 *     <li>//CONCURRENT_REMOVE_BELOW will remove the line below</li>
 *     <li>//CONCURRENT_REMOVE_ABOVE will remove the line above</li>
 *     <li>//CONCURRENT_REMOVE_LINE will remove the line it is placed on</li>
 *     <li>//CONCURRENT_MACRO NAME TEXT creates a macro that can be used instead of text</li>
 *     <li>//CONCURRENT_OMIT_BEGIN It will omit everything until it finds an OMIT_END</li>
 *     <li>//CONCURRENT_OMIT_END It will stop omitting when this is encountered.</li>
 * </ul>
 *
 * A macro is identified by enclosing its name with brackets, e.g. {NAME}.
 *
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway")
public class AutocodeConcurrent {
    public static String IMPORT_GENERATED = "import javax.annotation.Generated;";

    public static String prefix = "//CONCURRENT_";
    public static String tab = "\t";
    public static String sourceRootName = "java";
    public static String pathRootToTest = "../../test/java";
    public static ConvertString originalToMT = name -> name + "_MT.java";

    /**
     * Converts the file from single thread into concurrent implementation
     */
    public static void convertFile(File original) throws IOException {
        File outputFile = determineClassName(original);

        String classNameOld = className(original);
        String classNameNew = className(outputFile);

        // Read the file and split it up into lines
        List<String> inputLines = readLines(original, UTF_8);
        List<String> outputLines = new ArrayList<>();

        List<Macro> macros = new ArrayList<>();

        // If an import statement has been found
        boolean foundImport = false;

        // parse each line by line looking for instructions
        boolean foundClassDef = false;
        // If true it will not copy lines over
        boolean omit = false;
        for (int i = 0; i < inputLines.size(); i++) {
            String line = inputLines.get(i);
            int where = line.indexOf(prefix);
            if (where < 0) {
                if (!foundImport && !foundClassDef && line.startsWith("import")) {
                    foundImport = true;
                    // Don't add the import twice
                    if (!containsStartsWith(IMPORT_GENERATED, inputLines))
                        outputLines.add(IMPORT_GENERATED);
                } else if (!foundClassDef && line.contains("class " + classNameOld)) {
                    foundClassDef = true;
                    if (foundImport)
                        outputLines.add("@Generated(\"" + derivePackagePath(outputFile) + "." + classNameOld + "\")");
                    line = line.replaceFirst("class " + classNameOld, "class " + classNameNew);
                } else if (foundImport && line.startsWith("@Generated")) {
                    // If the file already has a generated statement and we are going to add our own remove the old one
                    continue;
                } else {
                    line = line.replace(classNameOld + "(", classNameNew + "(");
                }
                if (omit)
                    continue;
                for (Macro m : macros)
                    line = line.replace(m.name, m.text);
                outputLines.add(line);
                continue;
            }
            String type = readType(line, where + prefix.length());
            String whitespaces = line.substring(0, where);
            int frontLength = where + prefix.length() + type.length();
            String message = line.length() > frontLength ? line.substring(frontLength + 1) : "";
            switch (type) {
                case "CLASS_NAME":
                    continue; // ignore. already processed
                case "INLINE":
                    outputLines.add(whitespaces + message);
                    break;
                case "ABOVE":
                    // remove the previous line
                    outputLines.remove(outputLines.size() - 1);
                    outputLines.add(whitespaces + message);
                    break;
                case "BELOW":
                    outputLines.add(whitespaces + message);
                    i += 1; // skip next line
                    break;
                case "REMOVE_ABOVE":
                    outputLines.remove(outputLines.size() - 1);
                    break;
                case "REMOVE_BELOW":
                    i += 1; // skip next line
                    break;
                case "REMOVE_LINE":
                    break;
                case "OMIT_BEGIN":
                    omit = true;
                    break;
                case "OMIT_END":
                    omit = false;
                    break;
                case "MACRO": {
                    String[] words = message.split(" ");
                    if (words.length != 2)
                        throw new RuntimeException("Expected only two words for the macro. " + message);
                    Macro m = new Macro();
                    m.name = words[0];
                    m.text = words[1];
                    macros.add(m);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown: " + type);
            }
        }

        PrintStream out = new PrintStream(outputFile);
        for (int i = 0; i < outputLines.size(); i++) {
            out.println(outputLines.get(i));
        }
        out.close();

        createTestIfNotThere(outputFile, sourceRootName, pathRootToTest);
    }

    /**
     * If a test class doesn't exist it will create one. This is to remind the user to do it
     */
    private static void createTestIfNotThere(File file, String sourceRootName, String pathRootToTest) {
        String fileName = "Test" + file.getName();

        List<String> packagePath = new ArrayList<>();
        while (true) {
            if (file.getParentFile() == null) {
                throw new IllegalArgumentException("Problem! Can't find '" + sourceRootName + "' directory");
            }
            String parentName = file.getParentFile().getName();
            file = file.getParentFile();
            if (parentName.equals(sourceRootName)) {
                break;
            } else {
                packagePath.add(parentName);
            }
        }
        file = new File(file, pathRootToTest);
        for (int i = packagePath.size() - 1; i >= 0; i--) {
            file = new File(file, packagePath.get(i));
        }
        file = new File(file, fileName);
        // only create it if it doesn't exist
        if (file.exists()) {
            return;
        }
        // Simplify the path before passing it in
        createTestFile(file.toPath().toAbsolutePath().normalize().toFile());
    }

    private static void createTestFile(File path) {

        if (!path.getParentFile().exists()) {
            if (!path.getParentFile().mkdirs())
                throw new RuntimeException("Failed to create directories. " + path.getAbsolutePath());
        }
        System.out.println("Creating " + path);

        try {
            String className = className(path);
            String packagePath = derivePackagePath(path);

            PrintStream out = new PrintStream(path);
            out.println("package " + packagePath + ";\n" +
                    "\n" +
                    "import org.junit.jupiter.api.Test;\n" +
                    "\n" +
                    "import static org.junit.jupiter.api.Assertions.fail;\n" +
                    "\n" +
                    "class " + className + " {\n" +
                    tab + "@Test\n" +
                    tab + "void compareToSingle() {\n" +
                    tab + tab + "fail(\"implement\");\n" +
                    tab + "}\n" +
                    "}\n");
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String derivePackagePath(File file) {
        List<String> packagePath = new ArrayList<>();
        while (true) {
            if (file.getParentFile() == null)
                throw new IllegalArgumentException("Problem! Can't find java directory");

            String parent = file.getParentFile().getName();
            if (parent.equals("test") || parent.equals("src") || parent.equals("java")) {
                break;
            } else {
                packagePath.add(parent);
                file = file.getParentFile();
            }
        }

        String output = "";
        for (int i = packagePath.size() - 1; i >= 0; i--) {
            output += packagePath.get(i) + ".";
        }
        return output.substring(0, output.length() - 1);
    }

    /**
     * Searches the input file for an override. If none is found then _MT is added to the class name.
     *
     * @param original Input file
     * @return Output file
     */
    private static File determineClassName(File original) throws IOException {
        String text = readFileToString(original, Charset.forName("UTF-8"));

        if (!text.contains("//CONCURRENT"))
            throw new IOException("Not a concurrent file");

        String pattern = "//CONCURRENT_CLASS_NAME ";
        int where = text.indexOf(pattern);
        if (where < 0) {
            String name = originalToMT.convert(className(original));
            return new File(original.getParent(), name);
        }

        String name = readUntilEndOfLine(text, where + pattern.length());
        return new File(original.getParent(), name + ".java");
    }

    private static String readType(String line, int location) {
        int index0 = location;
        while (location < line.length()) {
            char c = line.charAt(location);
            if (Character.isWhitespace(c)) {
                return line.substring(index0, location);
            }
            location += 1;
        }
        // something went wrong
        return line.substring(index0, location);
    }

    private static String readUntilEndOfLine(String text, int location) {
        int index0 = location;
        while (location < text.length()) {
            char c = text.charAt(location);
            if (c == '\r' || c == '\n') {
                return text.substring(index0, location);
            }
            location += 1;
        }
        return text.substring(index0, location);
    }

    static String readFileToString(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    private static String className(File file) {
        String n = file.getName();
        return n.substring(0, n.length() - 5);
    }

    private static class Macro {
        String name;
        String text;
    }

    public static void convertDir( File directory, String include, String exclude ) {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("Must be a directory: '" + directory.getPath() + "'");
        File[] files = directory.listFiles();
        if (files == null)
            throw new IllegalArgumentException("No files");
        for (File f : files) {
            String name = f.getName();
            if (!name.matches(include) || name.matches(exclude))
                continue;
            try {
                convertFile(f);
            } catch (IOException ignore) {
//				System.out.println(name+" "+e.getMessage());
            }
        }
    }

    public static List<String> readLines(final File file, final Charset encoding) throws IOException {
        List<String> lines = new ArrayList<>();
        try (Stream<String> stream = Files.lines(file.toPath(), encoding)) {
            stream.forEach(lines::add);
        }
        return lines;
    }

    /**
     * Checks to see if any of the lines start with the specified text
     */
    public static boolean containsStartsWith(String text, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(text))
                return true;
        }
        return false;
    }

    @FunctionalInterface public interface ConvertString {
        String convert(String input);
    }
}
