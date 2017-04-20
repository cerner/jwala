package com.cerner.jwala.service.group.impl.spring.component;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.OperationalState;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.service.MessagingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link GroupStateNotificationServiceImpl}.
 *
 * Created by Jedd Cuison on 4/18/2016.
 */
public class GroupStateNotificationServiceImplTest {

    private GroupStateNotificationServiceImpl groupStateNotificationServiceImpl;

    @Mock
    private GroupCrudService mockGroupCrudService;

    @Mock
    private JvmCrudService mockJvmCrudService;

    @Mock
    private WebServerCrudService mockWebServerCrudService;

    private static String [] groupStateArray = new String[2];
    private static int groupStateArrayCount = 0;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        groupStateNotificationServiceImpl = new GroupStateNotificationServiceImpl(mockGroupCrudService, mockJvmCrudService,
                mockWebServerCrudService, new TesterMessagingService());
    }

    @Test
    public void testRetrieveStateAndSendToATopic() {
        final JpaJvm mockJpaJvm = mock(JpaJvm.class);
        final List<JpaGroup> jpaGroupList = new ArrayList<>();
        jpaGroupList.add(mock(JpaGroup.class));
        jpaGroupList.add(mock(JpaGroup.class));
        when(jpaGroupList.get(0).getId()).thenReturn(1L);
        when(jpaGroupList.get(1).getId()).thenReturn(2L);
        final Identifier<Jvm> id = new Identifier<>(1L);
        when(mockJvmCrudService.getJvm(eq(id))).thenReturn(mockJpaJvm);
        when(mockJpaJvm.getGroups()).thenReturn(jpaGroupList);
        when(mockJvmCrudService.getJvmCount(anyString())).thenReturn(6L);
        when(mockJvmCrudService.getJvmStartedCount(anyString())).thenReturn(3L);
        when(mockJvmCrudService.getJvmStoppedCount(anyString())).thenReturn(2L);
        when(mockJvmCrudService.getJvmForciblyStoppedCount(anyString())).thenReturn(1L);
        when(mockWebServerCrudService.getWebServerCount(anyString())).thenReturn(16L);
        when(mockWebServerCrudService.getWebServerStartedCount(anyString())).thenReturn(2L);
        when(mockWebServerCrudService.getWebServerStoppedCount(anyString())).thenReturn(8L);
        when(mockWebServerCrudService.getWebServerForciblyStoppedCount(anyString())).thenReturn(6L);
        groupStateNotificationServiceImpl.retrieveStateAndSend(id, Jvm.class);
        System.out.println(groupStateArray[0]);
        System.out.println(groupStateArray[1]);
        assertTrue(groupStateArray[0].indexOf("type=GROUP,message=,webServerCount=16,webServerStartedCount=2," +
                "webServerStoppedCount=8,webServerForciblyStoppedCount=6,jvmCount=6,jvmStartedCount=3,jvmStoppedCount=2," +
                "jvmForciblyStoppedCount=1]") > -1);
        assertTrue(groupStateArray[1].indexOf("type=GROUP,message=,webServerCount=16,webServerStartedCount=2," +
                "webServerStoppedCount=8,webServerForciblyStoppedCount=6,jvmCount=6,jvmStartedCount=3,jvmStoppedCount=2," +
                "jvmForciblyStoppedCount=1]") > -1);
    }

    private static class TesterMessagingService implements MessagingService {

        @Override
        public void send(Object payLoad) {
            final CurrentState<Group, OperationalState> groupState = (CurrentState<Group, OperationalState>) payLoad;
            groupStateArray[groupStateArrayCount++] = groupState.getId() + ", " + groupState.toString();
        }
    }

}
