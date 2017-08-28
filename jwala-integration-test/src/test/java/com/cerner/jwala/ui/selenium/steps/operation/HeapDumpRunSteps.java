package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.ui.selenium.component.JwalaUi;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

/**
 * Created by Sharvari Barve on 7/18/2017.
 */
public class HeapDumpRunSteps {

    @Autowired
    @Qualifier("parameterProperties")
    private Properties paramProp;

    @Autowired
    private JwalaUi jwalaUi;

    @When("^I click on heap dump of \"(.*)\" jvm of \"(.*)\" group$")
    public void clickHeapDump(String jvmName, String groupName) {
        jwalaUi.clickWhenReady(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='" + jvmName + "']/following-sibling::td//button[@title='Heap Dump']"));
    }


    @Then("^I see heap dump popup for jvm \"(.*)\"$")
    public void verifyHeapDumpPopup(String jvm) {
        jwalaUi.isElementExists(By.xpath("//*[contains(text(),'Dumping heap to " + paramProp.get("resource.deploy.path") + "/" + jvm + "/" + "heapDump" + "']"));
        jwalaUi.isElementExists(By.xpath("/contains(text(),'Heap dump file created')"));
    }

    @Then("^I see heap dump error for jvm$")
    public void verfiyHeapDumpError() {
        jwalaUi.isElementExists(By.xpath("/contains(text(),'Oops! Something went wrong! The JVM might not have been started.')"));
    }
}
