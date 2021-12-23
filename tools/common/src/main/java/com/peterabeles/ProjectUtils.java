/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Various utility functions for navigating a project
 *
 * @author Peter Abeles
 */
public class ProjectUtils {

    /** Root directory with source code in project */
    public static String projectMain = "main";

    /** Path to JMH benchmarks */
    public static String pathBenchmarks = "src/benchmark/java";

    /** Used to indicate if there are directories it should skip */
    public static SkipDirectory skipTest = (f)->false;

    /**
     * Used to determine if a directory is the project root. Default looks for .gitignore.
     */
    public static ProjectRoot checkRoot = (f) -> new File(".gitignore").exists();

    /**
     * Provides information about the library. Must be configured.
     */
    public static final LibrarySourceInfo libraryInfo = new LibrarySourceInfo();

    public static String findPathToProjectRoot() {
        String path = "./";
        while (!checkRoot.isRootDir(new File(path))) {
            path = "../" + path;
            if (!new File(path).exists()) {
                throw new RuntimeException("Couldn't find project root. path=" + path);
            }
        }
        return Paths.get(path).normalize().toFile().getAbsolutePath();
    }

    public static String projectRelativePath(String path) {
        File f = new File(path);
        if (f.isAbsolute())
            return path;

        return new File(findPathToProjectRoot(), path).getAbsolutePath();
    }

    public static String readLine(InputStream input, StringBuilder buffer) throws IOException {
        buffer.setLength(0);

        while (true) {
            int v = input.read();
            if (v == -1 || v == 10) {
                return buffer.toString();
            }

            buffer.append((char) v);
        }
    }

    public static String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        return dateFormat.format(date);
    }

    @FunctionalInterface
    public interface ProjectRoot {
        boolean isRootDir(File file);
    }

    @FunctionalInterface
    public interface SkipDirectory {
        boolean skip(File path);
    }
}
