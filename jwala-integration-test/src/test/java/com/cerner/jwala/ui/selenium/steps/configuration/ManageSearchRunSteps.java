package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Created by Sharvari Barve on 7/5/2017.
 */
public class ManageSearchRunSteps {
    @Autowired
    private JwalaUi jwalaUi;

    @When("^I fill in the search field with \"(.*)\"$")
    public void enterSearchString(String searchString) {
        jwalaUi.clickWhenReady(By.xpath("//input"));
        jwalaUi.sendKeys(searchString);
    }

    @Then("^I don't see \"(.*) in the table$")
    public void checkIfElementIsNotPresent(final String name) {
        assertEquals(false, jwalaUi.isElementExists(By.xpath("//button[text()='" + name + "']")));
    }

}
