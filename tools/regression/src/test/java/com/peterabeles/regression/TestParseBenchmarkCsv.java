/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestParseBenchmarkCsv {
    String TEST_CASE_0 =
            """
                    "Benchmark","Mode","Threads","Samples","Score","Score Error (99.9%)","Unit","Param: blockLength","Param: size"
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.mult","thrpt",1,3,0.002967,0.000023,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multMinus","thrpt",1,3,0.002888,0.000034,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multMinusTransA","thrpt",1,3,0.002515,0.000022,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multPlus","thrpt",1,3,0.002991,0.000133,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multPlusTransA","thrpt",1,3,0.002950,0.000232,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multTransA","thrpt",1,3,0.002865,0.000091,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multTransB","thrpt",1,3,0.001957,0.000147,"ops/ms",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.mult","avgt",1,3,335.330283,19.034948,"ms/op",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multMinus","avgt",1,3,347.177507,21.216441,"ms/op",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multMinusTransA","avgt",1,3,385.002814,23.841388,"ms/op",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multPlus","avgt",1,3,335.462057,20.983786,"ms/op",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multPlusTransA","avgt",1,3,344.023984,38.291692,"ms/op",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multTransA","avgt",1,3,338.739166,11.332490,"ms/op",80,1000
                    "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multTransB","avgt",1,3,503.252428,8.821169,"ms/op",80,1000
                    """;

    @Test
    void case0() throws IOException {
        ParseBenchmarkCsv parser = new ParseBenchmarkCsv();
        parser.parse(new ByteArrayInputStream(TEST_CASE_0.getBytes(StandardCharsets.UTF_8)));

        // See if there's the expected number
        assertEquals(7, parser.results.size());

        // Pick one and see if it has the expected values
        ParseBenchmarkCsv.Result r = parser.mapResults.get(
                "org.ejml.dense.block.BenchmarkMatrixMult_DDRB.multMinus,blockLength:80,size:1000");
        assertNotNull(r);
        assertEquals(0.002888, r.ops_per_ms, 7.5e-8);
        assertEquals(347.177507, r.ms_per_op, 7.5e-8);
    }
}
