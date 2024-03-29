/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.autocode;

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
