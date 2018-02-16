package com.cerner.jwala.ui.selenium.steps.operation;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.ui.selenium.SeleniumTestCaseException;
import com.cerner.jwala.ui.selenium.component.JwalaUi;
import com.cerner.jwala.ui.selenium.steps.JwalaOsType;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

/**
 * Created by Jedd Cuison on 8/28/2017
 */
public class JvmControlRunSteps {

    @Autowired
    private JwalaUi jwalaUi;

    @Autowired
    private JschService jschService;

    @Autowired
    @Qualifier("seleniumTestProperties")
    private Properties props;

    private static final int SHORT_CONNECTION_TIMEOUT = 10000;
    private static final String SHELL_READ_SLEEP_DEFAULT_VALUE = "250";
    private final static Logger LOGGER = LoggerFactory.getLogger(JvmControlRunSteps.class);

    @When("^I click the \"(.*)\" button of JVM \"(.*)\" under group \"(.*)\" in the operations tab$")
    public void clickControlJvmBtn(final String buttonTitle, final String jvmName, final String groupName) {
        jwalaUi.click(By.xpath("//tr[td[text()='" + groupName + "']]/following-sibling::tr//td[text()='"
                + jvmName + "']/following-sibling::td//button[@title='" + buttonTitle + "']"));
    }

    @When("^I click the operation's confirm delete JVM dialog yes button$")
    public void clickConfirmJvmDeleteYesButton() {
        jwalaUi.clickYes();
    }

    @Then("^I see that \"(.*)\" JVM got deleted successfully from the operations tab$")
    public void verifyIfDeleteJvmIsSuccessful(final String jvmName) {
        jwalaUi.waitUntilElementIsVisible(
                By.xpath("//div[text()='JVM " + jvmName +
                        " was successfully deleted. Jwala will need to refresh to display the latest data and recompute the states.']"));
        checkJVMServiceDeleteWasSuccessful(jvmName);
    }

    private void checkJVMServiceDeleteWasSuccessful(String jvmName) {

        // indirectly required by JschServiceImpl via use of ApplicationProperties
        System.setProperty("PROPERTIES_ROOT_PATH", this.getClass().getResource("/selenium/vars.properties").getPath()
                .replace("/vars.properties", ""));

        final String sshUser = props.getProperty("ssh.user.name");
        final String sshPwd = props.getProperty("ssh.user.pwd");
        final String jvmHostname = props.getProperty("host1");

        assert StringUtils.isNotEmpty(sshUser);
        assert StringUtils.isNotEmpty(sshPwd);
        assert StringUtils.isNotEmpty(jvmHostname);

        final RemoteSystemConnection remoteSystemConnection
                = new RemoteSystemConnection(sshUser, sshPwd, jvmHostname, 22);

        RemoteCommandReturnInfo remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "uname", SHORT_CONNECTION_TIMEOUT);
        final JwalaOsType osType = StringUtils.indexOf(remoteCommandReturnInfo.standardOuput, "CYGWIN") > -1 ? JwalaOsType.WINDOWS : JwalaOsType.UNIX;

        if (osType.equals(JwalaOsType.WINDOWS)) {
            remoteCommandReturnInfo = jschService.runExecCommand(remoteSystemConnection, "sc queryex " + jvmName, SHORT_CONNECTION_TIMEOUT);
            if (!remoteCommandReturnInfo.standardOuput.contains("The specified service does not exist as an installed service")) {
                throw new SeleniumTestCaseException("Failed to delete JVM service " + jvmName + " on host " + jvmHostname);
            } else {
                LOGGER.info("STD_OUT sc queryex::" + remoteCommandReturnInfo.standardOuput);
            }

        } else {
            remoteCommandReturnInfo = jschService.runExecCommand(remoteSystemConnection, "sudo chkconfig --list " + jvmName, SHORT_CONNECTION_TIMEOUT);
            if (!remoteCommandReturnInfo.standardOuput.contains("error reading information on service " + jvmName + ": No such file or directory")) {
                throw new SeleniumTestCaseException("Failed to delete JVM " + jvmName + " from runlevel on host " + jvmHostname);
            } else {
                LOGGER.info("STD_OUT chkconfig::" + remoteCommandReturnInfo.standardOuput);
            }
            remoteCommandReturnInfo = jschService.runExecCommand(remoteSystemConnection, "sudo service " + jvmName + " status", SHORT_CONNECTION_TIMEOUT);
            if (!remoteCommandReturnInfo.standardOuput.contains(jvmName + ": unrecognized service")) {
                throw new SeleniumTestCaseException("Failed to delete JVM service " + jvmName + " on host " + jvmHostname);
            } else {
                LOGGER.info("STD_OUT service status::" + remoteCommandReturnInfo.standardOuput);
            }
        }

    }

}
