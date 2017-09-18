package com.cerner.jwala.ui.selenium.operation;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Jedd Cuison on 8/29/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = {"classpath:com/cerner/jwala/ui/selenium/operations/controlJvm.feature"},
                 glue = {"com.cerner.jwala.ui.selenium.steps"}, tags = {"~@ignore"}, strict = true)
public class ControlJvmTest {}
