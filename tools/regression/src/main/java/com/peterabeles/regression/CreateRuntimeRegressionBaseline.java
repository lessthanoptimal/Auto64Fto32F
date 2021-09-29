/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import com.peterabeles.ProjectUtils;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Creates a new baseline results set by running the regression several times and selecting the best result
 * for each benchmark.
 *
 * @author Peter Abeles
 **/
public class CreateRuntimeRegressionBaseline {
    /**
     * The maximum number of times it will run a test and see if it's within tolerance
     */
    public int maxIterations = 10;

    /**
     * Path to output directory relative to project base
     */
    public String outputRelativePath = "tmp";

    /**
     * If true it won't run benchmarks again but will only combine existing results
     */
    public boolean combineOnly = false;

    /**
     * How long it took to run all the benchmarks in hours
     */
    private @Getter
    double timeBenchmarkHrs;
    /**
     * How long it took to combine all the results in milliseconds
     */
    private @Getter
    double timeCombineMS;

    public final RunAllRuntimeBenchmarks benchmark = new RunAllRuntimeBenchmarks();

    /**
     * Given the name give it the new results
     */
    @Getter
    private final Map<String, Double> nameToResults = new HashMap<>();

    /**
     * Converts the path to be relative to the project's root
     */
    public static ProjectRelativePath convertPath = (s) -> s;

    protected PrintStream logTiming;

    /**
     * Runs the benchmark set several times, finds the best times for each benchmark, save results
     */
    public void process() {
        nameToResults.clear();

        // Path to the main directory all results are saved inside of
        final File homeDirectory = new File(convertPath.convert(outputRelativePath));
        if (!homeDirectory.exists()) {
            if (!homeDirectory.mkdirs())
                System.err.println("Can't create home directory. " + homeDirectory.getPath());
        }

        try {
            logTiming = new PrintStream(new FileOutputStream(new File(homeDirectory, "time.txt")));

            // Save info about what is being computed
            RuntimeRegressionUtils.saveSystemInfo(homeDirectory, System.out);

            long time0 = System.currentTimeMillis();
            // Compute all the results. This will take a while
            if (!combineOnly) {
                for (int trial = 0; trial < maxIterations; trial++) {
                    long timeTrialStart = System.currentTimeMillis();
                    // Save the start time of each trial
                    logTiming.printf("trial%-2d  %s", trial, ProjectUtils.formatDate(new Date()));
                    logTiming.flush();
                    benchmark.outputRelativePath = outputRelativePath + "/" + "trial" + trial;
                    benchmark.process();
                    double elapsedHrs = (System.currentTimeMillis() - timeTrialStart) / (1000.0 * 60 * 60);
                    logTiming.printf(", %5.1f hrs\n", elapsedHrs);
                    logTiming.flush();
                    System.out.print("\n\nFinished Trial " + trial + "\n\n");
                }
            }
            long time1 = System.currentTimeMillis();
            timeBenchmarkHrs = (time1 - time0) / (double) (1000 * 60 * 60);

            // Load results and for each benchmark find the best result across all the trials
            combineTrialResults(homeDirectory);
            long time2 = System.currentTimeMillis();
            timeCombineMS = time2 - time1;

            // Save the results
            File file = new File(ProjectUtils.projectRelativePath(outputRelativePath),
                    RuntimeRegressionMasterApp.ALL_BENCHMARKS_FILE);
            RuntimeRegressionUtils.saveAllBenchmarks(nameToResults, file.getPath());

            // Save information on how long it took to compute
            logTiming.println();
            logTiming.printf("Benchmarks:  %.2f hrs\n", timeBenchmarkHrs);
            logTiming.printf("Combine:     %.2f ms\n", timeCombineMS);
            logTiming.println();
            logTiming.println("Finished:    " + ProjectUtils.formatDate(new Date()));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            logTiming.close();
        }
    }

    private void combineTrialResults(File homeDirectory) throws IOException {
        // List of all directories containing the results
        String[] directories = homeDirectory.list((current, name) ->
                new File(current, name).isDirectory() && name.startsWith("trial"));

        System.out.println("Matching directories=" + directories.length);

        for (int idx = 0; idx < directories.length; idx++) {
            File trialDir = new File(homeDirectory, directories[idx]);
            Map<String, Double> results = RuntimeRegressionUtils.loadJmhResults(trialDir);
            for (var e : results.entrySet()) {
                if (nameToResults.containsKey(e.getKey())) {
                    double current = nameToResults.get(e.getKey());
                    double found = e.getValue();
                    if (found < current)
                        nameToResults.put(e.getKey(), found);
                } else {
                    nameToResults.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    @FunctionalInterface
    public interface ProjectRelativePath {
        String convert(String path);
    }
}
