package com.cerner.jwala.ui.selenium.testsuite.configuration.resources;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.testsuite.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertTrue;

/**
 * Test resource topology UI
 * Created by Jedd Cuison on 2/2/2017
 */
public class ResourceTopologyTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String JVM_NAME = "zJvm-" + CURRENT_TIME_MILLIS;
    private static final String APP_NAME = "zApp-" + CURRENT_TIME_MILLIS;

    @Test
    public void testPresenceOfWebAppUnderJvm() {
        clickTab("Configuration");
        clickTab("Resources");
        final WebElement groupNode = clickTreeItemExpandCollapseIcon(GROUP_NAME_1);
        clickTreeItemExpandCollapseIcon(groupNode, "JVMs");
        clickTreeItemExpandCollapseIcon(JVM_NAME);
        assertTrue(SeleniumTestHelper.isElementRendered(driver,
                By.xpath("//li[span[text()='" + JVM_NAME + "']]//span[text()='" + APP_NAME + "']")));
    }

}
