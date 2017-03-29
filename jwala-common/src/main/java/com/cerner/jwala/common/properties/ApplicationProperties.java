package com.cerner.jwala.common.properties;

import com.cerner.jwala.common.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

public class ApplicationProperties {

    public static final String PROPERTIES_ROOT_PATH = "PROPERTIES_ROOT_PATH";
    private volatile Properties properties;

    private static final class DeferredLoader {
        public static final ApplicationProperties INSTANCE = new ApplicationProperties();
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);

    private static final String PROPERTIES_FILE_NAME = "vars.properties";

    private static final String REQUIRED = "#REQUIRED#";

    private ApplicationProperties() {
        properties = new Properties();
        init();
    }

    public static ApplicationProperties getInstance() {
        return DeferredLoader.INSTANCE;
    }

    public static Properties getProperties() {
        final Properties copy = new Properties();
        copy.putAll(getInstance().properties);
        return copy;
    }

    public static void reload() {
        getInstance().init();
    }

    public static String getRequired(PropertyKeys propertyNames) {
        if (propertyNames == null) {
            throw new ApplicationException("Attempted to call " + ApplicationProperties.class.getName() + ".get(property) with a null property key");
        }

        return getRequired(propertyNames.getPropertyName());
    }

    public static String getRequired(String key) {
        String propVal = getProperties().getProperty(key);
        LOGGER.trace("ApplicationsProperties.get({})=({})", key, propVal);

        if (propVal == null || propVal.isEmpty()) {
            throw new PropertyNotFoundException("Required property with key " + key + " was not valued.");
        }

        return propVal;
    }

    public static String get(PropertyKeys propertyNames) {
        if (propertyNames == null) {
            throw new ApplicationException("Attempted to call " + ApplicationProperties.class.getName() + ".get(property) with a null property key");
        }

        return get(propertyNames.getPropertyName());
    }

    public static String get(String key) {
        String propVal = getProperties().getProperty(key);
        LOGGER.trace("ApplicationsProperties.get({})=({})", key, propVal);

        if (REQUIRED.equalsIgnoreCase(propVal)) {
            LOGGER.warn("Required property with key {} was not valued.", key);
        }

        return propVal;
    }

    public static Integer getAsInteger(String key) {
        return Integer.parseInt(getProperties().getProperty(key));
    }

    /**
     *
     * @param key
     * @return
     */
    public static Integer getAsInteger(PropertyKeys key) {
        return getAsInteger(key.getPropertyName());
    }

    public static Boolean getAsBoolean(String key) {
        return Boolean.parseBoolean(getProperties().getProperty(key));
    }

    public static Boolean getRequiredAsBoolean(PropertyKeys key) {
        String value = getRequired(key);
        return Boolean.parseBoolean(value);
    }

    public static int size() {
        return getProperties().size();
    }

    private void init() {

        String propertiesRootPath = System.getProperty(PROPERTIES_ROOT_PATH);
        if (propertiesRootPath == null) {
            propertiesRootPath = this.getClass().getClassLoader().getResource(PROPERTIES_FILE_NAME).getPath();
            propertiesRootPath = propertiesRootPath.substring(0, propertiesRootPath.lastIndexOf('/'));
            LOGGER.error("Properties root path not set! Loading default resource root path: {}", propertiesRootPath);
        }

        String propertiesFile = propertiesRootPath + "/" + PROPERTIES_FILE_NAME;
        Properties tempProperties = new Properties();
        try {
            tempProperties.load(new FileReader(new File(propertiesFile)));
        } catch (IOException e) {
            throw new ApplicationException("Failed to load properties file " + propertiesFile, e);
        }
        properties = tempProperties;
        LOGGER.info("Properties loaded from path " + propertiesFile);
    }

    public static String get(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    /**
     * Method to get Integer as from property using ProperyKeys
     * @param key
     * @param defaultValue
     * @return
     */
    public static Integer getAsInteger(PropertyKeys key, Integer defaultValue) {
        final String returnVal = getProperties().getProperty(key.getPropertyName());
        if (null == returnVal) {
            LOGGER.debug("No property key found for {}, using default value {}", key, defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(returnVal);
        } catch (NumberFormatException ex) {
            String errorMsg = MessageFormat.format("Expecting an integer value for property with key {0} but instead found {1}", key, returnVal);
            LOGGER.error(errorMsg, ex);
            throw new ApplicationException(errorMsg, ex);
        }
    }
}
