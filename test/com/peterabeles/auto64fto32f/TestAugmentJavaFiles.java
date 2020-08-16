package com.peterabeles.auto64fto32f;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestAugmentJavaFiles {
    @Test
    public void standardClass() throws IOException {
        String input = "package dummy;\n" +
                "import foobar;\n" +
                "/** Going to mention the word class here to mess things up */\n" +
                "public class DummyCode_F64 {}\n";

        String expected = "package dummy;\n" +
                "import javax.annotation.Generated;\n" +
                "import foobar;\n" +
                "/** Going to mention the word class here to mess things up */\n" +
                "@Generated(\"dummy.DummyCode_F64\")\n" +
                "public class DummyCode_F64 {}\n";

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(expected,found);
    }

    @Test
    public void standardInterface() throws IOException {
        String input = "package dummy;\n" +
                "import foobar;\n" +
                "/** Going to mention the word class here to mess things up */\n" +
                "public interface DummyCode_F64 {}\n";

        String expected = "package dummy;\n" +
                "import javax.annotation.Generated;\n" +
                "import foobar;\n" +
                "/** Going to mention the word class here to mess things up */\n" +
                "@Generated(\"dummy.DummyCode_F64\")\n" +
                "public interface DummyCode_F64 {}\n";

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(expected,found);
    }

    @Test
    public void alreadyGenerated() throws IOException {
        String input = "package dummy;\n" +
                "import javax.annotation.Generated;\n" +
                "import foobar;\n" +
                "/** Going to mention the word class here to mess things up */\n" +
                "@Generated(\"dummy.DummyCode_F64\")\n" +
                "public class DummyCode_F64 {}\n";

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(input,found);
    }

    @Test
    public void withoutImport() throws IOException {
        String input = "package dummy;\n" +
                "\n" +
                "/**\n" +
                " * Going to mention the word class here to mess things up\n" +
                " */\n" +
                "public class DummyCode_F64 {\n"+
                "}\n";

        String expected = "package dummy;\n" +
                "\n" +
                "import javax.annotation.Generated;\n" +
                "/**\n" +
                " * Going to mention the word class here to mess things up\n" +
                " */\n" +
                "@Generated(\"dummy.DummyCode_F64\")\n" +
                "public class DummyCode_F64 {\n" +
                "}\n";

        var alg = new AugmentJavaFiles();
        InputStream foundStream = alg.augment(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),"DummyCode_F64");
        String found = new Scanner(foundStream).useDelimiter("\\A").next();

        assertEquals(expected,found);
    }
}
