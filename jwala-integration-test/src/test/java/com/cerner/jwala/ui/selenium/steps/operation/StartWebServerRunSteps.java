package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/15/2017
 */
public class StartWebServerRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("I click \"Start Web Servers\" button of group \"(.*)\"")
    public void clickStartWebServersOfGroup(final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName +
                "']]/following-sibling::tr//button[text()='Start Web Servers']"));
    }

    @When("^I click the start button of \"(.*)\" webserver of \"(.*)\" group$")
    public void clickStartIndvidualWebserver(final String webserverName, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webserverName + "']/following-sibling::td//button[@title='Start']"));
    }

    @Then("I see the state of \"(.*)\" web server of group \"(.*)\" is \"STARTED\"")
    public void checkIfWebServerStateIsStarted(final String webServerName, final String groupName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + webServerName + "']/following-sibling::td//span[text()='STARTED']"), 120);
    }
}
