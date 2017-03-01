package com.cerner.jwala.common;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Arvindo Kinny on 2/13/2017.
 */

public class FileUtilityTest {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileUtilityTest.class);
    public static final String SRC_TEST_RESOURCES = new File(".").getAbsolutePath() + "/src/test/resources/";
    FileUtility fileUtility = new FileUtility();
    public void setUp() {

    }

    @Test
    public void testUnZip() throws IOException {
        File tempDir = new File(new File(".").getAbsolutePath() + "/build/temp");
        tempDir.mkdirs();
        fileUtility.unzip(new File(SRC_TEST_RESOURCES + "instance-template-7.0.55.zip"), tempDir);
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testGetZipDirRoots() {
        Set<String> dirs = fileUtility.getZipRootDirs(SRC_TEST_RESOURCES + "instance-template-7.0.55.zip");
        LOGGER.info("Size {}", dirs.size());
        assertEquals(dirs.size(), 7);
    }
}