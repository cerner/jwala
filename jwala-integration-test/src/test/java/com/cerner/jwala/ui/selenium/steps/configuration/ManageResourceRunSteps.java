package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * Created by Rahul Sayini on 7/7/2017.
 */
public class ManageResourceRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Given("^I am in the resource tab$")
    public void goToConfigurationTab() {
        jwalaUi.clickTab("Resources");
        jwalaUi.sleep();
    }

    @And("I expanded component \"(.*)\"$")
    public void expandComponent(String component) {
        jwalaUi.clickTreeItemExpandCollapseIcon(component);
    }

    @And("I clicked on component \"(.*)\"$")
    public void clickComponent(String component) {
        jwalaUi.clickComponentForUpload(component);
        jwalaUi.sleep();
    }

    @And("I clicked on add resource")
    public void addResource() {
        jwalaUi.clickAddResource();
    }

    @And("^I fill in the \"Deploy Name\" field with \"(.*)\"$")
    public void setDeployName(String deployFilename) {
        jwalaUi.sendKeys(By.name("deployFilename"), deployFilename);
    }

    @And("^I fill in the \"Deploy Path\" field with \"(.*)\"$")
    public void setDeployPath(String deployPath) {
        jwalaUi.sendKeys(By.xpath("//label[text()='Deploy Path']/following-sibling::input"), paramProp.getProperty(deployPath) == null ? deployPath : (String) paramProp.get(deployPath));
    }

    @Then("^I fill in the webserver \"Deploy Path\" field with \"(.*)\" for web server \"(.*)\"$")
    public void fillWebserverDeployPath(String deployPath, String webserver) {
        String newDeployPath = paramProp.getProperty(deployPath) == null ? deployPath : (String) paramProp.get(deployPath);
        jwalaUi.sendKeys(By.xpath("//label[text()='Deploy Path']/following-sibling::input"), newDeployPath + "/" + webserver);
    }

    @And("I check Upload Meta Data File")
    public void clickUploadMetaDataFile() {
        jwalaUi.click(By.xpath("//div[contains(text(),'Upload Meta Data File')]/input"));
    }

    @And("^I choose the meta data file \"(.*)\"$")
    public void selectMetaDataFile(final String archiveFileName) {
        final Path mediaPath = Paths.get(jwalaUi.getProperties().getProperty("file.upload.dir") + "/" + archiveFileName);
        jwalaUi.sendKeys(By.name("metaDataFile"), mediaPath.normalize().toString());
    }

    @And("^I choose the resource file \"(.*)\"$")
    public void selectMediaArchiveFile(final String archiveFileName) {
        final Path mediaPath = Paths.get(jwalaUi.getProperties().getProperty("file.upload.dir") + "/" + archiveFileName);
        jwalaUi.sendKeys(By.name("templateFile"), mediaPath.normalize().toString());
    }

    @And("^I click the upload resource dialog ok button$")
    public void clickAddMediaOkDialogBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @Then("^check resource uploaded successful")
    public void checkForResource() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//input[contains(@class, 'noSelect')]/following-sibling::span"));
    }

    @Then("^I check for resource \"(.*)\"$")
    public void checkForResourceUpload(String resource) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//input/following-sibling::span[text()= '" + resource + "']"));
    }

    @When("^I click save button of \"(.*)\"$")
    public void saveButton(String text) {
        List<WebElement> elements = jwalaUi.getWebDriver().findElements(By.xpath("//span[contains(@class, 'ui-icon-disk') and @title='Save']"));
        WebElement saveElement = elements.get(0);
        for (WebElement element : elements) {
            if (element.getAttribute("outerHTML").contains(text)) {
                saveElement = element;
                break;
            }
        }
        saveElement.click();
    }

    @When("^I wait for \"(.*)\"")
    public void waitFor(String text) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Saved']"));
    }

    @When("^I enter value \"(.*)\" in the edit box for the file \"(.*)\"$")
    public void enterInEditBox(String text, String file) {
        jwalaUi.click(By.xpath("//*[text()='{']"));
        jwalaUi.sendKeys(Keys.DELETE);
        jwalaUi.sendKeys("{");
        jwalaUi.sendKeys(Keys.ENTER);
        jwalaUi.sendKeys(text + ",");
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

}