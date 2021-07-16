package com.peterabeles.auto64fto32f;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestAugmentJavaFiles {
    @Test void standardClass() throws IOException {
        String input = """
                package dummy;
                import foobar;
                /** Going to mention the word class here to mess things up */
                public class DummyCode_F64 {}
                """;

        String expected = """
                package dummy;
                import javax.annotation.Generated;
                import foobar;
                /** Going to mention the word class here to mess things up */
                @Generated("dummy.DummyCode_F64")
                public class DummyCode_F64 {}
                """;

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(expected,found);
    }

    @Test void standardInterface() throws IOException {
        String input = """
                package dummy;
                import foobar;
                /** Going to mention the word class here to mess things up */
                public interface DummyCode_F64 {}
                """;

        String expected = """
                package dummy;
                import javax.annotation.Generated;
                import foobar;
                /** Going to mention the word class here to mess things up */
                @Generated("dummy.DummyCode_F64")
                public interface DummyCode_F64 {}
                """;

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(expected,found);
    }

    @Test
    public void alreadyGenerated() throws IOException {
        String input = """
                package dummy;
                import javax.annotation.Generated;
                import foobar;
                /** Going to mention the word class here to mess things up */
                @Generated("dummy.DummyCode_F64")
                public class DummyCode_F64 {}
                """;

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(input,found);
    }

    @Test
    public void withoutImport() throws IOException {
        String input = """
                package dummy;

                /**
                 * Going to mention the word class here to mess things up
                 */
                public class DummyCode_F64 {
                }
                """;

        String expected = """
                package dummy;

                import javax.annotation.Generated;
                /**
                 * Going to mention the word class here to mess things up
                 */
                @Generated("dummy.DummyCode_F64")
                public class DummyCode_F64 {
                }
                """;

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(expected,found);
    }
}
