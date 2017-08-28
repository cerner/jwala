package com.cerner.jwala.ui.selenium.admin;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Sharvari Barve
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/admin/admin.feature" },
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class AdminTest {}
