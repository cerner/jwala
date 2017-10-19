package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;
import static org.junit.Assert.assertTrue;

/**
 * Created by Sharvari Barve on 8/14/2017.
 */
public class ResourceErrorHandlingRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    /*
    This is necessary to prevent popup from an unsaved file
     */
    @When("^I erase garbage value \"(.*)\"$")
    public void removeGarbageValue(String text) {
        jwalaUi.clickWhenReady(By.xpath("//*[contains(text(),'" + text + "')]"));
        jwalaUi.sendKeys(Keys.HOME);
        jwalaUi.sendKeys(Keys.chord(Keys.SHIFT, Keys.ARROW_DOWN));
        jwalaUi.sendKeys(Keys.DELETE);
    }

    @Then("^I confirm metaData error popup$")
    public void verifyMetaDataError() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Unable to save changes until the meta data errors are fixed: ')]"));
        clickOk();
    }

    @Then("^I confirm resource deploy error popup for file \"(.*)\" and jvm \"(.*)\"$")
    public void seeErrorForJvmSingleResource(String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for Jvm: " + jvm + "')]"));
        clickOk();
    }

    @Then("^I confirm multiple resource deploy error popup for file \"(.*)\" and file \"(.*)\" and jvm \"(.*)\"$")
    public void seeErrorForJvmMultipleResources(String file1, String file2, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for Jvm: " + jvm + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for Jvm: " + jvm + "')]"));
        clickOk();
    }

    @Then("^I confirm resource deploy error popup for file \"(.*)\" and webserver \"(.*)\"$")
    public void seeErrorForWebserver(String file, String seleniumWebserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for WebServer: " + seleniumWebserver + "')]"));
        clickOk();
    }

    @Then("^I confirm many resource deploy error popup for file \"(.*)\" and file \"(.*)\" and webserver \"(.*)\"$")
    public void seeErrorForWebserver(String file1, String file2, String seleniumWebserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for WebServer: " + seleniumWebserver + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for WebServer: " + seleniumWebserver + "')]"));
        clickOk();
    }

    @Then("^I confirm error popup for resourceFile \"(.*)\" and web app \"(.*)\"$")
    public void verifyErrorWebApp(String file, String webApp) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for WebApp:" + webApp + "')]"));
        clickOk();
    }

    @Then("^I confirm error popup for file1 \"(.*)\" and file2 \"(.*)\" and web app \"(.*)\"$")
    public void verifyErrorWebAppMultipleResources(String file1, String file2, String webApp) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for WebApp:" + webApp + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for WebApp:" + webApp + "')]"));
        clickOk();
    }

    @And("^I confirm to unable to save error popup$")
    public void verifyUnableToSave() {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Unable to save changes until the meta data errors are fixed: Unexpected token')]"));
        clickOk();
    }

    @Then("^I verify failure to unzip the war file with deployPath \"(.*)\" and name as \"(.*)\"$")
    public void verifyUnableToUnzipError(String path, String resourceName) {
        String properPath = (paramProp.getProperty(path) == null) ? path : paramProp.getProperty(path);
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'cannot unzip from " + properPath + "/" + resourceName + ".war" + " to " + properPath
                + "/" + resourceName + "')]"));
        clickOk();
    }

    private void clickOk() {
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }
}
