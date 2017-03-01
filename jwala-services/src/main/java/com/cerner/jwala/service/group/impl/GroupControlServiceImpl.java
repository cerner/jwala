package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.GroupControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.request.group.ControlGroupJvmRequest;
import com.cerner.jwala.common.request.group.ControlGroupRequest;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.service.group.GroupControlService;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import org.springframework.transaction.annotation.Transactional;

public class GroupControlServiceImpl implements GroupControlService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupControlServiceImpl.class);
    private final GroupJvmControlService groupJvmControlService;
    private final GroupWebServerControlService groupWebServerControlService;
    
    public GroupControlServiceImpl(
            final GroupWebServerControlService theGroupWebServerControlService,
            final GroupJvmControlService theGroupJvmControlService) {
        groupWebServerControlService = theGroupWebServerControlService;
        groupJvmControlService = theGroupJvmControlService;
    }

    @Transactional
    @Override
    public void controlGroup(ControlGroupRequest controlGroupRequest, User aUser) {
        LOGGER.info("begin controlGroup operation {} for groupId {}", controlGroupRequest.getControlOperation(),
                controlGroupRequest.getGroupId());
        controlWebServers(controlGroupRequest, aUser);
        controlJvms(controlGroupRequest, aUser);
    }

    private void controlWebServers(ControlGroupRequest controlGroupRequest, User aUser) {
        ControlGroupWebServerRequest controlGroupWebServerCommand = convertToControlGroupWebServerRequest(controlGroupRequest);
        groupWebServerControlService.controlGroup(controlGroupWebServerCommand, aUser);
    }

    private void controlJvms(ControlGroupRequest controlGroupRequest, User aUser) {
        ControlGroupJvmRequest controlGroupJvmCommand = convertToControlGroupJvmRequest(controlGroupRequest);
        groupJvmControlService.controlGroup(controlGroupJvmCommand, aUser);
    }

    @Override
    public void controlGroups(final ControlGroupRequest controlGroupRequest, final User user) {
        LOGGER.debug("Controlling groups. ControlGroupRequest = {}", controlGroupRequest);

        ControlGroupWebServerRequest controlGroupWebServerCommand = convertToControlGroupWebServerRequest(controlGroupRequest);
        groupWebServerControlService.controlAllWebSevers(controlGroupWebServerCommand, user);

        ControlGroupJvmRequest controlGroupJvmCommand = convertToControlGroupJvmRequest(controlGroupRequest);
        groupJvmControlService.controlAllJvms(controlGroupJvmCommand, user);
    }


    private ControlGroupWebServerRequest convertToControlGroupWebServerRequest(ControlGroupRequest controlGroupRequest) {
        WebServerControlOperation webServerControlOperation = WebServerControlOperation.STOP;
        if (controlGroupRequest.getControlOperation().equals(GroupControlOperation.START)){
            webServerControlOperation = WebServerControlOperation.START;
        }

        return new ControlGroupWebServerRequest( controlGroupRequest.getGroupId(), webServerControlOperation);
    }

    private ControlGroupJvmRequest convertToControlGroupJvmRequest(ControlGroupRequest controlGroupRequest) {
        JvmControlOperation jvmControlOperation = JvmControlOperation.STOP;
        if (controlGroupRequest.getControlOperation().equals(GroupControlOperation.START)){
            jvmControlOperation = JvmControlOperation.START;
        }

        return new ControlGroupJvmRequest(controlGroupRequest.getGroupId(),jvmControlOperation);
    }
}
