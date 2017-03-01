package com.cerner.jwala.commandprocessor.impl;

import org.junit.Before;

import static org.junit.Assume.assumeTrue;

public class WindowsTest {

    @Before
    public void verifyRunningInWindowsEnvironment() {
        assumeTrue(System.getProperty("os.name").contains("Windows"));
    }
}
