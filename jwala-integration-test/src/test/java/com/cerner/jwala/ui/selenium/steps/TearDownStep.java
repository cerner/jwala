package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import cucumber.api.java.After;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Jedd Cuison on 8/7/2017
 */
public class TearDownStep {

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }
}
