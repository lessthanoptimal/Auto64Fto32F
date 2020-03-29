package com.peterabeles.auto64fto32f;

/**
 * Specify which language is being converted
 *
 * @author Peter Abeles
 */
public enum Language {

    JAVA("java"),
    KOTLIN("kt");

    private int length;
    private String suffix;

    Language( String suffix ) {
        this.suffix = suffix;
        this.length = suffix.length()+1;
    }

    public String suffix() {
        return this.suffix;
    }

    public int length() {
        return length;
    }

}
