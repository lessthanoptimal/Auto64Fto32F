/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles;

/**
 * Version information about the project
 *
 * @author Peter Abeles
 */
public class LibrarySourceInfo {
    public String projectName;
    public String version;
    public String gitSha;
    public String gitDate;

    public void checkConfigured() {
        if (projectName == null || version == null || gitSha == null || gitDate == null)
            throw new RuntimeException("You must configure 'ProjectUtils.libraryInfo'");
    }
}
