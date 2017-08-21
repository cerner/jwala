package com.cerner.jwala.ui.selenium.operation;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Sharvari Barve on 6/27/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/operations/wsStatus.feature"},
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class StatusTest {
}
