package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test for {@link JvmStateResolverWorker}.
 *
 * Created by Jedd Cuison on 4/18/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JvmStateResolverWorkerTest.Config.class})
public class JvmStateResolverWorkerTest {

    @Mock
    private Jvm mockJvm;

    @Mock
    private ClientHttpResponse mockResponse;

    @Autowired
    private JvmStateResolverWorker jvmStateResolverWorker;

    @Before
    public void setup() {
        initMocks(this);
        reset(Config.mockClientFactoryHelper, Config.mockHistoryFacadeService, Config.mockComponentsHttpClientRequestFactory,
                Config.mockMessagingService, Config.mockJvmStateService);
    }

    @Test
    public void testPingAndUpdateJvmStateNew() throws ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, Config.mockJvmStateService);
        assertEquals(JvmState.JVM_NEW, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusOk() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, Config.mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusNotOk() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(0, "STOPPED", "");
        when(Config.mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, Config.mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusNotOkAndRetCodeNotZero() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(Config.mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm,
                Config.mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusWithIoE() throws IOException, ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException());
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(Config.mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, Config.mockJvmStateService);
        assertEquals(JvmState.JVM_STOPPED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusWithRuntimeException() throws IOException, ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException());
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(Config.mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, Config.mockJvmStateService);
        assertNull(future.get());
    }

    @Configuration
    static class Config {
        static HttpComponentsClientHttpRequestFactory mockComponentsHttpClientRequestFactory =
                mock(HttpComponentsClientHttpRequestFactory.class);

        static ClientFactoryHelper mockClientFactoryHelper = mock(ClientFactoryHelper.class);

        static HistoryFacadeService mockHistoryFacadeService = mock(HistoryFacadeService.class);

        static MessagingService mockMessagingService = mock(MessagingService.class);

        static JvmStateService mockJvmStateService = mock(JvmStateService.class);

        @Bean(name = "httpRequestFactory")
        public HttpComponentsClientHttpRequestFactory getMockHttpClientRequestFactory() {
            return mockComponentsHttpClientRequestFactory;
        }

        @Bean
        public ClientFactoryHelper getMockClientFactoryHelper() {
            return mockClientFactoryHelper;
        }

        @Bean
        public HistoryFacadeService getMockHistoryService() {
            return mockHistoryFacadeService;
        }

        @Bean
        public MessagingService getMockMessagingService() {
            return mockMessagingService;
        }

        @Bean
        public JvmStateResolverWorker getJvmStateResolverWorker() {
            return new JvmStateResolverWorker();
        }

        @Bean
        public static JvmStateService getMockJvmStateService() {
            return mockJvmStateService;
        }
    }
}
