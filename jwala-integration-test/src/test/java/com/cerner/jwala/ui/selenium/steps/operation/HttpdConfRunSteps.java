package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class HttpdConfRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click on the httpd.conf of the webserver \"(.*)\" of the group \"(.*)\"$")
    public void clickHttpdConfOfWebServer(final String webserverName, final String groupName){
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='"+groupName+"']]/following-sibling::tr//td[text()='"+webserverName+"']/following-sibling::td//button[text()='httpd.conf']"));
    }


    @Then("^I see the proper httpd.conf$")
    public void verifyProperConfFile() {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'This is the main Apache HTTP server configuration file.')]"));
    }

    @Then("^I see error while reading conf file$")
    public void verifyHttpdConfError() {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Error reading httpd.conf:')]"));
    }
}
