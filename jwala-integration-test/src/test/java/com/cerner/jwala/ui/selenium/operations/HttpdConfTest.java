package com.cerner.jwala.ui.selenium.operations;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Sharvari Barve on 7/14/2017.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/operations/httpdConf.feature"},
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class HttpdConfTest {
}
