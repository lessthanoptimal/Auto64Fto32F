/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import com.peterabeles.LibrarySourceInfo;
import com.peterabeles.ProjectUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static com.peterabeles.ProjectUtils.formatDate;

/**
 * Utility functions for dealing with JMh log results.
 *
 * @author Peter Abeles
 */
public class RuntimeRegressionUtils {

    /**
     * Short summary of system and library info
     */
    public static String createInfoSummaryText() {
        LibrarySourceInfo info = ProjectUtils.libraryInfo;
        info.checkConfigured();

        String text = "";
        text += info.projectName + " Runtime Regression Baseline\n";
        text += "\n";
        text += "Hostname:      " + RuntimeRegressionUtils.getHostName() + "\n";
        text += "Machine Name:  " + SettingsLocal.machineName + "\n";
        text += "Date:          " + formatDate(new Date()) + "\n";
        text += String.format("%-14s %s\n", info.projectName + ":", info.version);
        text += String.format("%-14s %s\n", info.projectName + ":", info.gitDate);
        text += String.format("%-14s %s\n", info.projectName + ":", info.gitSha);
        return text;
    }

    /**
     * Adds useful information about the system it's being run on. Some of this will be system specific.
     */
    public static void saveSystemInfo(File directory, PrintStream err) {
        try {
            PrintStream out = new PrintStream(new File(directory, "SystemInfo.txt"));

            LibrarySourceInfo info = ProjectUtils.libraryInfo;
            info.checkConfigured();

            out.println("Hostname:      " + getHostName());
            out.println("Machine Name:  " + SettingsLocal.machineName);
            out.println("Date:          " + formatDate(new Date()));
            out.printf("%-14s %s\n", info.projectName + ":", info.version);
            out.printf("%-14s %s\n", info.projectName + ":", info.gitDate);
            out.printf("%-14s %s\n", info.projectName + ":", info.gitSha);

            // This won't work on every system
            try {
                double[] load = SystemInfo.lookupSystemLoad();

                out.println("---- Native Access Info");
                out.println("OS: " + SystemInfo.readOSVersion());
                out.println("CPU: " + SystemInfo.readCpu());
                out.println("Ave Load: 1m=" + load[0] + " 5m=" + load[1] + " 15m=" + load[2]);
            } catch (RuntimeException ignore) {
            }

            // This should work on every system
            out.println("----");
            out.println("Runtime.getRuntime().availableProcessors()," + Runtime.getRuntime().availableProcessors());
            out.println("Runtime.getRuntime().freeMemory()," + Runtime.getRuntime().freeMemory());
            out.println("Runtime.getRuntime().totalMemory()," + Runtime.getRuntime().totalMemory());

            String newLine = System.getProperty("line.separator");
            Properties properties = System.getProperties();
            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                String property = properties.getProperty(key.toString());
                // Get rid of newlines since they screw up the formatting
                property = property.replaceAll(newLine, "");
                out.println("\"" + key.toString() + "\",\"" + property + "\"");
            }
        } catch (Exception e) {
            e.printStackTrace(err);
            err.println("Error saving system info");
        }
    }

    /**
     * Returns the name of the device this regression is run on
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    /**
     * Loads all the JMH results in a directory and puts it into a map.
     */
    public static Map<String, Double> loadJmhResults(File directory) throws IOException {
        Map<String, Double> results = new HashMap<>();
        var parser = new ParseBenchmarkCsv();

        File[] children = directory.listFiles();
        if (children == null)
            return results;

        for (int i = 0; i < children.length; i++) {
            File f = children[i];
            if (!f.isFile() || !f.getName().endsWith(".csv"))
                continue;
            try {
                parser.parse(new FileInputStream(f));
                for (ParseBenchmarkCsv.Result r : parser.results) {
                    results.put(r.getKey(), r.getMilliSecondsPerOp());
                }
            } catch (IOException e) {
                throw new IOException("Exception parsing " + f.getPath(), e);
            }
        }

        return results;
    }

    /**
     * For every comparable result, see if the current performance shows any regressions
     *
     * @param tolerance fractional tolerance
     */
    public static Set<String> findRuntimeExceptions(Map<String, Double> baseline,
                                                    Map<String, Double> current,
                                                    double tolerance) {
        Set<String> exceptions = new HashSet<>();

        for (String name : baseline.keySet()) {
            double valueBaseline = baseline.get(name);
            if (!current.containsKey(name))
                continue;
            double valueCurrent = current.get(name);

            if (valueCurrent / valueBaseline - 1.0 <= tolerance)
                continue;

            exceptions.add(name);
        }

        return exceptions;
    }

    public static String encodeAllBenchmarks(Map<String, Double> results) {
        String text = "# Results Summary\n";
        for (String key : results.keySet()) {
            text += key + "," + results.get(key) + "\n";
        }
        return text;
    }

    public static void saveAllBenchmarks(Map<String, Double> results, String path) {
        String text = encodeAllBenchmarks(results);

        try {
            System.out.println("Saving to " + path);
            var writer = new PrintWriter(path);
            writer.print(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Double> loadAllBenchmarks(File file) {
        try {
            return loadAllBenchmarks(new FileInputStream(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, Double> loadAllBenchmarks(InputStream input) {
        Map<String, Double> results = new HashMap<>();
        BufferedReader buffered = new BufferedReader(new InputStreamReader(input));
        try {
            while (true) {
                String line = buffered.readLine();
                if (line == null)
                    break;

                if (line.startsWith("#") || line.isEmpty())
                    continue;

                // find the last comma, that's where it needs to split
                int lastIdx = line.lastIndexOf(',');
                if (lastIdx == -1)
                    throw new IOException("No comma found");

                String key = line.substring(0, lastIdx);
                String value = line.substring(lastIdx + 1);

                results.put(key, Double.parseDouble(value));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return results;
    }
}
