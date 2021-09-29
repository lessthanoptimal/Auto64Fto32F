/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import com.peterabeles.ProjectUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Runs all JMH benchmarks and saves the results plus exceptions.
 *
 * NOTE: This finds benchmarks by scanning the source code and not by using reflections to scan classes. This is
 * preferable since if a new module is added or re-named unless it's updated correctly in this build.gradle it
 * will silently fail by skipping those benchmarks.
 */
public class RunAllRuntimeBenchmarks extends JmhRunnerBase {
    public static String BENCHMARK_RESULTS_DIR = "runtime_regression";

    /** Manually specify which benchmarks to run based on class name */
    public List<String> userBenchmarkNames = new ArrayList<>();

    /**
     * The order in which benchmarks are run is randomized. This is intended to reduce systematic bias. E.g.
     * If a heavy task is run first it could heat up the computer causing it to throttle.
     */
    public boolean randomizedOrder = true;

    @Override protected void performBenchmarks() {
        String pathToMain = ProjectUtils.projectRelativePath(ProjectUtils.projectMain);
        List<String> benchmarkNames = new ArrayList<>();
        if (userBenchmarkNames.isEmpty()) {
            findBenchmarksByModule(pathToMain, benchmarkNames);
        } else {
            benchmarkNames.addAll(userBenchmarkNames);
        }
        // Randomize the order to reduce systematic bias if requested
        if (randomizedOrder) {
            Collections.shuffle(benchmarkNames);
        }
        for (String benchmarkName : benchmarkNames) {
            runBenchmark(benchmarkName, null);
        }
    }

    /**
     * Recursively searches each module by file path to find benchmarks then runs them
     */
    private void findBenchmarksByModule( String pathToMain, List<String> benchmarkNames ) {
        File[] moduleDirectories = new File(pathToMain).listFiles();
        Objects.requireNonNull(moduleDirectories);

        for (File module : moduleDirectories) {
            if (ProjectUtils.skipTest.skip(module))
                continue;

//			System.out.println("module "+module.getPath());
            File dirBenchmarks = new File(module, ProjectUtils.pathBenchmarks);

            if (!dirBenchmarks.exists())
                continue;

            recursiveFindBenchmarks(dirBenchmarks, dirBenchmarks, benchmarkNames);
        }
    }

    /**
     * Looks for benchmarks inside of this directory then checks all the children
     */
    public void recursiveFindBenchmarks( File root, File directory, List<String> benchmarkNames ) {
        File[] children = directory.listFiles();
        if (children == null)
            return;

        for (File f : children) {
            if (!f.isFile() || !f.getName().startsWith("Benchmark"))
                continue;

            Path relativeFile = root.toPath().relativize(f.toPath());
            String classPath = relativeFile.toString().replace(File.separatorChar, '.').replace(".java", "");

            // Load the class
            Class<?> c;
            try {
                c = Class.forName(classPath);
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                logException(e.getClass().getSimpleName() + " " + classPath);
                continue;
            }

            benchmarkNames.add(c.getName());
        }

        // Depth first search through directories
        for (File f : children) {
            if (!f.isDirectory())
                continue;
            recursiveFindBenchmarks(root, f, benchmarkNames);
        }
    }

    public static void main( String[] args ) {
        new RunAllRuntimeBenchmarks().process();
    }
}
