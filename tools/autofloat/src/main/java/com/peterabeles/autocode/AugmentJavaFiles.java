/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.autocode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Augments Java files to add code which did not originally exist. Currently this is only done to mark files as
 * auto generated. Almost all other modifications are key word swaps.
 *
 * @author Peter Abeles
 */
public class AugmentJavaFiles {
    /**
     * Augments the input stream and returns a new stream with the augmentations
     *
     * @param stream Stream after being converted to 32
     * @param originalFileName Original name of file used to generate it
     * @return New stream after being augmented
     */
    public InputStream augment( InputStream stream , String originalFileName) throws IOException {
        // Copy the entire stream into memory to make random access easier
        String content = new Scanner(stream).useDelimiter("\\A").next();

        StringBuilder builder = new StringBuilder(content.length());

        String packageName = extractPackageName(content);
        if( packageName.length() != 0 )
            packageName += ".";

        // Place the import statement just before the first "import"
        int indexOfImport = findIndexWhereKeywordStartsLine(content,"import");
        if( indexOfImport == -1 )
            indexOfImport = findIndexAfterPackage(content); // no imports so stick it after package
        builder.append(content,0,indexOfImport);
        builder.append("import javax.annotation.Generated;\n");

        int indexOfClass = findLineWhereClassIsDefined(content,indexOfImport);

        // Make sure it already isn't marked as auto generated. We do this now because 99% of the time it will
        // save time by avoiding searching the entire file for the word "generated"
        if( content.substring(0, indexOfClass).contains("javax.annotation.Generated")) {
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        builder.append(content,indexOfImport,indexOfClass);
        builder.append("@Generated(\""+packageName+originalFileName+"\")\n");
        builder.append(content,indexOfClass,content.length());

        return new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String extractPackageName(String content) throws IOException {
        int idx0 = findIndexWhereKeywordStartsLine(content,"package");
        if( idx0 == -1 )
            return "";
        idx0 += "package".length()+1;
        int idx1 = idx0;
        while( idx1 < content.length() ) {
            if( content.charAt(idx1) == ';') {
                return content.substring(idx0,idx1);
            }
            idx1++;
        }
        throw new IOException("Couldn't find valid package name");
    }

    private int findIndexWhereKeywordStartsLine(String content, String keyword) throws IOException {
        int loc = 0;
        while( true ) {
            loc = content.indexOf(keyword,loc);
            if( loc == -1 )
                return -1;
            if( loc == 0 )
                break;
            // see if import starts the time
            if(isNewLine(content.charAt(loc-1)) )
                break;
            loc += keyword.length();
        }
        return loc;
    }

    private int findIndexAfterPackage(String content) throws IOException {
        int index = findIndexWhereKeywordStartsLine(content,"package");
        if( index == -1 )
            return 0;
        boolean foundNewLine = false;
        while( index < content.length() ) {
            if( foundNewLine ) {
                if( !isNewLine(content.charAt(index)) )
                    return index;
            } else if( isNewLine(content.charAt(index))) {
                foundNewLine = true;
            }
            index++;
        }
        return index;
    }

    private int findLineWhereClassIsDefined( String content, int startIndex ) throws IOException {
        int searchClass = content.indexOf("class",startIndex);
        int searchInterface = content.indexOf("interface",startIndex);

        int nextSearch = -1;
        while( true ) {
            if( nextSearch == 1 ) {
                searchClass = content.indexOf("class",searchClass+5);
            } else if( nextSearch == 2 ) {
                searchInterface = content.indexOf("interface", searchInterface + 9);
            }
            if( searchClass < 0 && searchInterface < 0 )
                throw new IOException("Couldn't find valid 'class' or 'interface'");
            String keyword;
            int indexInContent;
            if( searchInterface < 0 || (searchClass>=0 && searchClass<searchInterface)) {
                keyword = "class";
                indexInContent = searchClass;
                nextSearch = 1;
            } else {
                keyword = "interface";
                indexInContent = searchInterface;
                nextSearch = 2;
            }
            String line = extractLine(content,indexInContent);
            String[] words = line.split("\\s+");

            // make sure 'class' is a word and assume that 'class' and the class name are on the same line
            String className = null;
            for (int i = 0; i < words.length-1; i++) {
                if( !words[i].equals(keyword))
                    continue;
                className = words[i+1];
                break;
            }
            if( className == null ) {
                continue;
            }

            int locOfKeyWord = line.indexOf(keyword);
            int locOfSlash = line.indexOf("/");
            int locOfStar = line.indexOf("*");

            // make a bunch of simplifying assumptions about comments and what's legal in java...
            if( locOfSlash != -1 && locOfSlash < locOfKeyWord )
                continue;
            if( locOfStar != -1 && locOfStar < locOfKeyWord )  // you could do /**/
                continue;
            return indexInContent - locOfKeyWord;
        }
    }

    private static String extractLine( String content , int seedIndex ) {
        int idx0 = Math.max(0,seedIndex-1);
        while( idx0 > 0 ) {
            if( isNewLine(content.charAt(idx0)) ) {
                idx0 += 1;
                break;
            }
            idx0--;
        }
        int idx1 = Math.min(content.length(),seedIndex+1);
        while( idx1 < content.length() ) {
            if( isNewLine(content.charAt(idx1)) ) {
                break;
            }
            idx1++;
        }
        return content.substring(idx0,idx1);
    }

    private static boolean isNewLine( char c ) {
        return c == '\n' || c == '\r';
    }
}
