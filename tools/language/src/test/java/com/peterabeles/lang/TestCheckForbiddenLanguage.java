/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestCheckForbiddenLanguage {

	@Test void disableAll() {
		String D = CheckForbiddenLanguage.DEFAULT_IDENTIFIER;
		String L = CheckForbiddenLanguage.DISABLE_ALL;
		String comment = "//" + D + " " + L;

		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");

		assertTrue(alg.process(comment + "\n\n 1\nfoo.forbidden()"));
	}

	@Test void disableCheck() {
		String D = CheckForbiddenLanguage.DEFAULT_IDENTIFIER;
		String L = CheckForbiddenLanguage.DISABLE_CHECK;
		String comment = "//" + D + " " + L + " function_forbidden";

		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");

		assertTrue(alg.process(comment + "\n\n 1\nfoo.forbidden()"));
		assertFalse(alg.process("foo.forbidden()\n" + comment + "\n\n 1\nfoo.forbidden()"));
	}

	@Test void disableCheck_DoesNotExist() {
		String D = CheckForbiddenLanguage.DEFAULT_IDENTIFIER;
		String L = CheckForbiddenLanguage.DISABLE_CHECK;
		String comment = "//" + D + " " + L + " function_yolo";

		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");

		assertFalse(alg.process(comment + "\n\n 1\nfoo.asdf()"));
		assertSame(CheckForbiddenLanguage.MALFORMED_COMMAND, alg.getFailures().get(0).check);
	}

	@Test void ignoreBelow() {
		// create a simple shorthand because of how verbose it would be otherwise
		String D = CheckForbiddenLanguage.DEFAULT_IDENTIFIER;
		String L = CheckForbiddenLanguage.IGNORE_BELOW;
		String comment = "//" + D + " " + L;

		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");
		assertTrue(alg.process(comment + " 1\nfoo.forbidden()"));
		assertTrue(alg.process(comment + " 1\nfoo.forbidden()\n"));
		assertFalse(alg.process(comment + " 1\n\nfoo.forbidden()"));
		assertEquals(2, alg.getFailures().get(0).line);
		assertEquals(3, alg.getFailures().get(1).line);
		assertFalse(alg.process(comment + " 0\nfoo.forbidden()"));
		assertEquals(1, alg.getFailures().get(0).line);
		assertEquals(2, alg.getFailures().get(1).line);
		assertFalse(alg.process(comment + " 0\nfoo.forbidden()\n"));
		assertEquals(1, alg.getFailures().get(0).line);
		assertEquals(2, alg.getFailures().get(1).line);
	}

	/**
	 * If there's a disable comment AND there is nothing to disable, that should be an error too
	 */
	@Test void ignoreBelow_error_if_no_error() {
		// create a simple shorthand because of how verbose it would be otherwise
		String D = CheckForbiddenLanguage.DEFAULT_IDENTIFIER;
		String L = CheckForbiddenLanguage.IGNORE_BELOW;
		String comment = "//" + D + " " + L;

		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");
		assertFalse(alg.process(comment + " 1\n\n\n"));
	}

	@Test void ignoreLine() {
		// create a simple shorthand because of how verbose it would be otherwise
		String D = CheckForbiddenLanguage.DEFAULT_IDENTIFIER;
		String L = CheckForbiddenLanguage.IGNORE_LINE;
		String comment = "//" + D + " " + L;

		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");
		assertTrue(alg.process("foo.forbidden() " + comment));
		assertTrue(alg.process("foo.forbidden()" + comment));
		assertTrue(alg.process("\nfoo.forbidden()" + comment));
		assertTrue(alg.process("foo.forbidden()" + comment + "\n"));
		assertTrue(alg.process("foo.forbidden()" + comment + "\n\n"));

		// nothing is ignored
		assertFalse(alg.process("foo.asdf()" + comment));
	}

	/** If there's a multi line comment in the middle of a line it is removed and converted into a space */
	@Test void multiLineCommentConvertedToSpace() {
		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forbidden", "Because");

		assertFalse(alg.process("foo./*asdf*/forbidden()"));
	}
}
