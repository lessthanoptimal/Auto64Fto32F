Auto64Fto32F:  A minimalist library for converting double (64-bit) numerical Java code into float (32-bit).

Code written for 64-bit floats (i.e. doubles) is automatically converted into float 32-bit code by performing a user specified sequence of keyword replacements.  No special template language is required.  However, a special coding style is required.

1) Recusively search input directory for files ending in "_F64.java"
2) Create a new file with the ending "_F32.java"
3) Perform REGEX/keyword swap and save results to output file

The suffice it searches for can be change from '_F64' into other valid string.

## Transformation Process

The process of converting double math into float math is fairly simple not very 'intelligent'.

1. Copies over the copyright at the start of the file unmodified.

   This is accomplished by simply copying everything until a line with 4 characters and line[2] == '/' is encountered.
   
2. Break the remainder of the document into words and white space.

   White space is written to the output unmodified
   
3. Words are transformed by applying three separate sets of text replacements in order.
  * REGEX matching:  If part of the word matches a regex that portion of the word is replaced with replacement text
  * Starts with matching:  If the word begins with the specified text it is replaced with the replacement text.
  * double replacement: An internally specified regex is used to detect doubles (e.g. 45.0 or 4.) and convert it into a float
  * After REGEX matching: Another round of regex matching.  Can be used to undo an earlier modification

See next section below for how to programmically specify each type of text replacement.

## Default Replacements

In the constructor of ConvertFile32From64 there is a flag to use the default replacements.  If set to true this will be added automatically.

```java
replacePattern("/\\*\\*/double", "FIXED_DOUBLE");
replacePattern("double", "float");
replacePattern("Double", "Float");
replacePattern("_F64", "_F32");

replaceStartsWith("Math.", "(float)Math.");
replaceStartsWith("-Math.", "(float)-Math.");

replacePatternAfter("FIXED_DOUBLE", "/\\*\\*/double");
```

## Code Examples

The following is an example of code which is to be transformed.  It's Java code, just with more restrictive systax and some extra "pointless" comments to provide hints.

Original Code:
```java
/**/double moo = /**/Math.cos(0.5);
double foo = 1.6*bar + Math.exp(6) + (double)moo;
```
Converted Code:
```java
/**/double moo = /**/Math.cos(0.5);
float foo = 1.6f*bar + (float)Math.exp(6) + (float)moo;
```

The sequence ```/**/double``` is searched for an ignored, thus it will remain a double in the floating point code.  ```/**/Math``` will not be typecasted because there is no space in front of Math.  More on that Math issue later.  

To avoid a compile time exception in the float code the variable 'moo' needs to be converted into a float. That is accomplished through a gratuitous (double) typecast.


## Gotchas
```java
double foo = 1.6*bar +Math.exp(6);
```
```java
float foo = 1.6f*bar +Math.exp(6);
```
Notice how the converted code will generate a compile time error since Math.exp() returns a double?  Math operators require a space in front.  While annoying this easily handles endge cases that a straight forward keyword swap can't handle.


## Author

This software is written by Peter Abeles and has been released as public domain software.  See UNLICENSE