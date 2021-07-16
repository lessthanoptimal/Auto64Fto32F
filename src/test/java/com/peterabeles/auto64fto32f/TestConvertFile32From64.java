package com.peterabeles.auto64fto32f;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestConvertFile32From64 {

    /**
     * Adds a custom ignore toa file and sees if it is handled correctly
     */
    @Test void customIgnore() throws IOException {
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
