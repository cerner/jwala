package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import org.apache.catalina.LifecycleState;
import org.jgroups.Message;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link JGroupsServerInfoMessageBuilderTest}
 *
 * Created by Jedd Cuison on 8/16/2016
 */
public class JGroupsServerInfoMessageBuilderTest {

    @Test
    public void testBuildMsg() throws Exception {
        final Message msg = new JGroupsServerInfoMessageBuilder().setServerId("1")
                                                                 .setState(LifecycleState.STOPPED)
                                                                 .setSrcAddress(new IpAddress("localhost"))
                                                                 .setDestAddress(new IpAddress("localhost"))
                                                                 .build();
        assertNotNull(msg);
    }
}
