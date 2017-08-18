package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class HttpdConfRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Then("^I see the proper httpd.conf")
    public void verifyConf() {
        jwalaUi.isElementExists(By.xpath("/contains(text(),'This is the main Apache HTTP server configuration file.')"));
    }

    @Then("^I see error while reading conf file")
    public void verifyError() {
        jwalaUi.isElementExists(By.xpath("/contains(text(),'Error reading httpd.conf:')"));
    }
}
