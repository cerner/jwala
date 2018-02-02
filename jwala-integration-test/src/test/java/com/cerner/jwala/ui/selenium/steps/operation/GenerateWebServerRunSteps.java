package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/14/2017
 */
public class GenerateWebServerRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click the \"Generate Web Servers\" button of group \"(.*)\"$")
    public void clickGenerateWebServersBtnOfGroup(final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr/td//button[span[text()='Generate Web Servers']]"));
    }

    @Then("^I see that the web servers were successfully generated for group \"(.*)\"$")
    public void checkForTheSuccessfulGenerationOfWebServers(final String groupName) {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Successfully generated the web servers for " + groupName + "']"), 600);
        jwalaUi.clickOk();
    }

    @When("^I generate \"(.*)\" web server of \"(.*)\" group$")
    public void generateIndividualWebserver(final String webserver, final String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='" + webserver + "']/following-sibling::td//button[@title='Generate the httpd.conf and deploy as a service']"));
    }

    @Then("^I see the web server was successfully generated$")
    public void checkForSuccessfulGenerationOfAWebserver() {
        jwalaUi.waitUntilElementIsVisible(By.xpath("//div[text()='Successfully installed the service, and generated and deployed configuration file(s).']"), 600);
        jwalaUi.clickOk();
    }
}
