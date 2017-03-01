package com.cerner.jwala.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.exception.ApplicationException;

import java.io.*;
import java.util.Properties;

public class ExternalProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalProperties.class);

    private static volatile String propertiesFilePath = null;
    private static volatile Properties properties;

    private ExternalProperties() {
        properties = new Properties();
        init();
    }

    private static final class DeferredLoader {
        public static final ExternalProperties INSTANCE = new ExternalProperties();
    }
    
    public static ExternalProperties getInstance() {
        return DeferredLoader.INSTANCE;
    }

    public static Properties getProperties() {
        final Properties copy = new Properties();
        copy.putAll(getInstance().properties);
        return copy;
    }

    public static void setPropertiesFilePath(String propFilePath){
        propertiesFilePath = propFilePath;
        reload();
    }

    public static void reload() {
        getInstance().init();
    }

    public static String get(String key) {
        String propVal = getProperties().getProperty(key);
        LOGGER.debug("PropertyGet({})=({})",key,propVal);
        return propVal;
    }

    public static Integer getAsInteger(String key) {
        return Integer.parseInt(getProperties().getProperty(key));
    }

    public static Boolean getAsBoolean(String key) {
        return Boolean.parseBoolean(getProperties().getProperty(key));
    }

    public static int size() {
        return getProperties().size();
    }

    private void init() {
        String propertiesFile = propertiesFilePath != null ? propertiesFilePath : null;
        Properties tempProperties = new Properties();
        if (propertiesFile != null) {
            try {
                tempProperties.load(new FileReader(new File(propertiesFile)));
            } catch (IOException e) {
                throw new ApplicationException("Failed to load properties file " + propertiesFile, e);
            }
            LOGGER.info("Properties loaded from path " + propertiesFile);
        }
        properties = tempProperties;
    }

    public static String get(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public static void loadFromInputStream(InputStream inputStream) {
        getInstance().load(inputStream);
    }

    private void load(InputStream inputStream) {
        LOGGER.info("Load the external properties from a stream");
        Properties tempProperties = new Properties();
        try {
            tempProperties.load(inputStream);
        } catch (IOException e) {
            throw new ApplicationException("Failed to load properties from stream", e);
        }
        properties = tempProperties;
    }

    public static void reset() {
        getInstance().clearProperties();
    }

    private void clearProperties() {
        properties = new Properties();
    }
}
