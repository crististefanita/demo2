package com.api.helper.file;

import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@Log4j
public class PropertiesFile {
    private static FileInputStream fis;
    private static Properties prop = null;

    public static String getProperty(String property) {
        try {
            fis = new FileInputStream(new File("./src/test/resources/config.properties"));
            prop = new Properties();
            prop.load(fis);
        } catch (FileNotFoundException fnfe) {
            log.error("Properties File Not Found", fnfe);
        } catch (IOException ioe) {
            log.error("IO Exception while loading Properties File", ioe);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                log.error("IO Exception while closing file input stream", e);
            }
        }
        return prop.getProperty(property).trim();
    }
}
