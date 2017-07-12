package com.cerner.jwala.ui.selenium;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Jedd Cuison on 6/21/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:com/cerner/jwala/ui/selenium/security.feature" },
        glue = {"com.cerner.jwala.ui.selenium.steps" })
public class SecurityTest {}
