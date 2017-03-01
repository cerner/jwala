package com.cerner.jwala.listener;

import org.apache.catalina.*;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created on 10/10/2016.
 */
public class H2LifecycleListenerTest {

    private H2LifecycleListener listener;
    private static final String DEFAULT_WEBSERVER_PARAM = "-webSSL,-webPort,8888";
    private static final String DEFAULT_TCPSERVER_PARAM = "-tcpPort,9999";

    @Before
    public void setup(){
        listener = new H2LifecycleListener();
        listener.setTcpServerParam(DEFAULT_TCPSERVER_PARAM);
        listener.setWebServerParam(DEFAULT_WEBSERVER_PARAM);
    }

    @Test
    public void testLifecycleEvent() {

        // test starting the servers
        Lifecycle mockLifeCycle = mock(Lifecycle.class);
        when(mockLifeCycle.getState()).thenReturn(LifecycleState.STARTING_PREP);
        LifecycleEvent mockLifecycleEvent = new LifecycleEvent(mockLifeCycle, "JVM STATE", "JVM STATE DATA");

        listener.lifecycleEvent(mockLifecycleEvent);

        // test stopping the servers
        when(mockLifeCycle.getState()).thenReturn(LifecycleState.DESTROYING);

        listener.lifecycleEvent(mockLifecycleEvent);

        // just verify the getState gets called for now so we can't access the servers from the listener ...
        verify(mockLifeCycle, times(2)).getState();
    }

}
