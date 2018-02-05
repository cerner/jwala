package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

/**
 * Created by Sharvari Barve on 9/2/2017.
 */
public class HandleResourceRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Given("I click the \"(.*)\" topology node")
    public void selectEntityTopologyNode(final String entityName) {
        jwalaUi.clickWhenReady(By.xpath("//span[text()='" + entityName + "']"));
    }

    @And("I press enter to confirm that I don't want to save changes to the resource")
    public void confirmNotToSaveTheResource() {
        jwalaUi.getWebDriver().switchTo().alert().accept();
    }

    @When("^I select resource file \"(.*)\"$")
    public void selectFile(String fileName) {
        jwalaUi.click(By.xpath("//span[text()='" + fileName + "']"));
    }

    @And("^I right click resource file \"(.*)\"$")
    public void rightClickFile(String fileName) {
        jwalaUi.rightClick(By.xpath("//span[text()='" + fileName + "']"));
    }

    @And("^I click resource deploy option$")
    public void clickDeploy() {
        jwalaUi.click(By.xpath("//*[text()='deploy']"));
    }

    @And("^I click resource deploy to a host option$")
    public void clickDeployToAHost() {
        jwalaUi.click(By.xpath("//*[text()='a host']"));
    }

    @And("^I click resource deploy All option$")
    public void clickDeployAll() {
        jwalaUi.click(By.xpath("//*[text()='all hosts']"));
    }

    @And("^I click yes button to deploy a resource popup")
    public void confirmDeployPopup() {
        jwalaUi.click(By.xpath("//*[text()='Yes']"));
    }

    @When("^I click \"(.*)\" component$")
    public void selectComponent(String component) {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(text(),'" + component + "')]"));
    }

    @And("^I select the resource file \"(.*)\"$")
    public void clickResource(String resource) {
        jwalaUi.click(By.xpath("//span[text()='" + resource + "']"));
    }

    @When("^I add property \"(.*)\"$")
    public void addProperty(String property) {
        jwalaUi.waitUntilElementIsNotVisible(
                By.xpath("//div[text()='Please select a JVM, Web Server or Web Application and a resource']"), 5);
        jwalaUi.click(By.xpath("//div[@class='CodeMirror-lines']"));
        jwalaUi.sendKeysViaActions("\n" + property + "\n");
    }

    @And("^I expand \"(.*)\" node in data tree$")
    public void expandPropertyDataTree(String property) {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(@class,'nodeKey') and contains(text(),'" + property + "') ]/preceding-sibling::span"));
    }

    @Then("^I verify dataTree has the key-value pair as \"(.*)\" and \"(.*)\" respectively$")
    public void verifyDataTree(String key, String value) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[contains(@class,'nodeKey') and contains(text(),'" + key + "') ]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[contains(@class,'nodeVal') and contains(text(),'" + value + "')]"));
    }

    /**
     * Insert text in the resource template/metadata text box
     *
     * @param inputStr the string that is to be inserted in the resource text box
     * @param atStr the String whose location is where the inputStr parameter will be inserted at
     */
    @When("^I enter value \"(.*)\" in the edit box at text \"(.*)\"$")
    public void insertStrInTheResourceEditor(final String inputStr, final String atStr) {
        try {
            jwalaUi.click(By.xpath("(//pre[contains(@class, 'CodeMirror-line')]//span[text()='" + atStr.trim() + "'])[1]"));
        } catch (final NoSuchElementException e) {
            jwalaUi.click(By.xpath("(//pre[contains(@class, 'CodeMirror-line')]//span[contains(text(), '" + atStr.trim() + "')])[1]"));
        }
        jwalaUi.sendKeysViaActions(inputStr);
    }

    /*
        clicks the save button of the template or the metadata, as the xpath returns both the elements
     */
    @And("^I click save button of edit box of \"(.*)\"$")
    public void clickSaveButton(String tab) {
        List<WebElement> elements = jwalaUi.getWebDriver().findElements(By.xpath("//span[contains(@class, 'ui-icon-disk') and @title='Save']"));
        WebElement saveElement = tab.equals("Template") ? elements.get(0) : elements.get(1);
        saveElement.click();
    }

    @Then("^I wait for notification \"(.*)\"")
    public void waitForNotification(String notification) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='" + notification + "']"));
    }

    @And("^I verify edit \"(.*)\"$")
    public void verifyText(String text) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(.,'" + text + "')]"));
    }

    @When("^I click the resource delete icon$")
    public void clickDelete() {
        jwalaUi.click(By.xpath("//*[@title='delete']"));
    }

    @And("^I click check-box for resourceFile \"(.*)\"$")
    public void selectResourceCheckBox(String resourceFile) {
        jwalaUi.clickWhenReady(By.xpath("//li/span[text()='" + resourceFile + "']/preceding-sibling::input[@type='checkbox']"));
    }

    @Then("^I confirm delete a resource popup$")
    public void deleteResource() {
        jwalaUi.isElementExists(By.xpath("//div[contains(text(), 'Are you sure you want to delete the selected resource template(s) ?')]"));
        jwalaUi.click(By.xpath("//button/span[text()='Yes']"));
    }

    @Then("^I confirm delete a external property resource popup$")
    public void deleteResourceForExternalProperty() {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(), 'Are you sure you want to delete the property file ?')]"));
        jwalaUi.click(By.xpath("//button/span[text()='Yes']"));
    }

    @And("^I click \"([^\"]*)\" tab$")
    public void clickTab(String tab) {
        jwalaUi.clickTab(tab);
    }

    @And("^I check for unable to save error$")
    public boolean checkForUnableToSaveError() {
        return jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Unable to save')]"));
    }

    /*
        Needed if resources are immediately saved one after the other, resulting in a saved notifcation hovering over the saved button
     */
    @And("^I wait for the save button to be visible again$")
    public void waitForSaveButton() {
        jwalaUi.waitUntilElementIsNotVisible(By.xpath("//*[contains(text(),'Saved')]"), 30);
        jwalaUi.clickWhenReady(By.xpath("//*[contains(@class, 'ui-icon-disk') and @title='Save']"), 60);
    }

    @And("^I enter attribute in metaData with key as \"(.*)\" and value as \"(.*)\"$")
    public void enterAttribute(String key, String value) {
        jwalaUi.clickWhenReady(By.xpath("(//pre[contains(@class, 'CodeMirror-line')]//span[text()='{'])[1]"));
        jwalaUi.doubleClick();
        jwalaUi.sendKeysViaActions("{\n" + key + ":" + value + ",");
    }

    /*
      Text is divided into multiple spans hence checking only partial text
     */
    @And("^I click the ok button to override JVM Templates$")
    public void overrideJvmTemplates() {
        jwalaUi.isElementExists(By.xpath("//span[contains(text(),'Saving will overwrite all')]"));
        jwalaUi.clickOk();
    }

    @And("^I confirm the resource deploy to a host popup$")
    public void confirmDeployToAHost() {
        jwalaUi.isElementExists(By.xpath("//span[contains(text(),'Select a host')]"));
        jwalaUi.clickOk();
    }

    @And("^I confirm overriding individual instances popup for resourceFile \"(.*)\"$")
    public void confirmOverride(String resource) {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Any previous customizations to an individual instance of \"" + resource + "\" will be overwritten.')]"));
         jwalaUi.clickYes();
    }

    @And("^I don't see \"([^\"]*)\" node in the data tree$")
    public void verifyabsenceOfNode(String nodeName)  {
        assertFalse(jwalaUi.isElementExists(By.xpath("//span[contains(@class,'nodeKey') and contains(text(),'" + nodeName + "') ]/preceding-sibling::span")));
    }



}
