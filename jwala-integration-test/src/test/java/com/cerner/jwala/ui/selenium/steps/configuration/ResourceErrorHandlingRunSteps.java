package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;


/**
 * Created by Sharvari Barve on 8/14/2017.
 */
public class ResourceErrorHandlingRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;


    @When("^I erase garbage value \"(.*)\"$")
    public void deleteResourceEditorText(final String text) {
        // Click the first occurrence of the element only
        final String normalizedText = text.charAt(0) == '"' ? text.substring(1, text.length() - 1) : text;
        By by = By.xpath("(//pre[contains(@class, 'CodeMirror-line')]//span[text()='" + normalizedText + "'])[1]");
        jwalaUi.click(by);
        jwalaUi.sendKeysViaActions(Keys.BACK_SPACE);
    }

    @Then("^I confirm metaData error popup$")
    public void verifyMetaDataError() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Unable to save changes until the meta data errors are fixed: ')]"));
        jwalaUi.clickOk();
    }

    @Then("^I confirm resource deploy error popup for file \"(.*)\" and jvm \"(.*)\"$")
    public void seeErrorForJvmSingleResource(String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for Jvm: " + jvm + "')]"));
        jwalaUi.clickOk();
    }

    @Then("^I confirm multiple resource deploy error popup for file \"(.*)\" and file \"(.*)\" and jvm \"(.*)\"$")
    public void seeErrorForJvmMultipleResources(String file1, String file2, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for Jvm: " + jvm + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for Jvm: " + jvm + "')]"));
        jwalaUi.clickOk();
    }

    @Then("^I confirm resource deploy error popup for file \"(.*)\" and webserver \"(.*)\"$")
    public void seeErrorForWebserver(String file, String seleniumWebserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for WebServer: " + seleniumWebserver + "')]"));
        jwalaUi.clickOk();
    }

    @Then("^I confirm many resource deploy error popup for file \"(.*)\" and file \"(.*)\" and webserver \"(.*)\"$")
    public void seeErrorForWebserver(String file1, String file2, String seleniumWebserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for WebServer: " + seleniumWebserver + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for WebServer: " + seleniumWebserver + "')]"));
        jwalaUi.clickOk();
    }

    @Then("^I confirm error popup for resourceFile \"(.*)\" and web app \"(.*)\"$")
    public void verifyErrorWebApp(String file, String webApp) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for WebApp:" + webApp + "')]"));
        jwalaUi.clickOk();
    }

    @Then("^I confirm error popup for file1 \"(.*)\" and file2 \"(.*)\" and web app \"(.*)\"$")
    public void verifyErrorWebAppMultipleResources(String file1, String file2, String webApp) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for WebApp:" + webApp + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for WebApp:" + webApp + "')]"));
        jwalaUi.clickOk();
    }

    @And("^I confirm to unable to save error popup$")
    public void verifyUnableToSave() {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Unable to save changes until the meta data errors are fixed: Unexpected token')]"));
        jwalaUi.clickOk();
    }

    @Then("^I verify failure to unzip the war file with deployPath \"(.*)\" and name as \"(.*)\"$")
    public void verifyUnableToUnzipError(String path, String resourceName) {
        String properPath = (paramProp.getProperty(path) == null) ? path : paramProp.getProperty(path);
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'cannot unzip from " + properPath + "/" + resourceName + ".war" + " to " + properPath
                + "/" + resourceName + "')]"));
        jwalaUi.clickOk();
    }

}
