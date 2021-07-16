package com.peterabeles.auto64fto32f;

/**
 * Specify which language is being converted
 *
 * @author Peter Abeles
 */
public enum Language {

    JAVA("java"),
    KOTLIN("kt");

    private String suffix;

    Language( String suffix ) {
        this.suffix = suffix;
    }

    public String suffix() {
        return this.suffix;
    }
}
