package com.cerner.jwala.ui.selenium.operation;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:com/cerner/jwala/ui/selenium/operations/deleteInOperations.feature"},
        glue = {"com.cerner.jwala.ui.selenium.steps"}, strict = true, tags = {"~@ignore"})
public class DeleteInOperationsTest {}
