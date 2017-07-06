package com.cerner.jwala.ui;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by Jedd Cuison on 7/6/2017
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"html:build/cucumber-report"},
                 features = {"classpath:com/cerner/jwala/ui/selenium"},
                 glue = {"com.cerner.jwala.ui.selenium.steps"})
public class JwalaUiMainTest {}
