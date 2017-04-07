package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link WebServerCommandServiceImpl}.
 * <p/>
 * Created by Jedd Cuison on 8/27/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebServerRequestServiceImplTest.Config.class})
public class WebServerRequestServiceImplTest {

    @Mock
    private WebServerService webServerService;

    @Mock
    private JschBuilder jschBuilder;

    @Mock
    private JSch jSch;

    @Mock
    private SshConfiguration sshConfig;

    @Mock
    private WebServer aWebServer;

    @Mock
    private ClientHttpRequest request;

    @Mock
    private ClientHttpResponse clientHttpResponse;

    @Mock
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Mock
    private RemoteCommandExecutorService mockRemoteCommandExecutorService;

    final private Identifier<WebServer> id = new Identifier<>(1L);

    private WebServerCommandServiceImpl impl;

    @Autowired
    @Qualifier("factoryHelper")
    private ClientFactoryHelper factoryHelper;

    public WebServerRequestServiceImplTest() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, this.getClass().getClassLoader()
                .getResource("vars.properties").getPath().replace("vars.properties", ""));
    }

    @Before
    public void setup() throws JSchException, CommandFailureException {
        MockitoAnnotations.initMocks(this);

        when(aWebServer.getName()).thenReturn("Apache2.2");
        when(webServerService.getWebServer(eq(id))).thenReturn(aWebServer);
        when(jschBuilder.build()).thenReturn(jSch);

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class)))
                .thenReturn(new RemoteCommandReturnInfo(0, "The content of httpd.conf", null));

        assertNotNull(factoryHelper);
        impl = new WebServerCommandServiceImpl(webServerService, sshConfig,
                mockRemoteCommandExecutorService);
    }

    @Test
    @Ignore
    public void testSecureCopyHttpdConf() throws CommandFailureException, IOException, URISyntaxException {
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(webServerService.getWebServer(anyString())).thenReturn(aWebServer);
        when(aWebServer.getStatusUri()).thenReturn(new URI("http://context/status.png"));

        // TODO move to web server control service impl test
        /*final CommandOutput execData = impl.secureCopyFile("ANY-SERVER-NAME", "d:/path/with/forward/slashes/new-httpd.conf", rtCommandBuilder);
        assertEquals("Expecting no errors so standard out should be empty", "", execData.getStandardOutput());
        assertEquals("Expecting no errors so standard error should be empty", "", execData.getStandardError());*/
    }

    // TODO do we need this anymore??
    @Configuration
    static class Config {
        @Mock
        private static HttpComponentsClientHttpRequestFactory httpClientRequestFactory;

        public Config() {
            MockitoAnnotations.initMocks(this);
        }

        @Bean(name = "httpRequestFactory")
        public HttpComponentsClientHttpRequestFactory getHttpClientRequestFactory() {
            return httpClientRequestFactory;
        }

        @Bean
        public ClientFactoryHelper getClientFactoryHelper() {
            return new ClientFactoryHelper();
        }

        @Bean(name = "factoryHelper")
        public ClientFactoryHelper getClientFactoryHelper(final ClientFactoryHelper factoryHelper) {
            return factoryHelper;
        }
    }
}
