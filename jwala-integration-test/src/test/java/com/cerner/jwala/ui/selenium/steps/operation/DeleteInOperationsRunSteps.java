package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertFalse;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class DeleteInOperationsRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click on yes button$")
    public void clickOkButton() {
        jwalaUi.click(By.xpath("//button[span[text()='Yes']]"));
    }

    @Then("^I see delete error$")
    public void deleteError() {
        jwalaUi.isElementExists(By.xpath("/contains(text(),'Please stop')"));
    }

    @Then("^I don't see delete error$")
    public void verifyNoError() {
        assertFalse(jwalaUi.isElementExists(By.xpath("/contains(text(),'Please stop')")));
    }

    @Then("^I see element in operations table\"(.*)\"$")
    public void verifyElementPresent(String name) {
        Assert.assertTrue(jwalaUi.isElementExists(By.xpath("/contains(text(),'" + name + "')")));
    }

    @Then("^I don't see \"(.*)\"$")
    public void verifyElementNotPresent(String name) {
        Assert.assertFalse(jwalaUi.isElementExists(By.xpath("/contains(text(),'" + name + "')")));
    }
}