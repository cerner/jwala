package com.cerner.jwala.ui.selenium.testsuite;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;

/**
 * Contains tests that should be done before hitting the main tests
 * Created by Jedd Cuison on 2/28/2017
 */
public class PreFlightTest extends JwalaTest {

    @Test
    public void testUrl() throws IOException {
        driver.get(getBaseUrl());
        assertFalse(driver.getPageSource().contains("404"));
    }

}
