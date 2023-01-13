package com.api.helper.api;

import java.io.FileInputStream;
import java.util.Properties;

public class RetrieveUrl {
    private RetrieveUrl() {
        throw new IllegalStateException("Utility class");
    }

    public static final String CONFIG_PROP = "./src/main/resources/testData.properties";

    public static String retrieveUrl(String urlName) {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_PROP)) {
            prop.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (prop.getProperty(urlName));
    }

}
