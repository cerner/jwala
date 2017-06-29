package com.cerner.jwala.ui.selenium.steps.configuration;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
public class ConfigurationRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Given("^I am in the configuration tab$")
    public void goToConfigurationTab() {
        jwalaUi.clickTab("Configuration");
    }
}
