package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class HeapDumpRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Then("^I see heap dump popup$")
    public void verifyHeapDumpPopup() {
        jwalaUi.isElementExists(By.xpath("/contains(text(),'Heap dump file created')"));
    }

    @Then("^I see heap dump error for jvm$")
    public void verfiyHeapDumpError() {
        jwalaUi.isElementExists(By.xpath("/contains(text(),'Oops! Something went wrong! The JVM might not have been started.')"));
    }
}
