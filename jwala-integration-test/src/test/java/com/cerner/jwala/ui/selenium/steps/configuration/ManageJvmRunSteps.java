package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by RS045609 on 7/5/2017.
 */
public class ManageJvmRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the jvm tab$")
    public void goToJvmTab() {
        jwalaUi.clickTab("JVM");
    }

    @When("^I click the add jvm button$")
    public void clickAddJvmBtn() {
        jwalaUi.click(By.xpath("//span[text()='Add']"));
    }

    @And("^I see the jvm add dialog$")
    public void checkAddJvmDialogBoxIsDisplayed() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//span[text()='Add JVM']"));
    }
}
