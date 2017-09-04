package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * Created by Sharvari Barve on 8/14/2017.
 */
public class ResourceErrorHandlingRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @When("^I enter garbage value in metadata$")
    public void enterGarbageValueInMetaData() {
        jwalaUi.clickWhenReady(By.xpath("//*[text()='{']"));
        jwalaUi.sendKeys(Keys.ENTER + "${{" + Keys.ENTER);
    }

    @When("^I enter garbage value in template at text \"(.*)\"$")
    public void enterGarbageTemplate(String fileContent) {
        jwalaUi.click(By.xpath("//*[contains(text(),'" + fileContent + "')]"));
        jwalaUi.sendKeys(Keys.ENTER + "${{" + Keys.ENTER);
    }

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

    @And("^I see save button of \"(.*)\" again$")
    public void seeSaveButton(String label) {
        jwalaUi.waitUntilElementIsNotVisible(By.xpath("//*[text()='Saved']"));
        jwalaUi.waitUntilElementIsClickable(By.xpath("//*[@title='Save' and contains(@data-reactid,'" + label + "')]"), 600);
    }

    @Then("^I verify metaData error$")
    public void verifyMetaDataError() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'Unable to save changes until the meta data errors are fixed: ')]"));
    }

    @Then("^I verify resource deploy error for file \"(.*)\" and jvm \"(.*)\"$")
    public void seeErrorForJvmSingleResource(String file, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for Jvm: " + jvm + "')]"));
    }

    @Then("^I verify multiple resource deploy error for file \"(.*)\" and file \"(.*)\" and jvm \"(.*)\"$")
    public void seeErrorForJvmMultipleResources(String file1, String file2, String jvm) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for Jvm: " + jvm + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for Jvm: " + jvm + "')]"));
    }

    @Then("^I verify resource deploy error for file \"(.*)\" and webserver \"(.*)\"$")
    public void seeErrorForWebserver(String file, String seleniumWebserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for WebServer: " + seleniumWebserver + "')]"));
    }

    @Then("^I verify many resource deploy error for file \"(.*)\" and file \"(.*)\" and webserver \"(.*)\"$")
    public void seeErrorForWebserver(String file1, String file2, String seleniumWebserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for WebServer: " + seleniumWebserver + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for WebServer: " + seleniumWebserver + "')]"));
    }

    @Then("^I verify error for resourceFile \"(.*)\" and web app \"(.*)\"$")
    public void verifyErrorWebApp(String file, String webApp) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file + " for WebApp:" + webApp + "')]"));
    }

    @Then("^I verify error for file1 \"(.*)\" and file2 \"(.*)\" and web app \"(.*)\"$")
    public void verifyErrorWebAppMultipleResources(String file1, String file2, String webApp) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file1 + " for WebApp:" + webApp + "')]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//*[contains(text(),'TEMPLATE: Failed to bind data and properties to : " + file2 + " for WebApp:" + webApp + "')]"));

    }


    @And("^I click ok button to unable to save popup$")
    public void clickOkToUnableToSave() {
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }

    @And("^I verify unable to save error$")
    public void verifyUnableToSave() {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Unable to save changes until the meta data errors are fixed: Unexpected token')]"));
    }

    @And("^I click ok to resource error popup$")
    public void clickOkToResourceError(){
        jwalaUi.click(By.xpath("//*[text()='Ok']"));
    }
}