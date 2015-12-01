package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import com.siemens.cto.aem.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.request.webserver.*;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.request.group.*;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.request.webserver.UploadHttpdConfTemplateRequest;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupJvmControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.GroupWebServerControlService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.GroupIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupChildType;
import com.siemens.cto.aem.ws.rest.v1.service.group.GroupServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.group.MembershipDetails;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GroupServiceRestImpl implements GroupServiceRest {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceRestImpl.class);

    private final GroupService groupService;
    private final ResourceService resourceService;

    @Autowired
    private GroupControlService groupControlService;

    @Autowired
    private GroupJvmControlService groupJvmControlService;

    @Autowired
    private GroupWebServerControlService groupWebServerControlService;

    @Autowired
    @Qualifier("groupStateService")
    private StateService<Group, GroupState> groupStateService;

    public GroupServiceRestImpl(final GroupService theGroupService, ResourceService theResourceService) {
        groupService = theGroupService;
        resourceService = theResourceService;
    }


    public Response getGroupHistory(String groupName) {
        logger.debug("Get Groups History requested for group : {}", groupName);
        Group group = groupService.getGroup(groupName);

        // SMG: 11/15/15
        // TODO: Is this needed or is the proper response sent back to the client using the error from the persistence layer
        if (group == null) {
            logger.warn("Group with name {} not found.", groupName);
            return ResponseBuilder.notOk(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(
                    AemFaultType.GROUP_NOT_FOUND, "Group " + groupName + " not found."));
        }

        return ResponseBuilder.ok(group.getHistory());
    }

    @Override
    public Response getGroups(final NameSearchParameterProvider aGroupNameSearch, final boolean fetchWebServers) {
        logger.debug("Get Groups requested with search: {}", aGroupNameSearch.getName());

        final List<Group> groups;
        if (aGroupNameSearch.isNamePresent()) {
            groups = groupService.findGroups(aGroupNameSearch.getName());
        } else {
            groups = groupService.getGroups(fetchWebServers);
        }

        return ResponseBuilder.ok(groups);
    }

    @Override
    public Response getGroup(final String groupIdOrName, final boolean byName) {
        if (byName) {
            return  ResponseBuilder.ok(groupService.getGroup(groupIdOrName));
        }
        final Identifier<Group> groupId = new Identifier<Group>(groupIdOrName);
        logger.debug("Get Group requested: {}", groupId);
        return ResponseBuilder.ok(groupService.getGroup(groupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName,
                                final AuthenticatedUser aUser) {
        logger.debug("Create Group requested: {}", aNewGroupName);
        return ResponseBuilder.created(groupService.createGroup(new CreateGroupRequest(aNewGroupName),
                aUser.getUser()));
    }

    @Override

    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                                final AuthenticatedUser aUser) {
        logger.debug("Update Group requested: {}", anUpdatedGroup);

        // TODO: Refactor adhoc conversion to process group name instead of Id.
        final Group group = groupService.getGroup(anUpdatedGroup.getId());
        final JsonUpdateGroup updatedGroup = new JsonUpdateGroup(group.getId().getId().toString(),
                                                                 anUpdatedGroup.getName());

        return ResponseBuilder.ok(groupService.updateGroup(updatedGroup.toUpdateGroupCommand(),
                aUser.getUser()));
    }

    @Override
    public Response removeGroup(final String name, final boolean byName) {
        logger.debug("Delete Group requested: {} byName={}", name, byName);
        if (byName) {
            groupService.removeGroup(name);
        } else {
            groupService.removeGroup(new Identifier<Group>(name));
        }
        return ResponseBuilder.ok();
    }

    @Override
    public Response removeJvmFromGroup(final Identifier<Group> aGroupId,
                                       final Identifier<Jvm> aJvmId,
                                       final AuthenticatedUser aUser) {
        logger.debug("Remove JVM from Group requested: {}, {}", aGroupId, aJvmId);
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId,
                        aJvmId),
                aUser.getUser()));
    }

    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd,
                                   final AuthenticatedUser aUser) {
        logger.debug("Add JVM to Group requested: {}, {}", aGroupId, someJvmsToAdd);
        final AddJvmsToGroupRequest command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                aUser.getUser()));
    }

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                     final JsonControlJvm jsonControlJvm,
                                     final AuthenticatedUser aUser) {
        logger.debug("Control all JVMs in Group requested: {}, {}", aGroupId, jsonControlJvm);
        final JvmControlOperation command = jsonControlJvm.toControlOperation();
        final ControlGroupJvmRequest grpCommand = new ControlGroupJvmRequest(aGroupId,
                JvmControlOperation.convertFrom(command.getExternalValue()));
        groupJvmControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response populateJvmConfig(final Identifier<Group> aGroupId, final AuthenticatedUser aUser, final boolean overwriteExisting) {
        List<UploadJvmTemplateRequest> uploadJvmTemplateCommands = new ArrayList<>();
        for (Jvm jvm : groupService.getGroup(aGroupId).getJvms()) {
            for (final ResourceType resourceType : resourceService.getResourceTypes()) {
                if ("jvm".equals(resourceType.getEntityType()) && !"invoke.bat".equals(resourceType.getConfigFileName())) {
                    FileInputStream dataInputStream;
                    try {
                        dataInputStream = new FileInputStream(new File(ApplicationProperties.get("paths.resource-types") + "/" + resourceType.getTemplateName()));
                        UploadJvmTemplateRequest uploadJvmTemplateCommand = new UploadJvmTemplateRequest(jvm, resourceType.getTemplateName(), dataInputStream) {
                            @Override
                            public String getConfFileName() {
                                return resourceType.getConfigFileName();
                            }
                        };
                        uploadJvmTemplateCommands.add(uploadJvmTemplateCommand);
                    } catch (FileNotFoundException e) {
                        throw new InternalErrorException(AemFaultType.INVALID_PATH, "Could not find resource template", e);
                    }
                }
            }
        }
        return ResponseBuilder.ok(groupService.populateJvmConfig(aGroupId, uploadJvmTemplateCommands, aUser.getUser(), overwriteExisting));
    }

    @Override
    public Response populateWebServerConfig(final Identifier<Group> aGroupId, final AuthenticatedUser aUser, final boolean overwriteExisting) {
        List<UploadWebServerTemplateRequest> uploadWSTemplateCommands = new ArrayList<>();
        UploadWebServerTemplateCommandBuilder uploadCommandBuilder = new UploadWebServerTemplateCommandBuilder();
        for (WebServer webServer : groupService.getGroupWithWebServers(aGroupId).getWebServers()) {
            UploadHttpdConfTemplateRequest httpdConfTemplateCommand = uploadCommandBuilder.buildHttpdConfCommand(webServer);
            uploadWSTemplateCommands.add(httpdConfTemplateCommand);
        }
        return ResponseBuilder.ok(groupService.populateWebServerConfig(aGroupId, uploadWSTemplateCommands, aUser.getUser(), overwriteExisting));
    }

    @Override
    public Response controlGroupWebservers(final Identifier<Group> aGroupId,
                                           final JsonControlWebServer jsonControlWebServer,
                                           final AuthenticatedUser aUser) {
        logger.debug("Control all WebServers in Group requested: {}, {}", aGroupId, jsonControlWebServer);
        final WebServerControlOperation command = jsonControlWebServer.toControlOperation();
        final ControlGroupWebServerRequest grpCommand = new ControlGroupWebServerRequest(aGroupId,
                WebServerControlOperation.convertFrom(command.getExternalValue()));
        groupWebServerControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlGroup(final Identifier<Group> aGroupId,
                                 final JsonControlGroup jsonControlGroup,
                                 final AuthenticatedUser aUser) {

        GroupControlOperation groupControlOperation = jsonControlGroup.toControlOperation();
        logger.debug("starting control group {} with operation {}", aGroupId, groupControlOperation);

        ControlGroupRequest grpCommand = new ControlGroupRequest(aGroupId, groupControlOperation);
        groupControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response resetState(final Identifier<Group> aGroupId,
                               final AuthenticatedUser aUser) {
        return ResponseBuilder.ok(groupControlService.resetState(aGroupId,
                aUser.getUser()));
    }

    @Override
    public Response getCurrentJvmStates(final GroupIdsParameterProvider aGroupIdsParameterProvider) {
        logger.debug("Current Group states requested : {}", aGroupIdsParameterProvider);
        final Set<Identifier<Group>> groupIds = aGroupIdsParameterProvider.valueOf();
        final Set<CurrentState<Group, GroupState>> currentGroupStates;

        if (groupIds.isEmpty()) {
            currentGroupStates = groupStateService.getCurrentStates();
        } else {
            currentGroupStates = groupStateService.getCurrentStates(groupIds);
        }

        return ResponseBuilder.ok(currentGroupStates);
    }

    private List<MembershipDetails> createMembershipDetailsFromJvms(final List<Jvm> jvms) {
        final List<MembershipDetails> membershipDetailsList = new LinkedList<>();
        for (Jvm jvm : jvms) {
            final List<String> groupNames = new LinkedList<>();
            for (LiteGroup group : jvm.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(jvm.getJvmName(),
                    GroupChildType.JVM,
                    groupNames));
        }
        return membershipDetailsList;
    }

    private List<MembershipDetails> createMembershipDetailsFromWebServers(final List<WebServer> webServers) {
        final List<MembershipDetails> membershipDetailsList = new LinkedList<>();
        for (WebServer webServer : webServers) {
            final List<String> groupNames = new LinkedList<>();
            for (Group group : webServer.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(webServer.getName(),
                    GroupChildType.WEB_SERVER,
                    groupNames));
        }
        return membershipDetailsList;
    }

    @Override
    public Response getOtherGroupMembershipDetailsOfTheChildren(final Identifier<Group> id,
                                                                final GroupChildType groupChildType) {
        final List<Jvm> jvmGroupingDetails;
        final List<WebServer> webServerGroupingDetails;

        if (groupChildType != null) {
            if (groupChildType == GroupChildType.JVM) {
                jvmGroupingDetails = groupService.getOtherGroupingDetailsOfJvms(id);
                return ResponseBuilder.ok(createMembershipDetailsFromJvms(jvmGroupingDetails));
            } else if (groupChildType == GroupChildType.WEB_SERVER) {
                webServerGroupingDetails = groupService.getOtherGroupingDetailsOfWebServers(id);
                return ResponseBuilder.ok(createMembershipDetailsFromWebServers(webServerGroupingDetails));
            }
        }

        jvmGroupingDetails = groupService.getOtherGroupingDetailsOfJvms(id);
        final List<MembershipDetails> membershipDetailsList = createMembershipDetailsFromJvms(jvmGroupingDetails);
        webServerGroupingDetails = groupService.getOtherGroupingDetailsOfWebServers(id);
        membershipDetailsList.addAll(createMembershipDetailsFromWebServers(webServerGroupingDetails));

        return ResponseBuilder.ok(membershipDetailsList);
    }

}
