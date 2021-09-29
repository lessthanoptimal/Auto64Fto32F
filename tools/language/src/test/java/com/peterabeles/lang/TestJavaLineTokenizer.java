/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.lang;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Abeles
 */
public class TestJavaLineTokenizer {
	@Test void wordTokens() {
		var alg = new JavaLineTokenizer();

		alg.parse("int hi = there ;");
		checkTokens(alg.stringTokens, "int", "hi", "=", "there", ";");

		alg.parse("int hi=there;");
		checkTokens(alg.stringTokens, "int", "hi", "=", "there", ";");

		alg.parse("for( var i : list ) {");
		checkTokens(alg.stringTokens, "for", "(", "var", "i", ":", "list", ")", "{");

		alg.parse("Moo foo=new Boo()");
		checkTokens(alg.stringTokens, "Moo", "foo", "=", "new", "Boo", "()");

		alg.parse("Moo foo_moo");
		checkTokens(alg.stringTokens, "Moo", "foo_moo");

		alg.parse("\"for this is\"");
		checkTokens(alg.stringTokens, "\"for this is\"");
	}

	@Test void stringTokenEscape() {
		var alg = new JavaLineTokenizer();
		alg.parse("\"for \\\"this is\"");
		checkTokens(alg.stringTokens, "\"for \"this is\"");
		alg.parse("\"for \\\\\"\\\"this is\"");
		checkTokens(alg.stringTokens, "\"for \\\"", "\\", "\"this is\"");
	}

	void checkTokens( List<String> found, String... expected ) {
		assertEquals(expected.length, found.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], found.get(i));
		}
	}
}
