package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Jedd Cuison on 6/30/2017
 */
public class CreateMediaRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the add media button$")
    public void clickAddMediaBtn() {
        jwalaUi.click(By.xpath("//span[text()='Add']"));
    }

    @And("^I see the media add dialog$")
    public void checkIfAddMediaDialogIsDisplayed() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Add Media']"));
    }

    @And("^I fill in the \"Media Name\" field with \"(.*)\"$")
    public void setMediaName(final String mediaName) {
        jwalaUi.sendKeys(By.name("name"), mediaName);
    }

    @And("^I select \"Media Type\" item \"(.*)\"$")
    public void selectMediaType(final String mediaType) {
        jwalaUi.selectItem(By.name("type"), mediaType);
    }

    @And("^I choose the media archive file \"(.*)\"$")
    public void selectMediaArchiveFile(final String archiveFileName) {
        final Path mediaPath = Paths.get(jwalaUi.getProperties().getProperty("media.source.dir") + "/" + archiveFileName);
        jwalaUi.sendKeys(By.name("mediaArchiveFile"), mediaPath.normalize().toString());
    }

    @And("^I fill in the \"Remote Directory\" field with \"(.*)\"$")
    public void setRemoteDir(final String remoteDir) {
        jwalaUi.sendKeys(By.name("remoteDir"), remoteDir);
    }

    @And("^I click the add media dialog ok button$")
    public void clickAddMediaOkDialogBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Ok']]"));
    }

    @Then("I see \"(.*)\" in the media table")
    public void checkForMedia(final String mediaName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + mediaName + "']"), 300);
    }
}
