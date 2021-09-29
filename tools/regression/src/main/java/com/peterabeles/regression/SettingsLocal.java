/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

/*
 * Auto64to32F is released to Public Domain or MIT License. Either maybe used.
 */

package com.peterabeles.regression;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Local settings specific to the machine the regression tests is being run on. This is read once when the application
 * launches. It must never be added to Git.
 *
 * @author Peter Abeles
 */
public class SettingsLocal {
    public static final String FILE_NAME = "settings_local.yaml";
    public static final String DEFAULT_MACHINE_NAME = "Default";

    public static final String KEY_VERSION = "version";
    public static final String KEY_MACHINE_NAME = "machine_name";

    /** Name used when a unique identifier of the specific machine is needed */
    public static String machineName = DEFAULT_MACHINE_NAME;

    public static void loadExitIfFail(File file) {
        try {
            load(file);
        } catch( IOException e ) {
            e.printStackTrace(System.err);
            System.err.println("Error occurred while reading: '"+file.getPath()+"'");
            System.exit(1);
        }
    }

    public static void loadStdErrIfFail(File file) {
        try {
            load(file);
        } catch( IOException e ) {
            e.printStackTrace(System.err);
            System.err.println("Error occurred while reading: '"+file.getPath()+"'");
        }
    }

    /**
     * Loads the settings file or throws an exception if something goes wrong.
     */
    public static void load(File file) throws IOException {
        // Create a new file if it doesn't exist
        if( !file.exists() ) {
            System.out.println("Creating new local settings: "+file.getPath());
            save(file);
            return;
        }

        Reader reader = new FileReader(file);
        Yaml yaml = createYmlObject();
        Map<String,Object> data = yaml.load(reader);

        int version = (int)data.get(KEY_VERSION);
        if( version != 1 )
            throw new RuntimeException(FILE_NAME+" has a different version. Manually update. Found="+version);
        machineName = (String)data.get(KEY_MACHINE_NAME);

        if( machineName.equals(DEFAULT_MACHINE_NAME)) {
            System.err.println("Please change the machine name inside of "+
                    FILE_NAME+" as it is currently the default value");
        }

        reader.close();
    }

    /**
     * Saves the settings as a yaml file.
     *
     */
    public static void save(File file) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(file));

        out.println("# Automatically generated local settings. DO NOT ADD TO GIT");

        Map<String, Object> data = new HashMap<>();
        data.put(KEY_VERSION,1);
        data.put(KEY_MACHINE_NAME,machineName);

        Yaml yaml = createYmlObject();
        yaml.dump(data,out);
        out.close();
    }

    public static Yaml createYmlObject() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }
}
