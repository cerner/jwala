package com.cerner.jwala.ui.selenium.configuration;

import com.cerner.jwala.ui.selenium.Test;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Jedd Cuison on 6/27/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:com/cerner/jwala/ui/selenium/configuration/manageGroup.feature" },
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class ManageGroupTest extends Test {}
