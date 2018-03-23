package com.peterabeles.auto64fto32f;

import java.io.File;

/**
 * Traverses through a directory tree recursively converting code from double into floats.
 *
 * @author Peter Abeles
 */
public class RecursiveConvert {

	private ConvertFile32From64 converter;

	private String suffice64 = "_F64";
	private String suffice32 = "_F32";


	public RecursiveConvert(ConvertFile32From64 converter ) {
		this.converter = converter;
	}

	public void setSuffice( String suffice64 , String suffice32 ) {
		this.suffice64 = suffice64;
		this.suffice32 = suffice32;
	}

	public void process( File inputDirectory ) {
		process(inputDirectory,inputDirectory);
	}

	public void process( File inputDirectory , File outputDirectory ) {
		if( !inputDirectory.isDirectory() ) {
			throw new IllegalArgumentException( "Not a directory. "+inputDirectory.getPath() );
		}
		if( !outputDirectory.exists() ) {
			if( !outputDirectory.mkdirs() ) {
				throw new RuntimeException("Can't create output directory. "+outputDirectory.getPath());
			}
		} if( !outputDirectory.isDirectory() ) {
			throw new IllegalArgumentException( "Output isn't a directory. "+outputDirectory.getPath() );
		}

		System.out.println( "---- Directory " + inputDirectory );

		// examine all the files in the directory first
		File[] files = inputDirectory.listFiles();
		if( files == null )
			return;

		int length64 = suffice64.length()+5;

		for( File f : files ) {
			String n = f.getName();
			if( n.endsWith( suffice64+".java" ) ) {
				n = n.substring(0, n.length() - length64) + suffice32+".java";
				try {
					System.out.println( "Generating " + n );
					converter.process(f,new File(outputDirectory,n));
				} catch( Exception e ) {
					System.out.println("\n\n\nCode generation failed!");
					e.printStackTrace();
					System.out.flush();
					System.err.flush();
					throw new RuntimeException( e );
				}
			}
		}

		for( File f : files ) {
			if( f.isDirectory() && !f.isHidden() ) {
				process( f , new File(outputDirectory,f.getName()));
			}
		}
	}
}
