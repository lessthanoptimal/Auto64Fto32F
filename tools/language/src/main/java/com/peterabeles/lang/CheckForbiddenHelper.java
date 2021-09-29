/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.lang;

/**
 * Helper functions for adding rules
 *
 * @author Peter Abeles
 */
public class CheckForbiddenHelper {
	/**
	 * Checks to see if a call function has been made. It works by searching for the name then seeing if the
	 * previous non-whitespace character is a ".".
	 */
	public static void addForbiddenFunction( CheckForbiddenLanguage checker,
											 String functionName, String reason ) {
		CheckForbiddenLanguage.ConditionalRule rule = (line, words) -> {
			for (int wordIdx = 1; wordIdx < words.size(); wordIdx++) {
				if (!words.get(wordIdx).equals(functionName))
					continue;

				if (words.get(wordIdx-1).equals("."))
					return false;
			}
			return true;
		};
		checker.addConditional("function_" + functionName, functionName, reason, rule);
	}

	/**
	 * A simple check that sees if var is only used when the type is shown via "new". There are ways to trick this
	 * function into thinking the type is explicitly shown when it is not.
	 *
	 * @param allowForEach If true then var inside of for each loops will be allowed
	 */
	public static void forbidNonExplicitVar(CheckForbiddenLanguage checker, boolean allowForEach ) {
		CheckForbiddenLanguage.ConditionalRule rule = (line, words) -> {
			for (int i = 0; i < words.size()-3; i++) {
				if (!words.get(i).equals("var"))
					continue;

				if (allowForEach && i >= 2 && words.get(i-2).equals("for")) {
					continue;
				}

				// see if it's used as a variable name
				if (words.get(i+1).equals("=") || words.get(i+1).equals(".") || words.get(i+1).equals(","))
					continue;

				// var moo = new Foo()
				if (!words.get(i+3).equals("new"))
					return false;
			}
			return true;
		};
		checker.addConditional("explicit_var", "var",
				"Auto type inference with var reduces code readability, maintainability, and hide mistakes during refactoring", rule);
	}

	/**
	 * Looks for "for-each" style loops
	 */
	public static void forbidForEach( CheckForbiddenLanguage checker ) {
		CheckForbiddenLanguage.ConditionalRule rule = (line, words) -> {
			for (int wordIdx = 0; wordIdx < words.size()-4; wordIdx++) {
				if (!words.get(wordIdx).equals("for"))
					continue;

				// for ( var a : list ) {
				//  0  1  2  3 4   5  6 7
				if (words.get(wordIdx+4).equals(":"))
					return false;
			}
			return true;
		};
		checker.addConditional("for_each", "for",
				"For highly optimized code that requires a constant runtime GC calls must be avoided. "+
				"for-each style loops create a temporary iterator which can kill performance.", rule);
	}
}
