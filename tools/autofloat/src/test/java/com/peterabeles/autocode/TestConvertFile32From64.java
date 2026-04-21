/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.autocode;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestConvertFile32From64 {

    @Test
    void handleQuoteTokens() {
        var input = new ArrayList<String>();
        input.add("asdf");
        input.add(" ");
        input.add("asdf\"");
        input.add("asdf\"roodf\\\"sdf\"");
        input.add("\"roodf\"");
        input.add("sdf\"");
        input.add("\"");

        var found = ConvertFile32From64.handleQuoteTokens(input);

        var expected = new String[]{"asdf", " ", "asdf", "\"", "asdf", "\"", "roodf\\\"sdf", "\"", "\"", "roodf", "\"", "sdf", "\"", "\""};

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], found.get(i));
        }
        assertEquals(expected.length, found.size());
    }

    /**
     * Adds a custom ignore toa file and sees if it is handled correctly
     */
    @Test
    void customIgnore() throws IOException {
        String input = """
                package dummy;
                
                import foo.bar.Stuff_F64;
                import foo.bar.Moo_F64;
                
                //CUSTOM ignore Stuff_F64
                
                public class DummyCode_F64 {
                \tStuff_F64 foo;
                \tMoo_F64 boo;
                }
                """;

        String expected = """
                package dummy;
                
                import foo.bar.Stuff_F64;
                import foo.bar.Moo_F32;
                
                //CUSTOM ignore Stuff_F64
                
                public class DummyCode_F32 {
                \tStuff_F64 foo;
                \tMoo_F32 boo;
                }
                """;

        File fileInput = new File("DummyCode_F64.java");
        File fileExpected = new File("DummyCode_F32.java");

        try {
            var alg = new ConvertFile32From64(true);

            FileUtils.write(fileInput, input, Charset.defaultCharset());

            alg.process(fileInput, fileExpected);
            String found = FileUtils.readFileToString(fileExpected, StandardCharsets.UTF_8);

            System.out.println(expected);
            System.out.println("---------------------");
            System.out.println(found);
            assertEquals(expected, found);
        } finally {
            fileInput.delete();
            fileExpected.delete();
        }
    }

    @Test
    void stringsAreIgnored() throws IOException {
        String input = """
                /** " do nothing here */
                // " this should be ignored "
                public class DummyCode_F64 {
                \tString foo = "Let's ignore Stuff_F64 1.2345 df";
                \tString moo = "escape\\"Stuff_F64 1.2.34 ";
                \tString moo = "singletokenstring";
                \t("Row and/or column out of range. "+row+" "+col);
                \tCode_F64+"word "+1.234;
                \tCode_F64+"wo\\"rd"+1.234;
                \tdouble lala = 1.2345;
                }
                """;

        String expected = """
                /** " do nothing here */
                // " this should be ignored "
                public class DummyCode_F32 {
                \tString foo = "Let's ignore Stuff_F64 1.2345 df";
                \tString moo = "escape\\"Stuff_F64 1.2.34 ";
                \tString moo = "singletokenstring";
                \t("Row and/or column out of range. "+row+" "+col);
                \tCode_F32+"word "+1.234f;
                \tCode_F32+"wo\\"rd"+1.234f;
                \tfloat lala = 1.2345f;
                }
                """;

        File fileInput = new File("DummyCode_F64.java");
        File fileExpected = new File("DummyCode_F32.java");

        try {
            var alg = new ConvertFile32From64(true);

            FileUtils.write(fileInput, input, Charset.defaultCharset());

            alg.process(fileInput, fileExpected);
            String found = FileUtils.readFileToString(fileExpected, StandardCharsets.UTF_8);

            assertEquals(expected, found);
        } finally {
            fileInput.delete();
            fileExpected.delete();
        }
    }

    File createFile(String text) {
        try {
            File file = File.createTempFile("auto", "txt");
            FileWriter out = new FileWriter(file);
            out.write(text);
            out.close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
