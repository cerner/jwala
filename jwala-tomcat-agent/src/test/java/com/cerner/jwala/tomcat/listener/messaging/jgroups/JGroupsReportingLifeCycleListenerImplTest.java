package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleState;
import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Properties;

import static java.lang.Thread.sleep;
import static org.jgroups.util.Util.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test for {@link JGroupsReportingLifeCycleListener}
 *
 * Created by Jedd Cuison on 8/24/2016.
 */
public class JGroupsReportingLifeCycleListenerImplTest {

    private final Properties prop = new Properties();
    private final JGroupsReportingLifeCycleListener lifeCycleListener = new JGroupsReportingLifeCycleListener();

    @Mock
    private Appender mockAppender;

    @Captor
    private ArgumentCaptor captorLoggingEvent;

    @Mock
    private Lifecycle mockLifeCycle;

    public JGroupsReportingLifeCycleListenerImplTest() throws IOException {
        prop.load(this.getClass().getResourceAsStream("/test.properties"));
    }

    @Before
    public void setup() {
        initMocks(this);

        lifeCycleListener.setServerId("1");
        lifeCycleListener.setServerName("testJvm");
        lifeCycleListener.setJgroupsPreferIpv4Stack("true");
        lifeCycleListener.setJgroupsConfigXml("tcp.xml");
        lifeCycleListener.setJgroupsCoordinatorIp(prop.getProperty("coordinator.ip"));
        lifeCycleListener.setJgroupsCoordinatorHostname("testHostname");
        lifeCycleListener.setJgroupsCoordinatorPort("30000");
        lifeCycleListener.setJgroupsClusterName("testCluster");
        lifeCycleListener.setSchedulerDelayInitial("1");
        lifeCycleListener.setSchedulerDelaySubsequent("1");
        lifeCycleListener.setSchedulerDelayUnit("SECONDS");
        lifeCycleListener.setSchedulerThreadCount("1");

        LogManager.getRootLogger().addAppender(mockAppender);
    }

    @After
    public void teardown() {
        LogManager.getRootLogger().removeAppender(mockAppender);
    }

    @Test
    public void testLifecycleEvent() throws Exception {
        when(mockLifeCycle.getState()).thenReturn(LifecycleState.STOPPING);
        lifeCycleListener.lifecycleEvent(new LifecycleEvent(mockLifeCycle, null, null));
        sleep(2500);

        when(mockLifeCycle.getState()).thenReturn(LifecycleState.STOPPED);
        lifeCycleListener.lifecycleEvent(new LifecycleEvent(mockLifeCycle, null, null));
        sleep(2500);

        verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent) captorLoggingEvent.capture());

        final int eventTotal = captorLoggingEvent.getAllValues().size();
        final LoggingEvent loggingEvent = (LoggingEvent) captorLoggingEvent.getAllValues().get(eventTotal - 1);
        assertEquals("Channel closed", loggingEvent.getMessage());
        System.out.println(captorLoggingEvent.getAllValues().size());

        // make sure that the scheduler has stopped by checking if there are extra events after the listener has processed
        // a STOPPED life cycle
        sleep(2500); // pause to let any rogue scheduler do logging if there are any...
        assertEquals(eventTotal, captorLoggingEvent.getAllValues().size());
    }
}