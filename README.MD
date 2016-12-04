Auto64Fto32F:  A minimalist library for converting double (64-bit) numerical Java code into float (32-bit).

Code written for 64-bit floats (i.e. doubles) is automatically converted into float 32-bit code by performing a user specified sequence of keyword replacements.  No special template language is required.  However, a special coding style is required.

1) Recusively search input directory for files ending in "_F64.java"
2) Create a new file with the ending "_F32.java"
3) Perform REGEX/keyword swap and save results to output file

The suffice it searches for can be change from '_F64' into other valid string.

## Usage Example

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

The sequence /**/double is searched for an ignored, thus it will remain a double in the floating point code.  /**/Math will not be type casted because there is no space infront of Math.  More on that Math issue later.  

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