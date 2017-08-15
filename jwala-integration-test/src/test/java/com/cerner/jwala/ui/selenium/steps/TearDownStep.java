package com.cerner.jwala.ui.selenium.steps;

import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.ui.selenium.SeleniumTestHelper;
import cucumber.api.java.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Jedd Cuison on 8/7/2017
 */
public class TearDownStep {

    @Autowired
    private JschService jschService;

    @Autowired
    @Qualifier("seleniumTestProperties")
    private Properties props;

    @After
    public void afterScenario() throws SQLException, IOException, ClassNotFoundException {
        final List<ServerInfo> webServerNameList;
        final List<ServerInfo> jvmNameList;

        try {
            webServerNameList = getStoppedWebServers();
            jvmNameList = getStoppedJvms();
        } catch (final IOException | ClassNotFoundException | SQLException e) {
            throw new TearDownException("There was an error in retrieving the list of web servers from the database!", e);
        }

        final String sshUser = props.getProperty("ssh.user.name");
        final String sshPwd = props.getProperty("ssh.user.pwd");

        System.setProperty("PROPERTIES_ROOT_PATH", this.getClass().getResource("/selenium/vars.properties").getPath()
                .replace("/vars.properties", ""));
        for (final ServerInfo serverInfo : webServerNameList) {
            final RemoteSystemConnection remoteSystemConnection
                    = new RemoteSystemConnection(sshUser, sshPwd, serverInfo.host, 22);
            jschService.runShellCommand(remoteSystemConnection, "sc delete " + serverInfo.name, 300000);
        }

        for (final ServerInfo serverInfo : jvmNameList) {
            final RemoteSystemConnection remoteSystemConnection
                    = new RemoteSystemConnection(sshUser, sshPwd, serverInfo.host, 22);
            jschService.runShellCommand(remoteSystemConnection, "sc delete " + serverInfo.name, 300000);
        }

        SeleniumTestHelper.runSqlScript(this.getClass().getClassLoader().getResource("./selenium/cleanup.sql").getPath());
    }

    /**
     * Get stopped web servers from db
     * @return list of stopped web servers
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private List<ServerInfo> getStoppedWebServers() throws IOException, ClassNotFoundException, SQLException {
        final Properties properties = SeleniumTestHelper.getProperties();
        Class.forName(properties.getProperty("jwala.db.driver"));
        final String connectionStr = properties.getProperty("jwala.db.connection");
        final String userName = properties.getProperty("jwala.db.userName");
        final String password = properties.getProperty("jwala.db.password");
        final List<ServerInfo> webServerNameList = new ArrayList<>();
        try (final Connection conn = DriverManager.getConnection(connectionStr, userName, password)) {
            final Statement stmt = conn.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT NAME, HOST FROM WEBSERVER WHERE STATE='WS_UNREACHABLE'");
            while (resultSet.next()) {
                webServerNameList.add(new ServerInfo(resultSet.getString("NAME"), resultSet.getString("HOST")));
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
    private List<ServerInfo> getStoppedJvms() throws IOException, ClassNotFoundException, SQLException {
        final Properties properties = SeleniumTestHelper.getProperties();
        Class.forName(properties.getProperty("jwala.db.driver"));
        final String connectionStr = properties.getProperty("jwala.db.connection");
        final String userName = properties.getProperty("jwala.db.userName");
        final String password = properties.getProperty("jwala.db.password");
        final List<ServerInfo> jvmNameList = new ArrayList<>();
        try (final Connection conn = DriverManager.getConnection(connectionStr, userName, password)) {
            final Statement stmt = conn.createStatement();
            final ResultSet resultSet = stmt.executeQuery("SELECT NAME, HOSTNAME FROM JVM WHERE STATE='JVM_STOPPED'");
            while (resultSet.next()) {
                jvmNameList.add(new ServerInfo(resultSet.getString("NAME"), resultSet.getString("HOSTNAME")));
            }
        }
        return jvmNameList;
    }

    /**
     * Wrapper to hold server and host name
     */
    private static class ServerInfo {
        final String name;
        final String host;

        public ServerInfo(final String name, final String host) {
            this.name = name;
            this.host = host;
        }
    }
}
