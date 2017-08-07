package com.cerner.jwala.ui.selenium.steps.operations;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.security.UserAndPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by Sharvari Barve on 7/24/2017.
 */
public class ManagerRunSteps {
    @Autowired
    JwalaUi jwalaUi;


    @Autowired
    @Qualifier("parameterProperties")
    Properties paramProp;

    @When("^I verify url with port \"(.*)\" and with host \"(.*)\"$")
    public void login(String port, String host) {
        String url = jwalaUi.getWebDriver().getCurrentUrl();
        Alert alert = jwalaUi.getWebDriver().switchTo().alert();
        alert.authenticateUsing(new UserAndPassword(jwalaUi.getProperties().getProperty("jwala.user.name"), jwalaUi.getProperties().getProperty("jwala.user.pwd")));
    }


}
