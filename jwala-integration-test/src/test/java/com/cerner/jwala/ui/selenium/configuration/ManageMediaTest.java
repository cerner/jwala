package com.cerner.jwala.ui.selenium.configuration;


import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Jedd Cuison on 6/29/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/configuration/manageMedia.feature"},
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class ManageMediaTest {
}
