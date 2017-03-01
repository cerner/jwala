package com.cerner.jwala.control.webserver.impl;

import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ShellCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.DefaultExecCommandBuilderImpl;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultWebServerExecRequestBuilderImplTest {

    private WebServer webServer;
    private DefaultExecCommandBuilderImpl impl;
    private String webServerName;

    @Before
    public void setup() throws IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getCanonicalPath() + "/src/test/resources");
        impl = new DefaultExecCommandBuilderImpl();
        webServer = mock(WebServer.class);
        webServerName = "theWebServerName";

        when(webServer.getName()).thenReturn(webServerName);
    }

    @Test
    public void testStart() throws Exception {

        final WebServerControlOperation operation = WebServerControlOperation.START;

        impl.setEntityName(webServer.getName());
        impl.setOperation(operation);

//        final ExecCommand actualCommand = impl.build(new WindowsWebServerPlatformCommandProvider());
        final ExecCommand expectedCommand =
                new ShellCommand("`/usr/bin/cygpath ~/.jwala/start-service.sh`",
                        "\"" + webServerName + "\"", "20");

  //      assertEquals(expectedCommand.toCommandString(), actualCommand.toCommandString());
    }

    @Test
    public void testStop() throws Exception {

        final WebServerControlOperation operation = WebServerControlOperation.STOP;

        impl.setEntityName(webServer.getName());
        impl.setOperation(operation);

     //   final ExecCommand actualCommand = impl.build(new WindowsWebServerPlatformCommandProvider());
        final ShellCommand expectedCommand =
                new ShellCommand("`/usr/bin/cygpath ~/.jwala/stop-service.sh`",
                        "\"" + webServerName + "\"", "20");

       // assertEquals(expectedCommand.toCommandString(), actualCommand.toCommandString());
    }

}
