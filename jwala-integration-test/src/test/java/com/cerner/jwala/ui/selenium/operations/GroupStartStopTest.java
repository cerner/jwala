package com.cerner.jwala.ui.selenium.operations;

import com.cerner.jwala.ui.selenium.SeleniumTestCase;

public class GroupStartStopTest extends SeleniumTestCase {
    /*private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private InputStream inputStream;
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        setUpSeleniumDrivers();
    }

    @Test
    public void testGroupStartStop() throws Exception {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("userName")).sendKeys(properties.getProperty("jwala.user.name"));
        driver.findElement(By.id("password")).sendKeys(properties.getProperty("jwala.user.password"));
        driver.findElement(By.cssSelector("input[type=\"button\"]")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("Start Group".equals(driver.findElement(By.id("group-operations-tablebtnstartGroup1")).getText())) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.id("group-operations-tablebtnstartGroup1")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.id("ui-id-2"))) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.xpath("(//button[@type='button'])[8]")).click();
        driver.findElement(By.id("group-operations-table_1")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='ws-child-table_group-operations-table_1']/tbody/tr/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='ws-child-table_group-operations-table_1']/tbody/tr[2]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[2]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[3]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[4]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[5]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STARTED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[6]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.id("group-operations-tablebtnstopGroup1")).click();
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if (isElementPresent(By.id("ui-id-5"))) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.xpath("(//button[@type='button'])[65]")).click();
        // ERROR: Caught exception [ERROR: Unsupported command [getTable | id=ws-child-table_group-operations-table_1.1.7 | ]]
        // ERROR: Caught exception [ERROR: Unsupported command [getTable | id=ws-child-table_group-operations-table_1.2.7 | ]]
        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STOPPED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr/td[8]/div/span")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STOPPED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[2]/td[8]/div/span")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STOPPED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[3]/td[8]/div/span")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STOPPED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[4]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STOPPED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[5]/td[8]/div")).getText())) break;
            Thread.sleep(1000);
        }

        for (int second = 0;; second++) {
            if (second >= 60) fail("timeout");
            if ("STOPPED".equals(driver.findElement(By.xpath("//table[@id='jvm-child-table_group-operations-table_1']/tbody/tr[6]/td[8]/div/span")).getText())) break;
            Thread.sleep(1000);
        }

        driver.findElement(By.linkText("Logout")).click();
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }*/
}
