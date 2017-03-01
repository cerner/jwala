package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import com.cerner.jwala.tomcat.listener.messaging.MessagingServiceException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link JGroupsMessagingServiceImpl}
 *
 * Created by Jedd Cuison on 8/23/2016
 */
public class JGroupsMessagingServiceImplTest {

    private JGroupsMessagingServiceImpl messagingService;

    @Mock
    private JChannel mockChannel;

    @Before
    public void setup() {
        initMocks(this);
        messagingService = new JGroupsMessagingServiceImpl(mockChannel, "testCluster");
    }

    @Test
    public void testInit() throws Exception {
        when(mockChannel.isConnected()).thenReturn(false);
        messagingService.init();
        verify(mockChannel).connect(eq("testCluster"));
    }

    @Test
    public void testInitChannelAlreadyConnected() throws Exception {
        when(mockChannel.isConnected()).thenReturn(true);
        messagingService.init();
        verify(mockChannel, never()).connect(eq("testCluster"));
    }

    @Test(expected = MessagingServiceException.class)
    @SuppressWarnings("unchecked")
    public void testInitWithErrors() throws Exception {
        when(mockChannel.isConnected()).thenThrow(Exception.class);
        messagingService.init();
    }

    @Test
    public void testSend() throws Exception {
        final Message msg = new Message();
        messagingService.send(msg);
        verify(mockChannel).send(eq(msg));
    }

    @Test(expected = MessagingServiceException.class)
    public void testSendWithErrors() throws Exception {
        final Message msg = new Message();
        doThrow(Exception.class).when(mockChannel).send(msg);
        messagingService.send(msg);
    }

    @Test
    public void testDestroy() throws Exception {
        when(mockChannel.isConnected()).thenReturn(true);
        messagingService.destroy();
        verify(mockChannel).close();
    }

    @Test
    public void testDestroyChannelAlreadyDisconnected() throws Exception {
        when(mockChannel.isConnected()).thenReturn(false);
        messagingService.destroy();
        verify(mockChannel, never()).close();
    }

    @Test
    public void testGetChannel() {
        assertEquals(mockChannel, messagingService.getChannel());
    }
}