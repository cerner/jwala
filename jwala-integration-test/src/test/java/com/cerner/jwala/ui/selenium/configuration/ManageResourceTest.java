package com.cerner.jwala.ui.selenium.configuration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Rahul Sayini on 7/10/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/configuration/uploadResource.feature"},
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class ManageResourceTest {}
