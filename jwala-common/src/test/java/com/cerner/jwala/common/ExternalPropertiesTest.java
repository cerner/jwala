package com.cerner.jwala.common;

import junit.framework.TestCase;
import org.springframework.util.FileCopyUtils;

import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.ExternalProperties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ExternalPropertiesTest extends TestCase {

    public static final String SRC_TEST_RESOURCES_PROPERTIES = new File(".").getAbsolutePath() + "/src/test/resources/properties/";
    public static final String BUILD_TEST_RESOURCES_PROPERTIES = new File(".").getAbsolutePath() + "/build/";
    public static final String EXTERNAL_PROPERTIES = "external.properties";

    public void testBadPropertiesPath() {
        try {
            ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + "NOPE.properties");
            ExternalProperties.get("doesn't matter");
        } catch (ApplicationException e) {
            assertTrue(true);
            return;
        }

        assertFalse(false);
    }

    public void testLoadProperties() {
        ExternalProperties.setPropertiesFilePath(null);
        assertEquals(0, ExternalProperties.size());

        ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);
        assertTrue(ExternalProperties.size() > 0);
    }

    public void testReadProperties() {
        ExternalProperties.setPropertiesFilePath(SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);
        assertEquals("string property", ExternalProperties.get("string.property"));
        assertEquals(Integer.valueOf(5), ExternalProperties.getAsInteger("integer.property"));
        assertEquals(Boolean.TRUE, ExternalProperties.getAsBoolean("boolean.property"));
    }

    public void testReload() throws IOException {
        FileCopyUtils.copy(new File(SRC_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES), new File(BUILD_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES));

        ExternalProperties.setPropertiesFilePath(BUILD_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);

        final String propertyToAdd = "test.reload=true";
        writeNewPropertyToFile(propertyToAdd, BUILD_TEST_RESOURCES_PROPERTIES + EXTERNAL_PROPERTIES);

        ExternalProperties.reload();
        assertEquals("string property", ExternalProperties.get("string.property"));
        assertEquals(Integer.valueOf(5), ExternalProperties.getAsInteger("integer.property"));
        assertEquals(Boolean.TRUE, ExternalProperties.getAsBoolean("boolean.property"));
        assertEquals(Boolean.TRUE, ExternalProperties.getAsBoolean("test.reload"));
        assertNull(ApplicationProperties.get("home team"));
    }

    private void writeNewPropertyToFile(String propertyToAdd, String propertiesFilePath) throws IOException {
        Files.write(Paths.get(propertiesFilePath), propertyToAdd.getBytes(), StandardOpenOption.APPEND);
    }
}
