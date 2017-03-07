package com.cerner.jwala.ui.selenium.testSuiteClasses.configuration.jvm;

import com.cerner.jwala.ui.selenium.testSuiteClasses.JwalaTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jedd Cuison on 2/28/2017
 */
public class JvmCreateTest extends JwalaTest {

    private static final String GROUP_NAME_1 = "zGroup1-" + CURRENT_TIME_MILLIS;
    private static final String GROUP_NAME_2 = "zGroup2-" + CURRENT_TIME_MILLIS;

    private static final String JVM_NAME = "zJvm-" + CURRENT_TIME_MILLIS;

    @Test
    public void testCreateJvm() throws InterruptedException {
        clickTab("Configuration");
        clickTab("JVM");

        driver.findElement(By.xpath("//button[span[text()='Add']]")).click();

        // Get the width of the dialog box so we can test if the width changed later after selecting the groups
        final WebElement jvmDlg = driver.findElement(By.xpath("//div[contains(@class, 'ui-dialog')]"));
        final int dlgWidth = jvmDlg.getSize().getWidth();

        driver.switchTo().activeElement().sendKeys(JVM_NAME);
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys(JVM_NAME + "-host");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("/manager");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);
        driver.switchTo().activeElement().sendKeys("321");
        driver.switchTo().activeElement().sendKeys(Keys.TAB); // populates the other port text boxes
        Thread.sleep(500); // wait a little for the app to populate the other ports

        // check https port
        assertEquals("322", driver.switchTo().activeElement().getAttribute("value"));
        driver.switchTo().activeElement().sendKeys(Keys.TAB);

        final String statusPath = driver.findElement(By.xpath("//div[@class='jvmStatusUrl']")).getText();
        assertEquals("https://" + JVM_NAME + "-host:322/manager", statusPath);

        // redirect port
        assertEquals("323", driver.switchTo().activeElement().getAttribute("value"));
        driver.switchTo().activeElement().sendKeys(Keys.TAB);

        // shutdown port
        assertEquals("324", driver.switchTo().activeElement().getAttribute("value"));
        driver.switchTo().activeElement().sendKeys(Keys.TAB);

        // ajp port
        assertEquals("325", driver.switchTo().activeElement().getAttribute("value"));
        driver.switchTo().activeElement().sendKeys(Keys.TAB);

        driver.switchTo().activeElement().sendKeys("zUser");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);

        driver.switchTo().activeElement().sendKeys("zPassw0rd");
        driver.switchTo().activeElement().sendKeys(Keys.TAB);

        Select jdkDropDown = new Select(driver.switchTo().activeElement());
        jdkDropDown.selectByIndex(1);

        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_1 + "')]/input")).click();
        driver.findElement(By.xpath("//div[contains(text(), '" + GROUP_NAME_2 + "')]/input")).click();

        testIfDialogWidthChangedWhenGroupMsgAppeared(jvmDlg, dlgWidth);

        driver.findElement(By.xpath("//button[span[text()='Ok']]")).click();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[text()='" + JVM_NAME + "']"), 1));
    }

    /**
     * Tests if the JVM dialog width changed when the group message appeared
     * Note that this method also checks the message content as a secondary objective and to make sure that message
     * is displayed before testing the width
     * @param jvmDlg the JVM add/edit dialog
     * @param initialDialogWidth the intial width of the dialog box when first shown
     */
    private void testIfDialogWidthChangedWhenGroupMsgAppeared(final WebElement jvmDlg, final int initialDialogWidth) {
        final String msg = driver.findElement(By.xpath("//div[@class='groupListMsg']")).getText();
        assertEquals("The JVM templates will only be inherited from a single group", msg);
        assertEquals(initialDialogWidth, jvmDlg.getSize().getWidth());
    }

}
