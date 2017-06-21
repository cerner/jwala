package com.cerner.jwala.ui.selenium.steps;

import cucumber.api.java.en.Given;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
public class CommonRunSteps {

    final private LoginRunSteps loginRunSteps = new LoginRunSteps();

    @Given("^I am logged in$")
    public void isLoggedIn() {
        loginRunSteps.loadLoginPage();
        loginRunSteps.enterUserName();
        loginRunSteps.enterPassword();
        loginRunSteps.clickLoginButton();
        loginRunSteps.validateResult();
    }

}
