package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Created by Jedd Cuison on 6/26/2017
 */
public class BatchLoginRunSteps {

    private final List<String[]> userAccounts = new ArrayList<>();
    private final List<String> results = new ArrayList<>();

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I load predefined user accounts$")
    public void loadUserAccounts() throws IOException {
<<<<<<< HEAD
        try (final Stream<String> stream = Files.lines(Paths.get("c:/selenium/user-accounts.csv"))) {
=======
        try (final Stream<String> stream = Files.lines(Paths.get(jwalaUi.getProperties().getProperty("file.upload.dir")
                + "/user-accounts.csv"))) {
>>>>>>> master
            stream.skip(1).forEach(item -> userAccounts.add(item.split(",")));
        }
    }

    @Then("^I use those accounts to login successfully and unsuccessfully$")
    public void loginWithDifferentUserAccounts() {
        for (String [] userAccount : userAccounts) {
            jwalaUi.loadPath("/login");
            jwalaUi.sendKeys(By.id("userName"), userAccount[0]);
            jwalaUi.sendKeys(By.id("password"), userAccount[1]);
            jwalaUi.click(By.cssSelector("input[type=\"button\"]"));
            if (userAccount[2].equalsIgnoreCase("mainPage")) {
                jwalaUi.waitUntilElementIsVisible(By.className("banner-logout"));
            } else if (userAccount[2].equalsIgnoreCase("loginErrorMessage")) {
                jwalaUi.waitUntilElementIsVisible(By.className("login-error-msg"));
            } else {
                fail("Unexpected result = " + userAccount[2]);
            }
        }
    }
}
