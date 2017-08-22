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
public class ManageDrainRunSteps {

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click on the drain button for all webservers in the group$")
    public void clickGroupDrainForWebServers() {
        jwalaUi.clickWhenReady(By.xpath("//button[span[text()='Drain Web Servers']]"));
    }

    @When("^I click on the drain button for the jvm \"(.*)\" within group \"(.*)\"$")
    public void clickDrain(String jvmName,String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling:://tr/td[text()='" + jvmName + "']/following-sibling::td[5]/div/button[@title='Drain']"));
    }

    @When("^I click on the drain button for the webserver \"(.*)\" within group \"(.*)\"$")
    public void clickDrainForWebserver(String webserver, String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling:://tr/td[text()='" + webserver + "']/following-sibling::td[5]/div/button[@title='Drain']"));
    }

    @Then("^I see the drain message for webserver \"(.*)\" and host \"(.*)\"$")
    public void verifyDrainMessage(String webserverName, String host) {
        jwalaUi.isElementExists(By.xpath("//td[text()='" + webserverName + "']/following-sibling::td[contains(text(),'Drain request for https://" + host + "')]"));
    }

    @Then("^I see drain error for jvm \"(.*)\" with webserver \"(.*)\" in group \"(.*)\"$")
    public void verifyDrainError(String jvmName, String webserverName, String groupName) {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Drain JVM " + jvmName + "')]"));
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'The target Web Server " + webserverName + " in group " + groupName + " must be STARTED before attempting to drain users')]"));
    }

    @Then("^I do not see drain error for webserver \"(.*)\"$")
    public void verifynoDrainError(String webserverName) {
        assertFalse(jwalaUi.isElementExists(By.xpath("//*contains(text(),'The target Web Server " + webserverName + " must be STARTED before attempting to drain users')")));
    }
}
