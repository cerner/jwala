package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

import static org.junit.Assert.assertFalse;

/**
 * Created by Sharvari Barve on 7/14/2017.
 */
public class DrainRunSteps {

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click on the drain button for all webservers in the group$")
    public void clickGroupDrainForWebServers() {
        jwalaUi.click(By.xpath("//button[span[text()='Drain Web Servers']]"));
    }

    @Then("^I see the drain message for webserver \"(.*)\" and host \"(.*)\"$")
    public void verifyDrainMessage(String webserverName, String host) {
        jwalaUi.isElementExists(By.xpath("//td[text()='" + webserverName + "']/following-sibling::td[contains(text(),'Drain request for https://" + host + "')]"));
    }

    @Then("^I see drain error for jvm \"(.*)\"$")
    public void verifyDrainError(String jvmName) {
        jwalaUi.isElementExists(By.xpath("//*contains(text(),'must be STARTED before attempting to drain users')"));
    }

    @Then("^I do not see drain error for webserver \"(.*)\"$")
    public void verifynoDrainError(String webserverName) {
        assertFalse(jwalaUi.isElementExists(By.xpath("//*contains(text(),'The target Web Server " + webserverName + " must be STARTED before attempting to drain users')")));
    }
}
