package com.cerner.jwala.ui.selenium.steps.operations;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertFalse;

/**
 * Created by Sharvari Barve on 7/14/2017.
 */
public class ManageDrainRunSteps {
    @Autowired
    JwalaUi jwalaUi;


    @When("I click on the drain button for all webservers in the group")
    public void clickGroupDrain() {
        jwalaUi.click(By.xpath("//button[span[text()='Drain Web Servers']]"));
    }


    @Then("I see the drain message")
    public void verifyDrainMessage() {
        jwalaUi.isElementExists(By.xpath("//*contains(text(),'Drain request for' )"));

    }

    @Then("I see drain error")
    public void verifyDrainError() {
        jwalaUi.isElementExists(By.xpath("//*contains(text(),'must be STARTED before attempting to drain users')"));

    }

    @Then("I do not see drain error")
    public void verifynoDrainError() {
        assertFalse(jwalaUi.isElementExists(By.xpath("//*contains(text(),'must be STARTED before attempting to drain users')")));

    }

    @Then("I see the started  webserver \"(.*)\"$")
    public void waitUntilElementIsStarted(String webserver) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//tr/td[text()='" + webserver + "']//following-sibling::td[6]/div/span[contains(text(),'STARTED')"), (long) 240000);
    }

}



