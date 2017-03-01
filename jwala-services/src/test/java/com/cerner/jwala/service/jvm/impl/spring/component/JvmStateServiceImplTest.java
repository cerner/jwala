package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * {@link JvmStateServiceImpl} tests.
 *
 * Created by Jedd Cuison on 5/12/2016.
 */
public class JvmStateServiceImplTest {
    private JvmStateService jvmStateService;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    @Mock
    private InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> mockInMemoryStateManagerService;

    @Mock
    private JvmStateResolverWorker mockJvmStateResolverWorker;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private GroupStateNotificationService mockGroupStateNotificationService;

    private static final long JVM_STATE_UPDATE_INTERVAL = 60000;

    @Mock
    private RemoteCommandExecutorService mockRemoteCommandExecutorService;

    @Mock
    private SshConfiguration mockSshConfig;

    private static final  long LOCK_TIMEOUT = 600000;

    private static final  int KEY_LOCK_STRIPE_COUNT = 120;

    @Before
    public void setup() {
        initMocks(this);
        jvmStateService = new JvmStateServiceImpl(mockJvmPersistenceService,
                                                  mockInMemoryStateManagerService,
                                                  mockJvmStateResolverWorker,
                                                  mockMessagingService,
                                                  mockGroupStateNotificationService,
                                                  JVM_STATE_UPDATE_INTERVAL,
                                                  mockRemoteCommandExecutorService,
                                                  mockSshConfig,
                                                  LOCK_TIMEOUT,
                                                  KEY_LOCK_STRIPE_COUNT);
    }

    @Test
    public void testVerifyAndUpdateNotInMemOrStaleStates() {
        final List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(new Jvm(new Identifier<Jvm>(1L), "some-jvm", new HashSet<Group>()));
        when(mockJvmPersistenceService.getJvms()).thenReturn(jvmList);
        when(mockJvmStateResolverWorker.pingAndUpdateJvmState(eq(jvmList.get(0)), any(JvmStateService.class))).thenReturn(mock(Future.class));
        jvmStateService.verifyAndUpdateNotInMemOrStaleStates();
        verify(mockJvmStateResolverWorker).pingAndUpdateJvmState(eq(jvmList.get(0)), any(JvmStateService.class));
    }

    @Test
    public void testGetServiceStatus() {
        final Jvm jvm = new Jvm(new Identifier<Jvm>(1L), "some-jvm", new HashSet<Group>());
        jvmStateService.getServiceStatus(jvm);
        verify(mockRemoteCommandExecutorService).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    public void testUpdateStateOnStateStarted() {
        final Identifier<Jvm> id = new Identifier<>(1L);
        final Jvm mockJvm = mock(Jvm.class);
        final CurrentState<Jvm, JvmState> mockCurrentState = mock(CurrentState.class);
        when(mockCurrentState.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockCurrentState.getMessage()).thenReturn("some message...");
        when(mockJvm.getId()).thenReturn(id);
        when(mockInMemoryStateManagerService.get(eq(id))).thenReturn(mockCurrentState);
        when(mockInMemoryStateManagerService.containsKey(eq(id))).thenReturn(true);
        jvmStateService.updateState(mockJvm, JvmState.JVM_STOPPED);
        verify(mockJvmPersistenceService).updateState(eq(id), eq(JvmState.JVM_STOPPED), eq(StringUtils.EMPTY));
        verify(mockMessagingService).send(any(CurrentState.class));
        verify(mockGroupStateNotificationService).retrieveStateAndSend(eq(id), eq(Jvm.class));
    }

    @Test
    public void testUpdateStateOnStateStopped() {
        final Identifier<Jvm> id = new Identifier<>(1L);
        final Jvm mockJvm = mock(Jvm.class);
        final CurrentState<Jvm, JvmState> mockCurrentState = mock(CurrentState.class);
        when(mockCurrentState.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockCurrentState.getMessage()).thenReturn(StringUtils.EMPTY);
        when(mockJvm.getId()).thenReturn(id);
        when(mockInMemoryStateManagerService.get(eq(id))).thenReturn(mockCurrentState);
        when(mockInMemoryStateManagerService.containsKey(eq(id))).thenReturn(true);
        jvmStateService.updateState(mockJvm, JvmState.JVM_STOPPED);
        verify(mockJvmPersistenceService, never()).updateState(eq(id), eq(JvmState.JVM_STOPPED), eq(StringUtils.EMPTY));
    }

}