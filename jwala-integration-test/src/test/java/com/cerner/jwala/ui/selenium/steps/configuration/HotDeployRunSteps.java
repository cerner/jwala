package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Properties;

/**
 * Created by SB053052 on 9/4/2017.
 */
public class HotDeployRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @When("^I enter hot Deploy value \"(.*)\" in the edit box for the file \"(.*)\"$")
    public void checkForHotDeploy(String text, String file) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[text()='{']"));

        List<WebElement> elements = jwalaUi.getWebDriver().findElements(By.xpath("//span[contains(text(),'hotDeploy')]"));
        if (elements.size() > 0) {
            WebElement finalElement = elements.get(0);
        }
        if (elements.size() == 0) {
            jwalaUi.clickWhenReady(By.xpath("//*[text()='{']"));
            jwalaUi.sendKeys(Keys.DELETE);
            jwalaUi.sendKeys("{");
            jwalaUi.sendKeys(Keys.ENTER);
            jwalaUi.sendKeys(text + ",");
        } else {
            WebElement hotDeployElement = jwalaUi.getWebElement(By.xpath("//span[contains(text(), 'hotDeploy')]"));
            hotDeployElement.findElement(By.xpath("..")).findElement(By.xpath("..")).click();
            int i = 5;
            while (i >= 0) {
                jwalaUi.sendKeys(Keys.BACK_SPACE);
                i--;
            }
            String value = "true";
            jwalaUi.sendKeys(value);
            jwalaUi.sendKeys(Keys.ENTER);

        }
    }

    @When("^I enter deployToAllJvms value \"(.*)\" in the edit box for the file \"(.*)\"$")
    public void checkForDeployToAllJvms(String text, String file) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[text()='{']"));

        List<WebElement> elements = jwalaUi.getWebDriver().findElements(By.xpath("//span[contains(text(),'deployToJvms')]"));
        if (elements.size() > 0) {
            WebElement finalElement = elements.get(0);
        }
        WebElement jvmElement = jwalaUi.getWebElement(By.xpath("//span[contains(text(), 'deployToJvms')]"));
        jvmElement.findElement(By.xpath("..")).findElement(By.xpath("..")).click();
        int i = 5;
        while (i >= 0) {
            jwalaUi.sendKeys(Keys.BACK_SPACE);
            i--;
        }
        String value = "true";
        jwalaUi.sendKeys(value);
        jwalaUi.sendKeys(Keys.ENTER);

    }

    @Then("^I verify successful deploy$")
    public void verifySuccesfulDeploy() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Deploy successful!')]"));
    }

    @Then("^I verify error message for file \"(.*)\" for jvm \"(.*)\"$")
    public void verifyErrorMessageIndividiualJvm(String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target JVM " + jvm + " must be stopped or the resource " + file + " must be set to hotDeploy=true before attempting to update the resource files')]"));
    }

    @Then("^I verify deploy error message for ws file \"(.*)\" for webserver \"(.*)\"$")
    public void verifyErrorMessageIndividiualWebServer(String file, String webserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'The target Web Server " + webserver + " must be stopped or the resource must be configured to be hotDeploy=true before attempting to deploy the resource " + file + "')]"));
    }

    @Then("^I verify deploy error message for webserver \"(.*)\" in operations$")
    public void verifyErrorMessageWebserverOperations(String wsName) {
        jwalaUi.isElementExists(By.xpath("The target Web Server " + wsName + " must be stopped before attempting to update the resource file"));
    }

    @Then("^I verify error message for group \"(.*)\" for jvm file \"(.*)\" with one of JVMs as \"(.*)\"$")
    public void verifyErrorMessageGroupJvm(String group, String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Failed to deploy file " + file + " for group " + group + " since the following JVMs are running and the file is not configured for hot deploy: [" + jvm + "]')]"));
    }

    @Then("^I verify ws error message for group \"(.*)\" for ws file \"(.*)\" with one of Web-servers as \"(.*)\"$")
    public void verifyErrorMessageGroupWebServer(String group, String file, String ws) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(),'Failed to deploy " + file + " for group " + group + ": the following Web Servers were started and the resource was not configured for hotDeploy=true: [" + ws + "]')]"));
    }

    @Then("I verify error message for group \"(.*)\" for app file \"(.*)\" with one of JVMs as \"(.*)\"$")
    public void verifyErrorMessageIndividualApp(String group, String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(text(),'Failed to deploy file " + file + " for group " + group + ": not all JVMs were stopped - the following JVMs were started and the resource was not configured with hotDeploy=true: [" + jvm + "]')]"));
    }

    @And("^I click ok to resource deploy error message$")
    public void clickOkToDeployErrorPopup() {
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }
}


