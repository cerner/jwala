package com.cerner.jwala.ui.selenium.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Created by Jedd Cuison on 6/21/2017
 */
public class LogoutRunSteps {

    @Given("I am on the main page")
    public void isOnMainPage() {
        // waitUntilElementIsVisible(By.className("banner-logout"));
    }

    @When("I click the logout link")
    public void clickLogoutButton() {
        // clickWhenReady(By.linkText("Logout"));
    }

    @Then("I am redirected to the login page")
    public void validateResult() {
        // waitUntilElementIsVisible(By.xpath("//input[@value='Log In']"));
    }
}
