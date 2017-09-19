package com.cerner.jwala.ui.selenium.configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Jedd Cuison on 7/10/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:com/cerner/jwala/ui/selenium/configuration/manageJvm.feature" },
        glue = {"com.cerner.jwala.ui.selenium.steps"}, strict = true)
public class ManageJvmTest {}
