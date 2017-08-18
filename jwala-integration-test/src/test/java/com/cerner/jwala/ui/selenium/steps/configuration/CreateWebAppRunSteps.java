package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by Jedd Cuison on 7/5/2017
 */
public class CreateWebAppRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the web apps tab$")
    public void goToWebAppsTab() {
        jwalaUi.clickTab("Web Apps");
    }

    @When("^I click the add web app button$")
    public void clickAddWebAppBtn() {
        jwalaUi.click(By.xpath("//button[span[text()='Add']]"));
    }

    @And("^I see the web app add dialog$")
    public void checkForWebAppDialog() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Add Web Application']"));
    }

    @And("^I fill in the web app \"Name\" field with \"(.*)\"$")
    public void setWebAppName(final String name) {
        jwalaUi.sendKeys(By.name("name"), name);
    }

    @And("^I fill in the web app \"Context Path\" field with \"(.*)\"$")
    public void setContextPath(final String contextPath) {
        jwalaUi.sendKeys(By.name("webappContext"), contextPath);
    }

    @And("^I associate the web app to the following groups:$")
    public void setGroups(final List<String> groups) {
        for (final String group: groups) {
            jwalaUi.click(By.xpath("//div[text()='" + group + "']/input"));
        }
    }

    @And("^I click the add web app dialog ok button$")
    public void clickAddDialogOkBtn() {
        jwalaUi.click(By.xpath("//span[text()='Ok']"));
    }

    @And("^I see the following web app details in the web app table:$")
    public void checkForWebApp(final Map<String, String> webAppDetails) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + webAppDetails.get("name") + "']"));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + webAppDetails.get("context") + "']")));
        assertTrue(jwalaUi.isElementExists(By.xpath("//td[text()='" + webAppDetails.get("group") + "']")));
    }

    @And("^I see \"(.*)\" web app table$")
    public void checkForWebApp(final String webAppName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//button[text()='" + webAppName + "']"));
    }
}
