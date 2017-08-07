package com.cerner.jwala.ui.selenium.operations;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Created by SB053052 on 7/14/2017.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:com/cerner/jwala/ui/selenium/operations/manager.feature" },
        glue = {"com.cerner.jwala.ui.selenium.steps"})
public class JvmManagerTest {
}
