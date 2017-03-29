package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class GroupWebServerControlServiceImplTest {
    private GroupService mockGroupService;
    private WebServerControlService mockWebServerControlService;

    private GroupWebServerControlServiceImpl cut;
    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private Group mockGroup;
    private User testUser = new User("user");
    private ControlGroupWebServerRequest controlGroupWebServerRequest;
    private Set mockWebServersSet;
    private WebServer mockWebServer;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        mockGroupService = mock(GroupService.class);
        mockWebServerControlService = mock(WebServerControlService.class);

        cut = new GroupWebServerControlServiceImpl(mockGroupService, mockWebServerControlService);

        mockGroup = mock(Group.class);
        mockWebServer = mock(WebServer.class);
        controlGroupWebServerRequest = new ControlGroupWebServerRequest(groupId, WebServerControlOperation.START);

        mockWebServersSet = new HashSet();
        mockWebServersSet.add(mockWebServer);

        when(mockGroupService.getGroup(groupId)).thenReturn(mockGroup);
        when(mockGroupService.getGroupWithWebServers(groupId)).thenReturn(mockGroup);
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(111L));

        when(mockGroup.getWebServers()).thenReturn(mockWebServersSet);
        when(mockWebServerControlService.controlWebServer(any(ControlWebServerRequest.class), any(User.class))).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
    }

    @Test(expected = BadRequestException.class)
    public void testControlGroupWithInvalidGroup() {
        ControlGroupWebServerRequest controlGroupWebServerRequest = new ControlGroupWebServerRequest(null, WebServerControlOperation.START);
        cut.controlGroup(controlGroupWebServerRequest, testUser);
    }

    @Test
    public void testControlGroup() {
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        cut.controlGroup(controlGroupWebServerRequest, testUser);
    }

}
