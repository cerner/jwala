package com.cerner.jwala.ui.selenium.configuration.resources;

import com.cerner.jwala.ui.selenium.SeleniumTestCase;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Test resource topology UI
 *
 * Created by Jedd Cuison on 2/2/2017
 */
public class ResourceTopologyTest extends SeleniumTestCase {

    @Before
    public void setup() {
        setUpSeleniumDrivers();
    }

    @Test
    public void testPresenceOfWebAppUnderJvm() throws InterruptedException, IOException {
        login();
        Thread.sleep(1000);
        driver.findElement(By.xpath("//a[text()='Configuration']")).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath("//a[text()='Resources']")).click();
        Thread.sleep(1000);
        clickTreeItemExpandCollapseIcon("HEALTH CHECK 4.0", 1000);
        clickTreeItemExpandCollapseIcon("JVMs", 1000);
        clickTreeItemExpandCollapseIcon("CTO-N9SF-LTST-HEALTH-CHECK-4.0-SOMEHOST-3", 1000);
        assertTrue(isElementPresent(By.xpath("//li[span[text()='CTO-N9SF-LTST-HEALTH-CHECK-4.0-SOMEHOST-3']]//span[text()='HEALTH-CHECK-4.0']")));
    }

    private void clickTreeItemExpandCollapseIcon(final String itemLabel, final long waitTimeAfterClick) throws InterruptedException {
        driver.findElement(By.xpath("//li[span[text()='" +  itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]")).click();
        Thread.sleep(waitTimeAfterClick);
    }

}
