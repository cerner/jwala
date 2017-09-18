package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.And;
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

    @When("^I click the drain button of \"(.*)\" webserver under \"(.*)\" group$")
    public void clickDrainWebserverOfGroup(final String webserverName, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='" +
                webserverName + "']/following-sibling::td//button[@title='Drain']"));
    }

    @And("^I click the drain button of \"(.*)\" JVM under \"(.*)\" group$")
    public void clickDrainJvmOfGroup(final String jvmName, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='" +
                jvmName + "']/following-sibling::td//button[@title='Drain']"));
    }

    @Then("^I see the drain message for webserver \"(.*)\" and host \"(.*)\"$")
    public void verifyDrainMessage(String webserverName, String host) {
        jwalaUi.isElementExists(By.xpath("//td[text()='" + webserverName + "']/following-sibling::td[contains(text(),'Drain request for https://" + host + "')]"));
    }

    @And("^I do not see an error message after clicking drain$")
    public void verifyNoDrainError() {
        // Error has to be generic so it applies to any error message
        assertFalse(jwalaUi.isElementExists(By.xpath("//div[contains(@class, 'ui-state-error')]"), 10));
    }
}
