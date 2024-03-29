/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import com.peterabeles.ProjectUtils;
import com.peterabeles.regression.ParseBenchmarkCsv.Parameter;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Common class for running JMH benchmarks and logging errors and other information
 */
public abstract class JmhRunnerBase {

    /** A benchmark will timeout after this many minutes by default */
    public static long DEFAULT_TIMEOUT_MIN = 3;

    /**
     * How long a single JMH test has before it times out. This should be kept fairly small since this is designed
     * to catch regressions not evaluate performance on large datasets
     */
    public long timeoutMin = DEFAULT_TIMEOUT_MIN;

    /** Path to output directory relative to project base */
    public String outputRelativePath = "tmp";

    /** Sub directory to put log files into */
    public String logDirectory = "logs";

    // Directory it saved results too
    public File outputDirectory;

    // Print streams to different files
    protected PrintStream logExceptions;
    protected PrintStream logRuntimes;
    protected PrintStream logStderr;

    public void process() {
        PrintStream stderr = System.err;
        logExceptions = null;
        logRuntimes = null;
        logStderr = null;
        try {
            long time0 = System.currentTimeMillis();
            outputDirectory = new File(ProjectUtils.projectRelativePath(outputRelativePath));
            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                throw new UncheckedIOException(new IOException("Failed to mkdirs output: " + outputDirectory.getPath()));
            }
            System.out.println("Output Directory: " + outputDirectory.getAbsolutePath());
            try {
                File logs = new File(outputDirectory, logDirectory);
                if (!logs.exists() && !logs.mkdirs())
                    throw new UncheckedIOException(new IOException("Failed to create log directory. "+logs.getPath()));

                logExceptions = new PrintStream(new File(logs, "exceptions.txt"));
                logRuntimes = new PrintStream(new File(logs, "runtime.txt"));
                // print stderr to console and save to a file
                logStderr = new PrintStream(new File(logs, "stderr.txt"));
                System.setErr(new PrintStream(new RunAllRuntimeBenchmarks.MirrorStream(stderr, logStderr)));
                logRuntimes.println("# How long each benchmark took\n");
                logRuntimes.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            performBenchmarks();

            // Print out the total time the benchmark took
            long time1 = System.currentTimeMillis();
            long totalTimeMS = time1 - time0;
            int seconds = (int)(totalTimeMS/1000)%60;
            int minutes = (int)((totalTimeMS/(1000*60))%60);
            int hours = (int)((totalTimeMS/(1000*60*60))%24);
            logRuntimes.printf("\nTotal Elapsed Time is %2d:%2d:%2d\n", hours, minutes, seconds);
            System.out.printf("\nTotal Elapsed Time is %2d:%2d:%2d\n", hours, minutes, seconds);
        } catch (Exception e ) {
            e.printStackTrace(logStderr);
        } finally {
            // Stop mirroring stderr
            System.setErr(stderr);

            // Close all log files
            if (logStderr!=null) logStderr.close();
            if (logExceptions!=null) logExceptions.close();
            if (logRuntimes!=null) logRuntimes.close();

            System.out.println("Done!");
        }
    }

    protected abstract void performBenchmarks() throws IOException;

    /**
     * Runs the benchmark and saves the results to disk
     */
    public void runBenchmark( String benchmarkName, @Nullable List<Parameter> parameters ) {
        System.out.println("Running " + benchmarkName);
        // Shorten the name to have it fit on a single line
        String[] words = benchmarkName.split("\\.");
        String truncated = words.length >= 2 ? words[words.length-2] + "." + words[words.length-1] : benchmarkName;
        logRuntimes.printf("%-80s ", truncated);
        logRuntimes.flush();

        long time0 = System.currentTimeMillis();
        OptionsBuilder opt = new OptionsBuilder();

        if (parameters != null) {
            for (Parameter p : parameters) {
                opt.param(p.name, p.value);
            }
        }

        // The \b is used to ensure that you just match a single benchmark class. Without it Foo and FooMoo would be
        // run when you just want to run Foo
        opt.include("\\b" + benchmarkName + "\\b")
                // Using average since it seems to have less loss of precision across a range of speeds
                .mode(Mode.AverageTime)
                // Using nanoseconds since it seems to have less loss of precision for very fast and slow operations
                .timeUnit(TimeUnit.NANOSECONDS)
                // The number of times the benchmark is run  is basically at the bare minimum to speed everything up.
                // Otherwise it would take an excessive amount of time
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(3)
                .timeout(TimeValue.minutes(timeoutMin))
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .resultFormat(ResultFormatType.CSV)
                .result(outputDirectory.getPath() + "/" + benchmarkName + ".csv")
                .build();

        try {
            Runner runner = new Runner(opt);
            runner.run();
            // There is a weird halting issue after it runs for a while on one machine. This is an attempt to see
            // if it's GC related.
            System.out.println("System GC run = " + runner.runSystemGC());
        } catch (RunnerException e) {
            e.printStackTrace();
            logException("Exception running " + benchmarkName + " : " + e.getMessage());
        }
        long time1 = System.currentTimeMillis();
        logStderr.flush();
        logRuntimes.printf("%7.2f (min)\n", (time1 - time0)/(60_000.0));
        logRuntimes.flush();
    }

    protected void logException( String message ) {
        logExceptions.println(message);
        logExceptions.flush();
    }

    /** Copies the stream into two streams */
    public static class MirrorStream extends OutputStream {
        //@formatter:off
        PrintStream outA, outB;
        public MirrorStream( PrintStream outA, PrintStream outB ) {this.outA = outA; this.outB = outB;}
        @Override public void write( int b ) {outA.write(b); outB.write(b);}
        @Override public void write( byte[] b, int off, int len ) {outA.write(b, off, len); outB.write(b, off, len);}
        @Override public void flush() {outA.flush(); outB.flush();}
        @Override public void close() {outA.close(); outB.close();}
        //@formatter:on
    }
}
