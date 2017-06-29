package com.cerner.jwala.ui.selenium;

import org.junit.AfterClass;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Jedd Cuison on 6/28/2017
 */
public class Test {

    @AfterClass
    public static void tearDown() throws SQLException, IOException, ClassNotFoundException {
        // Do clean up here
        SeleniumTestHelper.runSqlScript(Test.class.getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }
}
