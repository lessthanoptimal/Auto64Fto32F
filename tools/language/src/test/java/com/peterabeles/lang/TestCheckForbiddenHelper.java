/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestCheckForbiddenHelper {
	@Test void addForbiddenFunction() {
		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.addForbiddenFunction(alg, "forEach", "Because");
		CheckForbiddenHelper.addForbiddenFunction(alg, "pool", "Because");

		assertTrue(alg.process("void main(){\n\tfoo.moo();\n\n"));
		assertTrue(alg.process("void main(){\n\tfoo.AorEach();\n\n"));
		assertTrue(alg.process("void main(){\n\tforEach();\n\n"));

		assertFalse(alg.process("void main(){\n\tforEach;foo.forEach();\n\n"));
		assertFalse(alg.process("void main(){\n\tfoo.forEach();\n\n"));
		assertFalse(alg.process("foo.forEach();"));
		assertFalse(alg.process("void main(){\n\ta = b;foo.forEach();\n\n"));
		assertFalse(alg.process("void main(){\n\tfoo \t. forEach();\n\n"));
		assertEquals(4, alg.lineNumber);
		assertEquals(1, alg.failures.size());
		CheckForbiddenLanguage.Failure f = alg.getFailures().get(0);
		assertEquals(2, f.line);
		assertEquals("forEach", f.check.keyword);
	}

	@Test void forbidNonExplicitVar() {
		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.forbidNonExplicitVar(alg, false, false);

		assertTrue(alg.process("var a = new Foo()"));
		assertTrue(alg.process("var a =new Foo()"));
		assertTrue(alg.process("var a= new Foo()"));
		assertTrue(alg.process("var a=new Foo()"));
		assertTrue(alg.process("void main(){\n\tvar a=new Foo();\n} \n"));
		assertTrue(alg.process("void main(){\n\tb = s;var a=new Foo();\n} \n"));
		assertTrue(alg.process("Boo var = new Foo()")); // this is legal for legacy reasons
		assertTrue(alg.process("var.moo.stuff()"));
		assertTrue(alg.process("call(var,1.0)"));
		assertTrue(alg.process("var a = \nmoo.foo()")); // known false negative

		// Known false failures
		assertFalse(alg.process("var a = value ? new Foo() : new Foo()"));
		assertFalse(alg.process("var a = (Foo)a.getStuff()"));

		// Failures
		assertFalse(alg.process("for( var a : list )"));
		assertFalse(alg.process("var a = moo.foo()"));
		assertFalse(alg.process("var a= moo.foo()"));
		assertFalse(alg.process("var a =moo.foo()"));
		assertFalse(alg.process("var a=moo.foo()"));
		assertFalse(alg.process("var a=new Moo();var a=moo.foo()"));
		assertFalse(alg.process("dude = moo;var a = moo.foo()"));
		assertFalse(alg.process("var b=new Moo();var a = moo.foo()"));
		assertFalse(alg.process("var a= (int)moo.foo();var b = (Bar)199;"));
		assertEquals(1, alg.getFailures().size());
		assertEquals(1, alg.getFailures().get(0).line);
		assertEquals("var", alg.getFailures().get(0).check.keyword);

		assertFalse(alg.process("var b=new \n\nMoo();var a = moo.foo()\n\n"));
		assertEquals(1, alg.getFailures().size());
		assertEquals(3, alg.getFailures().get(0).line);
		assertEquals("var", alg.getFailures().get(0).check.keyword);
	}

	@Test void forbidNonExplicitVar_allowForEach() {
		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.forbidNonExplicitVar(alg, true, false);

		assertTrue(alg.process("for( var a : list )"));
		assertTrue(alg.process("for ( var a : list )"));
	}

	@Test void forbidNonExplicitVar_allowTypeCast() {
		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.forbidNonExplicitVar(alg, false, true);

		assertTrue(alg.process("var a= ( int ) moo.foo();"));
		assertTrue(alg.process("var a= ( int)moo.foo();"));
		assertTrue(alg.process("var a=(int) moo.foo();"));
		assertTrue(alg.process("var a =(int)moo.foo();"));
		assertTrue(alg.process("var a= (int)moo.foo();"));

		assertTrue(alg.process("var a= (int[])moo.foo();"));
		assertTrue(alg.process("var a= (int  [ ] )moo.foo();"));

	}

	@Test void forbidForEach() {
		var alg = new CheckForbiddenLanguage();
		CheckForbiddenHelper.forbidForEach(alg);

		assertFalse(alg.process("for( var a : list )"));
		assertFalse(alg.process("for( var a:list)"));
		assertFalse(alg.process("\nfor( var a:list)\n"));

		assertTrue(alg.process("for( int i = 0; i < 10; i++)"));
		assertTrue(alg.process("for( int i=0;i<10;i++)"));
	}
}
