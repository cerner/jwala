package com.cerner.jwala.ui.selenium.steps;

import static com.cerner.jwala.common.domain.model.webserver.WebServerReachableState.*;
import static com.cerner.jwala.common.domain.model.jvm.JvmState.*;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.JschServiceException;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import cucumber.api.java.After;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Jedd Cuison on 8/7/2017
 */
public class TearDownStep {

    private static final String SHELL_READ_SLEEP_DEFAULT_VALUE = "250";

    @Autowired
    private JschService jschService;

    @Autowired
    @Qualifier("seleniumTestProperties")
    private Properties props;

    final Properties properties;
    final String connectionStr;
    final String userName;
    final String password;

    private final static Logger LOGGER = LoggerFactory.getLogger(TearDownStep.class);

    public TearDownStep() throws IOException, ClassNotFoundException {
        properties = SeleniumTestHelper.getProperties();
        Class.forName(properties.getProperty("jwala.db.driver"));
        connectionStr = properties.getProperty("jwala.db.connection");
        userName = properties.getProperty("jwala.db.userName");
        password = properties.getProperty("jwala.db.password");

        // indirectly required by JschServiceImpl via use of ApplicationProperties
        System.setProperty("PROPERTIES_ROOT_PATH", this.getClass().getResource("/selenium/vars.properties").getPath()
                .replace("/vars.properties", ""));
    }

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        final List<ServiceInfo> webServerNameList;
        final List<ServiceInfo> jvmNameList;

        LOGGER.info("Running after scenario...");

        try {
            webServerNameList = getWebServers();
            jvmNameList = getJvms();

            LOGGER.info("Web server name list size = {}", webServerNameList.size());
            LOGGER.info("JVM name list size = {}", jvmNameList.size());
        } catch (final IOException | ClassNotFoundException | SQLException e) {
            throw new TearDownException("There was an error in retrieving the list of web servers from the database!", e);
        }

        final String sshUser = props.getProperty("ssh.user.name");
        final String sshPwd = props.getProperty("ssh.user.pwd");

        for (final ServiceInfo serviceInfo : webServerNameList) {
            LOGGER.info("deleting web server \"{}\" service...", serviceInfo.name);
            deleteService(sshUser, sshPwd, serviceInfo);
        }

        for (final ServiceInfo serviceInfo : jvmNameList) {
            LOGGER.info("deleting JVM \"{}\" service...", serviceInfo.name);
            deleteService(sshUser, sshPwd, serviceInfo);
        }

        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

    /**
     * Delete an OS service
     * @param sshUser the ssh user
     * @param sshPwd the ssh password
     * @param serviceInfo information on the service to delete
     */
    private void deleteService(String sshUser, String sshPwd, ServiceInfo serviceInfo) {
        final RemoteSystemConnection remoteSystemConnection
                = new RemoteSystemConnection(sshUser, sshPwd, serviceInfo.host, 22);

        RemoteCommandReturnInfo remoteCommandReturnInfo = null;
        try {
            remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "uname", 10000);
        }
        catch (JschServiceException jsch){
            LOGGER.info("Unable to determine the os, hostname may be invalid");
            return;
        }
        LOGGER.info("Executed uname, result = {}", remoteCommandReturnInfo.standardOuput);
        final JwalaOsType osType = StringUtils.indexOf(remoteCommandReturnInfo.standardOuput, "CYGWIN") > -1 ?
                JwalaOsType.WINDOWS : JwalaOsType.UNIX;
        LOGGER.info("OS type = {}", osType);

        if (serviceInfo.isStarted()) {
            // Stop the service first so that service delete is executed by the OS asap
            switch (osType) {
                case WINDOWS:
                    remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sc stop " + serviceInfo.name, 300000);
                    break;
                case UNIX:
                    remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sudo service " + serviceInfo.name + " stop", 300000);
                    break;
                default:
                    throw new TearDownException(
                            MessageFormat.format("I don't know how to stop services for os type -> {0}", osType));
            }
            LOGGER.info("Shell command executed = {}", remoteCommandReturnInfo.standardOuput);
        }

        // If the service state is NEW, that means that the service has not yet been created so let's not issue a
        // delete command to save some time
        if (!serviceInfo.isNew()) {
            switch (osType) {
                case WINDOWS:
                    remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sc delete " + serviceInfo.name, 300000);
                    break;
                case UNIX:
                    remoteCommandReturnInfo = jschService.runShellCommand(remoteSystemConnection, "sudo rm /etc/init.d/" + serviceInfo.name, 300000);
                    break;
                default:
                    throw new TearDownException(
                            MessageFormat.format("I don't know how to delete services for os type -> {0}", osType));
            }
            LOGGER.info("Shell command executed = {}", remoteCommandReturnInfo.standardOuput);
        }
    }

    /**
     * Get stopped web servers from db
     * @return list of stopped web servers
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private List<ServiceInfo> getWebServers() throws IOException, ClassNotFoundException, SQLException {
        final List<ServiceInfo> webServerNameList = new ArrayList<>();
        try (final Connection conn = DriverManager.getConnection(connectionStr, userName, password)) {
            final Statement stmt = conn.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT NAME, HOST, STATE FROM WEBSERVER");
            while (resultSet.next()) {
                webServerNameList.add(new ServiceInfo(resultSet.getString("NAME"), resultSet.getString("HOST"),
                        resultSet.getString("STATE")));
            }
        }
        return webServerNameList;
    }

    /**
     * Get stopped JVMs from db
     * @return list of stopped JVMs
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private List<ServiceInfo> getJvms() throws IOException, ClassNotFoundException, SQLException {
        final List<ServiceInfo> jvmNameList = new ArrayList<>();
        try (final Connection conn = DriverManager.getConnection(connectionStr, userName, password)) {
            final Statement stmt = conn.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT NAME, HOSTNAME, STATE FROM JVM");
            while (resultSet.next()) {
                jvmNameList.add(new ServiceInfo(resultSet.getString("NAME"), resultSet.getString("HOSTNAME"),
                        resultSet.getString("STATE")));
            }
        }
        return jvmNameList;
    }

    /**
     * Wrapper to hold OS service name, host name and state
     */
    private static class ServiceInfo {
        final String name;
        final String host;
        final String state;

        public ServiceInfo(final String name, final String host, final String state) {
            this.name = name;
            this.host = host;
            this.state = state;
        }

        boolean isStarted() {
            return state.equalsIgnoreCase(WS_REACHABLE.name()) || state.equalsIgnoreCase(JVM_STARTED.name());
        }

        boolean isNew() {
            return state.equalsIgnoreCase(WS_NEW.name()) || state.equalsIgnoreCase(JVM_NEW.name());
        }
    }
}