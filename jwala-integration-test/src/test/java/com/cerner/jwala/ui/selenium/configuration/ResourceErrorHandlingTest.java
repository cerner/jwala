package com.cerner.jwala.ui.selenium.configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by SB053052 on 9/3/2017.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/configuration/resourceErrorHandling.feature"},
        glue = {"com.cerner.jwala.ui.selenium.steps"}, tags = {"~@ignore"}, strict = true)
public class ResourceErrorHandlingTest {
}
