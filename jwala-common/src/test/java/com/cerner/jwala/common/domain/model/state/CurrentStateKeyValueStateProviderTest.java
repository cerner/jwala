package com.cerner.jwala.common.domain.model.state;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static groovy.util.GroovyTestCase.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class CurrentStateKeyValueStateProviderTest {

    @Mock
    private KeyValueStateConsumer consumer;

    @Test
    public void testProvideStateWithoutMessage() throws Exception {
        final Identifier<WebServer> id = new Identifier<>(123456L);
        final WebServerReachableState state = WebServerReachableState.WS_REACHABLE;
        final DateTime asOf = DateTime.now();
        final StateType type = StateType.WEB_SERVER;

        final CurrentState<WebServer, WebServerReachableState> producer = new CurrentState<>(id,
                                                                                             state,
                                                                                             asOf,
                                                                                             type);
        assertEquals(id, producer.getId());
    }

    @Test
    public void testProvideStateWithMessage() throws Exception {
        final Identifier<WebServer> id = new Identifier<>(123456L);
        final WebServerReachableState state = WebServerReachableState.WS_REACHABLE;
        final DateTime asOf = DateTime.now();
        final StateType type = StateType.WEB_SERVER;
        final String message = "This is the state message";

        final CurrentState<WebServer, WebServerReachableState> producer = new CurrentState<>(id,
                                                                                             state,
                                                                                             asOf,
                                                                                             type,
                                                                                             message);
        assertEquals(message, producer.getMessage());
    }

}
