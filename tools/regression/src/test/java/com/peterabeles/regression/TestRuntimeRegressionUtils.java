/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestRuntimeRegressionUtils {
    @Test void encode_decode_allBenchmarks() {
        Map<String, Double> expected = new HashMap<>();
        expected.put("asfd.asfd.sfdf,foo:100,bar:12.83", 99.0);

        String encoded = RuntimeRegressionUtils.encodeAllBenchmarks(expected);

        Map<String, Double> found = RuntimeRegressionUtils.loadAllBenchmarks(
                new ByteArrayInputStream(encoded.getBytes(StandardCharsets.UTF_8)));

        assertEquals(expected.size(), found.size());

        for (var e : expected.entrySet()) {
            assertEquals(e.getValue(), found.get(e.getKey()));
        }
    }
}
