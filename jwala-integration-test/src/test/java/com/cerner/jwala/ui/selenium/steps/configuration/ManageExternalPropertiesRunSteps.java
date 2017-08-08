package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;

import static junit.framework.TestCase.assertFalse;

/**
 * Created by Sharvari Barve on 7/25/2017.
 */
public class ManageExternalPropertiesRunSteps {
    @Autowired
    JwalaUi jwalaUi;


    @When("^I click ext property add resource$")
    public void clickAddResource() {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(@class, 'ui-icon-plusthick')]"));
    }

    @When("^I click external property$")
    public void selectExternalProperty() {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(text(),'Ext Properties')]"));
    }

    @When("^I click external properties in data tree$")
    public void clickextPropertyDataTree() {
        jwalaUi.click(By.xpath("//span[contains(@class,'nodeKey') and contains(text(),'ext') ]/preceding-sibling::span"));
    }

    @Then("^I verify external property$")
    public void verifyexternalProperty() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[contains(text(),'ext.properties')]"));
    }

    @Then("^I verify external property override message$")
    public void verifyOverrideMsg() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Only one external properties file can be uploaded. Any existing ones will be overwritten.']"));
    }

    @Then("^I verify edit \"(.*)\"$")
    public void verifyText(String text) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[contains(.,'" + text + "')]"));
    }

    @Then("^I verify dataTree \"(.*)\" and value \"(.*)\"$")
    public void verifyDataTree(String key, String value) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[contains(@class,'nodeKey') and contains(text(),'" + key + "') ]"));
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[contains(@class,'nodeVal') and contains(text(),'" + value + "')]"));

    }

    @Then("^I click ext property data tree$")
    public void clickExtPropertyDataTree() {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(@class,'nodeKey') and contains(text(),'ext') ] /preceding-sibling::span"));
    }

    @When("I click the delete icon$")
    public void clickDelete() {
        jwalaUi.click(By.xpath("//*[@title='delete']"));


    }

    @When("^I click external properties check-box$")
    public void checkExternalProperty() {
        jwalaUi.click(By.xpath("//li/span[text()='ext.properties']/preceding-sibling::input[@type='checkbox']"));
    }

    @When("^I add property \"(.*)\"$")
    public void addProperty(String property) {
        jwalaUi.clickWhenReady(By.xpath("//span[text()='Ext Properties']"));
        jwalaUi.clickWhenReady(By.xpath("//li/span[text()='ext.properties']"));
        jwalaUi.click(By.xpath("//div[contains(@class, 'CodeMirror') and contains(@class, 'cm-s-default')]"));
        jwalaUi.sendKeys(Keys.ENTER + property + Keys.ENTER);
    }

    @Then("^I verify ext properties is deleted$")
    public void checkExternalPropertyIsDeleted() {
        assertFalse(jwalaUi.isElementExists(By.xpath("//span[contains(text(),'ext.properties']")));
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

}
