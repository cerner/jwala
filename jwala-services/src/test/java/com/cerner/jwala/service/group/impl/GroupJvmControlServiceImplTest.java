package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.request.group.ControlGroupJvmRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmControlService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupJvmControlServiceImplTest {

    private GroupService mockGroupService;

    private GroupJvmControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
    private User testUser = new User("user");
    private ControlGroupJvmRequest controlGroupJvmRequest;
    private JvmControlService mockJvmControlService;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockGroupService = mock(GroupService.class);
        mockJvmControlService = mock(JvmControlService.class);

        cut = new GroupJvmControlServiceImpl(mockGroupService, mockJvmControlService);

        mockGroup = mock(Group.class);
        controlGroupJvmRequest = new ControlGroupJvmRequest(groupId, JvmControlOperation.START);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
        when(mockJvmControlService.controlJvm(any(ControlJvmRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
    }

    @Test
    public void testControlGroupWithInvalidGroup() {
        ControlGroupJvmRequest controlGroupJvmRequest = new ControlGroupJvmRequest(groupId, JvmControlOperation.START);
        cut.controlGroup(controlGroupJvmRequest, testUser);
    }

    @Test
    public void testcheckSameState(){
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_STARTED));
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_INITIALIZING));
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_INITIALIZED));
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_START));
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_STARTING));
        assertTrue(cut.checkSameState(JvmControlOperation.STOP, JvmState.JVM_STOPPED));
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_STOPPING));
        assertTrue(cut.checkSameState(JvmControlOperation.STOP, JvmState.JVM_DESTROYED));
        assertTrue(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_DESTROYING));
        assertFalse(cut.checkSameState(JvmControlOperation.STOP, JvmState.JVM_STARTED));
        assertFalse(cut.checkSameState(JvmControlOperation.START, JvmState.JVM_STOPPED));
    }
}

