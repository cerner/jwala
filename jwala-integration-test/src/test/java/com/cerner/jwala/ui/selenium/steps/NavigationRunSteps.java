package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 8/18/2017
 */
public class NavigationRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the resource tab$")
    public void goToResourceTab() {
        jwalaUi.clickTab("Resources");
    }
}
