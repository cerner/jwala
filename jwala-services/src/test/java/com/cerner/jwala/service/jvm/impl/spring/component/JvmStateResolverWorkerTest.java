package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test for {@link JvmStateResolverWorker}.
 *
 * Created by Jedd Cuison on 4/18/2016.
 */
public class JvmStateResolverWorkerTest {

    private JvmStateResolverWorker jvmStateResolverWorker;

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private Jvm mockJvm;

    @Mock
    private JvmStateService mockJvmStateService;

    @Mock
    private ClientHttpResponse mockResponse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        jvmStateResolverWorker = new JvmStateResolverWorker(mockClientFactoryHelper);
    }

    @Test
    public void testPingAndUpdateJvmStateNew() throws ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_NEW, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusOk() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusNotOk() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(0, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_STOPPED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusNotOkAndRetCodeNotZero() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_UNKNOWN, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusWithIoE() throws IOException, ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException());
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_UNKNOWN, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusWithRuntimeException() throws IOException, ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException());
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertNull(future.get());
    }
}
