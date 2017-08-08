package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by Rahul Sayini on 7/7/2017
 */
public class UploadResourceRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Given("^I am in the resource tab$")
    public void goToConfigurationTab() {
        jwalaUi.clickTab("Resources");
    }

    @And("^I expand \"(.*)\" node$")
    public void expandNode(final String nodeName) {
        jwalaUi.expandNode(nodeName);
    }

    @And("^I click \"(.*)\" node$")
    public void clickNode(final String nodeLabel) {
        jwalaUi.clickNode(nodeLabel);
    }

    @When("^I click the add resource button$")
    public void addResource() {
        jwalaUi.clickWhenReady(By.xpath("//span[contains(@class, 'ui-icon-plusthick')]"));
    }

    @And("^I fill in the \"Deploy Name\" field with \"(.*)\"$")
    public void setDeployName(final String deployName) {
        jwalaUi.sendKeys(By.name("deployFilename"), deployName);
    }

    @And("^I fill in the \"Deploy Path\" field with \"(.*)\"$")
    public void setDeployPath(final String deployPath) {
        jwalaUi.sendKeys(By.xpath("//label[text()='Deploy Path']/following-sibling::input"), paramProp.getProperty(deployPath));
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

    @And("^I check Upload Meta Data File$")
    public void clickUploadMetaDataFile() {
        jwalaUi.click(By.xpath("//div[contains(text(),'Upload Meta Data File')]/input"));
    }

    @And("^I choose the meta data file \"(.*)\"$")
    public void selectMetaDataFile(final String archiveFileName) {
        final Path mediaPath = Paths.get(jwalaUi.getProperties().getProperty("file.upload.dir") + "/" + archiveFileName);
        jwalaUi.sendKeys(By.name("metaDataFile"), mediaPath.normalize().toString());
    }

    @Then("^I see that the resource got uploaded successfully$")
    public void checkForResource() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//input[contains(@class, 'noSelect')]/following-sibling::span"));
    }
}
