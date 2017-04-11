package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.control.webserver.command.WebServerCommandFactory;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.VerificationBehaviorSupport;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.DistributionService;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionControlServiceImpl;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {WebServerControlServiceImplVerifyTest.Config.class})
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private static final String WEB_SERVER_NAME = "webServer1";
    private static final String HOST_NAME = "host1";
    private static final String SOURCE_DIR = "./source";
    private static final String DEST_DIR = "./dest";
    public static final String USER_ID = "user-id";
    public static final String UNIX_HOME_DEST_PATH = "~/dest";

    @Autowired
    private WebServerControlService webServerControlService;

    private User user;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        user = new User("unused");
    }

    @Test
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        String wsName = "mockWebServerName";
        String wsHostName = "mockWebServerHost";
        final ControlWebServerRequest controlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer webServer = mock(WebServer.class);

        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(webServer.getName()).thenReturn(wsName);
        when(webServer.getHost()).thenReturn(wsHostName);
        when(webServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);

        final Identifier<WebServer> webServerId = mock(Identifier.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;
        final ClientHttpResponse mockClientHttpResponse = mock(ClientHttpResponse.class);

        when(controlWebServerRequest.getWebServerId()).thenReturn(webServerId);
        when(controlWebServerRequest.getControlOperation()).thenReturn(controlOperation);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(Config.mockWebServerCommandFactory.executeCommand(webServer, controlOperation))
                .thenReturn(new RemoteCommandReturnInfo(0, "Start succeeded", ""));
        webServerControlService.controlWebServer(controlWebServerRequest, user);

        verify(Config.mockWebServerCommandFactory).executeCommand(webServer, controlOperation);
    }

    @Test
    public void testStart() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(webserver);
        when(Config.mockWebServerCommandFactory.executeCommand(eq(webserver), eq(WebServerControlOperation.START))).thenReturn(new RemoteCommandReturnInfo(0, "SUCCESS", ""));
        ControlWebServerRequest controlWSRequest = new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.START);
        CommandOutput result = webServerControlService.controlWebServer(controlWSRequest, user);
        assertEquals(new ExecReturnCode(0), result.getReturnCode());

        when(Config.mockWebServerCommandFactory.executeCommand(eq(webserver), eq(WebServerControlOperation.STOP))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_PROCESS_KILLED, "", "PROCESS KILLED"));
        controlWSRequest = new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.STOP);
        CommandOutput returnOutput = webServerControlService.controlWebServer(controlWSRequest, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());
        verify(Config.mockWebServerService).updateState(any(Identifier.class), eq(WebServerReachableState.FORCED_STOPPED), eq(""));

        when(Config.mockWebServerCommandFactory.executeCommand(eq(webserver), eq(WebServerControlOperation.START))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS, "", "ABNORMAL SUCCESS"));
        controlWSRequest = new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.START);
        result = webServerControlService.controlWebServer(controlWSRequest, user);
        verify(Config.mockHistoryFacadeService, times(1)).write(anyString(), anyList(), anyString(), eq(EventType.SYSTEM_ERROR), anyString());
        assertEquals(new ExecReturnCode(0), result.getReturnCode());

        when(Config.mockWebServerCommandFactory.executeCommand(any(WebServer.class), any(WebServerControlOperation.class))).thenReturn(new RemoteCommandReturnInfo(1, "", "ABNORMAL SUCCESS"));
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(Config.mockHistoryFacadeService, times(2)).write(anyString(), anyList(), anyString(), eq(EventType.SYSTEM_ERROR), anyString());
    }

    @Test
    public void testSecureCopy() throws CommandFailureException {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getHost()).thenReturn(HOST_NAME);
        when(mockWebServer.getName()).thenReturn(WEB_SERVER_NAME);
        when(Config.mockWebServerService.getWebServer(WEB_SERVER_NAME)).thenReturn(mockWebServer);
        when(Config.mockDistributionService.remoteFileCheck(HOST_NAME, DEST_DIR)).thenReturn(false);
        webServerControlService.secureCopyFile(WEB_SERVER_NAME, SOURCE_DIR, DEST_DIR, USER_ID);
        verify(Config.mockDistributionService, never()).backupFile(HOST_NAME, DEST_DIR);
        verify(Config.mockDistributionService).remoteSecureCopyFile(HOST_NAME, SOURCE_DIR, DEST_DIR);
    }

    @Test
    public void testSecureCopyPerformsBackup() throws CommandFailureException {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getHost()).thenReturn(HOST_NAME);
        when(mockWebServer.getName()).thenReturn(WEB_SERVER_NAME);
        when(Config.mockWebServerService.getWebServer(WEB_SERVER_NAME)).thenReturn(mockWebServer);
        when(Config.mockDistributionService.remoteFileCheck(HOST_NAME, DEST_DIR)).thenReturn(true);
        webServerControlService.secureCopyFile(WEB_SERVER_NAME, SOURCE_DIR, DEST_DIR, USER_ID);
        verify(Config.mockDistributionService).backupFile(HOST_NAME, DEST_DIR);
        verify(Config.mockDistributionService).remoteSecureCopyFile(HOST_NAME, SOURCE_DIR, DEST_DIR);
    }

    @Test(expected = InternalErrorException.class)
    public void testSecureCopyFailsBackup() throws CommandFailureException {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn(WEB_SERVER_NAME);
        when(mockWebServer.getHost()).thenReturn(HOST_NAME);
        when(Config.mockWebServerService.getWebServer(WEB_SERVER_NAME)).thenReturn(mockWebServer);
        doThrow(new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, StringUtils.EMPTY))
                .when(Config.mockDistributionService).remoteSecureCopyFile(HOST_NAME, SOURCE_DIR, DEST_DIR);
        webServerControlService.secureCopyFile(WEB_SERVER_NAME, SOURCE_DIR, DEST_DIR, USER_ID);
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getHost()).thenReturn("test-host");
        webServerControlService.changeFileMode(mockWebServer, "777", "./target", "*");
        verify(Config.mockDistributionService).changeFileMode("test-host", "777", "./target", "*");
    }

    @Test
    public void testCreateDirectory() throws CommandFailureException {
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getHost()).thenReturn(HOST_NAME);
        webServerControlService.createDirectory(mockWebServer, DEST_DIR);
        verify(Config.mockDistributionService).remoteCreateDirectory(HOST_NAME, DEST_DIR);
    }

    @Test
    public void testSecureCopyHomeDir() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), WEB_SERVER_NAME);
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webserver);

        webServerControlService.secureCopyFile(WEB_SERVER_NAME, SOURCE_DIR, UNIX_HOME_DEST_PATH, USER_ID);
    }

    @Test(expected = InternalErrorException.class)
    public void testSecureCopyCreateParentFail() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), WEB_SERVER_NAME);
        when(Config.mockWebServerService.getWebServer(anyString())).thenReturn(webserver);
        when(Config.mockDistributionService.remoteFileCheck(anyString(), anyString()))
                .thenThrow(new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, StringUtils.EMPTY));
        webServerControlService.secureCopyFile(WEB_SERVER_NAME, SOURCE_DIR, DEST_DIR, USER_ID);
    }

    @Test
    public void testWaitForState() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.START);
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 120L);
        assertTrue(result);
    }

    @Test
    public void testWaitForStateFail() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.STOP);
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 5L);
        assertFalse(result);
    }

    @Test
    public void testWaitStateForStop() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.STOP);
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 5L);
        assertTrue(result);
    }

    @Test
    public void testWaitStateForForcedStop() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.STOP);
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.FORCED_STOPPED);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 5L);
        assertTrue(result);
    }

    @Test(expected = InternalErrorException.class)
    public void testWaitStateForUnexpectedOperation() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.BACK_UP);
        when(Config.mockWebServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.FORCED_STOPPED);
        webServerControlService.waitForState(mockControlWebServerRequest, 5L);
    }

    @Configuration
    static class Config {

        @Mock
        static WebServerCommandFactory mockWebServerCommandFactory;

        @Mock
        static DistributionService mockDistributionService;

        @Mock
        static WebServerService mockWebServerService;

        @Mock
        static HistoryFacadeService mockHistoryFacadeService;

        @Mock
        static RemoteCommandExecutorService mockRemoteCommandExecutorService;

        @Mock
        static SshConfiguration mockSshConfiguration;

        @Mock
        static SshConfig mockSshConfig;

        @Mock
        static BinaryDistributionControlServiceImpl binaryDistributionControlService;

        @Mock
        static ResourceService mockResourceService;

        @Mock
        static ResourceContentGeneratorService mockResourceContentGeneratorService;

        public Config() {
            initMocks(this);
        }

        @Bean
        public WebServerCommandFactory getMockWebServerCommandFactory() {
            return mockWebServerCommandFactory;
        }

        @Bean
        public DistributionService getMockDistributionService() {
            return mockDistributionService;
        }

        @Bean
        public WebServerService getMcokWebServerService() {
            return mockWebServerService;
        }

        @Bean
        public HistoryFacadeService getMockHistoryFacadeService() {
            return mockHistoryFacadeService;
        }

        @Bean
        public RemoteCommandExecutorService getMockRemoteCommandExecutorService() {
            return mockRemoteCommandExecutorService;
        }

        @Bean
        public SshConfiguration getMockSshConfiguration() {
            return mockSshConfiguration;
        }

        @Bean
        public SshConfig getMockSshConfig() {
            return mockSshConfig;
        }

        @Bean
        public BinaryDistributionControlService getBinaryDistributionControlService() {
            return binaryDistributionControlService;
        }

        @Bean
        @Scope("prototype")
        public WebServerControlService getWebServerControlService() {
            reset(mockWebServerCommandFactory, mockDistributionService, mockWebServerService,
                    mockHistoryFacadeService, mockRemoteCommandExecutorService,
                    mockSshConfig, binaryDistributionControlService, mockSshConfig);
            return new WebServerControlServiceImpl();
        }

        @Bean
        public ResourceService getResourceService() {
            return mockResourceService;
        }

        @Bean
        public ResourceContentGeneratorService getResourceContentGeneratorService() {
            return mockResourceContentGeneratorService;
        }

    }

}