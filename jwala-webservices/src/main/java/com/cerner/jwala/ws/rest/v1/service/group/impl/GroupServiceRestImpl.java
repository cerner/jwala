package com.cerner.jwala.ws.rest.v1.service.group.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupControlOperation;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.exception.GroupServiceException;
import com.cerner.jwala.service.group.*;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.provider.NameSearchParameterProvider;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import com.cerner.jwala.ws.rest.v1.service.group.GroupChildType;
import com.cerner.jwala.ws.rest.v1.service.group.GroupServiceRest;
import com.cerner.jwala.ws.rest.v1.service.group.MembershipDetails;
import com.cerner.jwala.ws.rest.v1.service.jvm.JvmServiceRest;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

public class GroupServiceRestImpl implements GroupServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceRestImpl.class);

    @Autowired
    private JvmServiceRest jvmServiceRest;

    @Autowired
    private GroupStateNotificationService groupStateNotificationService;

    private final GroupService groupService;
    private final ResourceService resourceService;
    private final ExecutorService executorService;
    private final ApplicationService applicationService;
    private final GroupControlService groupControlService;
    private final GroupJvmControlService groupJvmControlService;
    private final GroupWebServerControlService groupWebServerControlService;
    private final JvmService jvmService;
    private final WebServerService webServerService;
    private final ApplicationServiceRest applicationServiceRest;
    private final WebServerServiceRest webServerServiceRest;

    @Autowired
    public GroupServiceRestImpl(final GroupService groupService, final ResourceService resourceService,
                                final GroupControlService groupControlService, final GroupJvmControlService groupJvmControlService,
                                final GroupWebServerControlService groupWebServerControlService, final JvmService jvmService,
                                final WebServerService webServerService, ApplicationService applicationService,
                                final ApplicationServiceRest applicationServiceRest, final WebServerServiceRest webServerServiceRest) {
        this.groupService = groupService;
        this.resourceService = resourceService;
        this.groupControlService = groupControlService;
        this.groupJvmControlService = groupJvmControlService;
        this.groupWebServerControlService = groupWebServerControlService;
        this.jvmService = jvmService;
        this.webServerService = webServerService;
        this.applicationService = applicationService;
        this.applicationServiceRest = applicationServiceRest;
        this.webServerServiceRest = webServerServiceRest;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("resources.thread-task-executor.pool.size", "25")));
    }

    @Override
    public Response getGroups(final NameSearchParameterProvider aGroupNameSearch, final boolean fetchWebServers) {
        LOGGER.debug("Get Groups requested with search: {}", aGroupNameSearch.getName());

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
        LOGGER.debug("Get Group requested: {} byName={}", groupIdOrName, byName);
        if (byName) {
            return ResponseBuilder.ok(groupService.getGroup(groupIdOrName));
        }
        final Identifier<Group> groupId = new Identifier<>(groupIdOrName);
        return ResponseBuilder.ok(groupService.getGroup(groupId));
    }

    @Override
    public Response createGroup(final String aNewGroupName,
                                final AuthenticatedUser aUser) {
        try {
            LOGGER.info("Create Group requested: {} by user {}", aNewGroupName, aUser.getUser().getId());
            final Group group = groupService.createGroup(new CreateGroupRequest(aNewGroupName), aUser.getUser());
            return ResponseBuilder.created(group);
        } catch (EntityExistsException eee) {
            LOGGER.error("Group Name already exists: {}", aNewGroupName);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_GROUP_NAME, eee.getMessage(), eee));
        }
    }


    @Override
    public Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                                final AuthenticatedUser aUser) {
        LOGGER.info("Update Group requested: {} by user {}", anUpdatedGroup, aUser.getUser().getId());
        try {
            // TODO: Refactor adhoc conversion to process group name instead of Id.
            final Group group = groupService.getGroup(anUpdatedGroup.getId());
            final JsonUpdateGroup updatedGroup = new JsonUpdateGroup(group.getId().getId().toString(),
                    anUpdatedGroup.getName());

            return ResponseBuilder.ok(groupService.updateGroup(updatedGroup.toUpdateGroupCommand(),
                    aUser.getUser()));
        } catch (EntityExistsException eee) {
            LOGGER.error("Group Name already exists: {}", anUpdatedGroup.getName(), eee);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_GROUP_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response removeGroup(final String name, final boolean byName) {
        LOGGER.info("Delete Group requested: {} byName={}", name, byName);
        try {
            if (byName) {
                groupService.removeGroup(name);
            } else {
                groupService.removeGroup(new Identifier<>(name));
            }
            return ResponseBuilder.ok();
        } catch (GroupServiceException e) {
            LOGGER.error("Remove group error: {}", name, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.FAILED_TO_DELETE_GROUP, e.getMessage(), e));
        }
    }

    @Override
    public Response removeJvmFromGroup(final Identifier<Group> aGroupId,
                                       final Identifier<Jvm> aJvmId,
                                       final AuthenticatedUser aUser) {
        LOGGER.info("Remove JVM from Group requested: {}, {} by user {}", aGroupId, aJvmId, aUser.getUser().getId());
        return ResponseBuilder.ok(groupService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId,
                        aJvmId),
                aUser.getUser()));
    }

    @Override
    public Response addJvmsToGroup(final Identifier<Group> aGroupId,
                                   final JsonJvms someJvmsToAdd,
                                   final AuthenticatedUser aUser) {
        LOGGER.info("Add JVM to Group requested: {}, {} by user {}", aGroupId, someJvmsToAdd, aUser.getUser().getId());
        final AddJvmsToGroupRequest command = someJvmsToAdd.toCommand(aGroupId);
        return ResponseBuilder.ok(groupService.addJvmsToGroup(command,
                aUser.getUser()));
    }

    @Override
    public Response controlGroupJvms(final Identifier<Group> aGroupId,
                                     final JsonControlJvm jsonControlJvm,
                                     final AuthenticatedUser aUser) {
        LOGGER.info("Control all JVMs in Group requested: {}, {} by user {}", aGroupId, jsonControlJvm, aUser.getUser().getId());
        final JvmControlOperation command = jsonControlJvm.toControlOperation();
        final ControlGroupJvmRequest grpCommand = new ControlGroupJvmRequest(aGroupId,
                JvmControlOperation.convertFrom(command.getExternalValue()));
        groupJvmControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }


    @Override
    public Response updateGroupWebServerResourceTemplate(final String groupName, final String resourceTemplateName, final String content) {
        LOGGER.info("update group web server resource template {} for group {}", resourceTemplateName, groupName);
        try {
            final String updatedContent = groupService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content);
            Group group = groupService.getGroup(groupName);
            group = groupService.getGroupWithWebServers(group.getId());

            Set<WebServer> groupWebServers = group.getWebServers();
            if (null != groupWebServers) {
                final Map<String, Future<Response>> futureContents = new HashMap<>(groupWebServers.size());
                LOGGER.info("Updating the templates for all the Web Servers in group {}", groupName);
                for (final WebServer webServer : groupWebServers) {
                    final String webServerName = webServer.getName();
                    LOGGER.info("Updating Web Server {} template {}", webServerName, resourceTemplateName);
                    Future<Response> futureContent = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            return ResponseBuilder.ok(webServerService.updateResourceTemplate(webServerName, resourceTemplateName, updatedContent));
                        }
                    });
                    futureContents.put(webServerName, futureContent);
                }
                checkResponsesForErrorStatus(futureContents);
            } else {
                LOGGER.info("No Web Servers to update in group {}", groupName);
            }

            LOGGER.info("Update SUCCESSFUL");
            return ResponseBuilder.ok(updatedContent);

        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response previewGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String template) {
        LOGGER.debug("Preview group web server template for group {}", groupName);
        LOGGER.debug(template);
        try {
            return ResponseBuilder.ok(groupService.previewGroupWebServerResourceTemplate(resourceTemplateName, groupName, template, resourceService.generateResourceGroup()));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to preview the web server template for {}", groupName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_TEMPLATE, e.getMessage(), e));
        }
    }

    @Override
    public Response getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced) {
        LOGGER.debug("Get group web server resource template {} for group {} : tokens replaced={}", resourceTemplateName, groupName, tokensReplaced);
        return ResponseBuilder.ok(groupService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName, tokensReplaced, tokensReplaced ? resourceService.generateResourceGroup() : new ResourceGroup()));
    }

    @Override
    public Response generateAndDeployGroupJvmFile(final String groupName, final String fileName,
                                                  final AuthenticatedUser authUser) {
        return ResponseBuilder.ok(groupService.generateAndDeployGroupJvmFile(groupName, fileName, authUser.getUser()));
    }

    @Override
    public Response getGroupJvmResourceTemplate(final String groupName,
                                                final String resourceTemplateName,
                                                final boolean tokensReplaced) {
        LOGGER.debug("Get group JVM resource template {} for group {} : tokens replaced={}", resourceTemplateName, groupName, tokensReplaced);
        return ResponseBuilder.ok(groupService.getGroupJvmResourceTemplate(groupName, resourceTemplateName, resourceService.generateResourceGroup(), tokensReplaced));
    }

    @Override
    public Response updateGroupJvmResourceTemplate(final String groupName, final String resourceTemplateName, final String content) {
        LOGGER.info("Updating the group template {} for {}", resourceTemplateName, groupName);
        LOGGER.debug(content);

        try {

            final String updatedContent = groupService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content);
            final Group group = groupService.getGroup(groupName);

            Set<Jvm> groupJvms = group.getJvms();
            Map<String, Future<Response>> futureContents = new HashMap<>();
            if (null != groupJvms) {
                LOGGER.info("Updating the templates for all the JVMs in group {}", groupName);
                final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                for (final Jvm jvm : groupJvms) {
                    final String jvmName = jvm.getJvmName();
                    LOGGER.info("Updating JVM {} template {}", jvmName, resourceTemplateName);
                    Future<Response> futureContent = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            return ResponseBuilder.ok(jvmService.updateResourceTemplate(jvmName, resourceTemplateName, updatedContent));
                        }
                    });
                    futureContents.put(jvmName, futureContent);
                }
                checkResponsesForErrorStatus(futureContents);
            } else {
                LOGGER.info("No JVMs to update in group {}", groupName);
            }

            LOGGER.info("Update SUCCESSFUL");
            return ResponseBuilder.ok(updatedContent);

        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response previewGroupJvmResourceTemplate(String groupName, String fileName, String template) {
        LOGGER.debug("Preview group JVM resource template for group {}", groupName);
        LOGGER.debug(template);
        try {
            final Group group = groupService.getGroup(groupName);
            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            final Set<Jvm> jvms = group.getJvms();
            return ResponseBuilder.ok(resourceService.generateResourceFile(fileName, template, resourceGroup, (null != jvms && !jvms.isEmpty() ? jvms.iterator().next() : null), ResourceGeneratorType.PREVIEW));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to preview the JVM template for {}", groupName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_TEMPLATE, e.getMessage(), e));
        }
    }

    @Override
    public Response generateAndDeployGroupWebServersFile(final String groupName, final String resourceFileName, final AuthenticatedUser aUser) {
        LOGGER.info("generate and deploy the web server file {} to group {} by user", resourceFileName, groupName, aUser.getUser().getId());
        final Group group = groupService.getGroup(groupName);
        final Group groupWithWebServers = groupService.getGroupWithWebServers(group.getId());
        final String resourceTemplateContent = groupService.getGroupWebServerResourceTemplate(groupName, resourceFileName, false, resourceService.generateResourceGroup());
        final String resourceMetaData = groupService.getGroupWebServerResourceTemplateMetaData(groupName, resourceFileName);
        final Set<WebServer> webServers = groupWithWebServers.getWebServers();
        if (null != webServers && !webServers.isEmpty()) {

            checkWebServerStateBeforeDeploy(resourceFileName, groupWithWebServers, webServers);

            final Map<String, Future<Response>> futureMap = executeWebServerResourceDeploy(groupName, resourceFileName, aUser, resourceTemplateContent, resourceMetaData, webServers);

            checkResponsesForErrorStatus(futureMap);
        } else {
            LOGGER.info("No web servers in group {}", groupName);
        }
        return ResponseBuilder.ok(resourceTemplateContent);
    }

    private Map<String, Future<Response>> executeWebServerResourceDeploy(final String groupName, final String resourceFileName, final AuthenticatedUser aUser, final String httpdTemplateContent, final String resourceMetaData, Set<WebServer> webServers) {
        final Map<String, Future<Response>> futureMap = new HashMap<>();
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (final WebServer webserver : webServers) {
            final String name = webserver.getName();
            Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    webServerServiceRest.updateResourceTemplate(name, resourceFileName, httpdTemplateContent);
                    ResourceIdentifier resourceId = new ResourceIdentifier.Builder()
                            .setGroupName(groupName)
                            .setWebServerName(name)
                            .setResourceName(resourceFileName)
                            .build();
                    resourceService.updateResourceMetaData(resourceId, resourceFileName, resourceMetaData);
                    return webServerServiceRest.generateAndDeployConfig(name, resourceFileName, aUser);

                }
            });
            futureMap.put(name, responseFuture);
        }
        return futureMap;
    }

    private void checkWebServerStateBeforeDeploy(String resourceFileName, Group groupWithWebServers, Set<WebServer> webServers) {
        List<String> startedWebServers = new ArrayList<>();
        for (WebServer webServer : webServers) {
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setWebServerName(webServer.getName())
                    .setResourceName(resourceFileName)
                    .setGroupName(groupWithWebServers.getName())
                    .build();
            String metaDataStr = resourceService.getResourceContent(resourceIdentifier).getMetaData();
            try {
                ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(resourceFileName, webServer, metaDataStr);
                if (webServerService.isStarted(webServer)) {
                    if (metaData.isHotDeploy()) {
                        LOGGER.info("Web Server {} is started, but resource {} is configured for hot deploy. Continuing with deploy ...", webServer.getName(), resourceFileName);
                    } else {
                        startedWebServers.add(webServer.getName());
                    }
                }
            } catch (IOException e) {
                String errorMsg = MessageFormat.format("Failed to tokenize resource {0} meta data for Web Server {1} during deployment of Web Server resource", resourceFileName, webServer.getName());
                LOGGER.error(errorMsg, e);
                throw new InternalErrorException(FaultType.BAD_STREAM, errorMsg);
            }
        }

        if (!startedWebServers.isEmpty()) {
            String deployMsg = MessageFormat.format("Failed to deploy {0} for group {1}: the following Web Servers were started and the resource was not configured for hotDeploy=true: {2}",
                    resourceFileName, groupWithWebServers.getName(), startedWebServers);
            LOGGER.info(deployMsg);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, deployMsg);
        }

    }

    private void checkResponsesForErrorStatus(Map<String, Future<Response>> futureMap) {
        Map<String, List<String>> entityDetailsMap = new HashMap<>();
        for (String keyEntityName : futureMap.keySet()) {
            Response response;
            try {
                long timeout = Long.parseLong(ApplicationProperties.get("remote.jwala.execution.timeout.seconds", "600"));
                Future<Response> responseFuture = futureMap.get(keyEntityName);
                if (responseFuture != null) {
                    response = responseFuture.get(timeout, TimeUnit.SECONDS);
                    if (response.getStatus() > 399) {
                        final String reasonPhrase = response.getStatusInfo().getReasonPhrase();
                        LOGGER.error(MessageFormat.format("Remote command failed for {0}: {1}", keyEntityName, reasonPhrase));
                        entityDetailsMap.put(keyEntityName, Collections.singletonList(reasonPhrase));
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("FAILURE getting response for {}", keyEntityName, e);
                final Throwable cause = e.getCause();
                if (cause instanceof InternalErrorException) {
                    if (((InternalErrorException) cause).getErrorDetails().isEmpty()) {
                        entityDetailsMap.put(keyEntityName, Collections.singletonList(cause.getMessage()));
                    } else {
                        entityDetailsMap.putAll(((InternalErrorException) cause).getErrorDetails());
                    }
                } else {
                    entityDetailsMap.put(keyEntityName, Collections.singletonList(e.getMessage()));
                }
            } catch (TimeoutException e) {
                LOGGER.error("Timed out getting response.", e);
                entityDetailsMap.put(keyEntityName, Collections.singletonList(e.getMessage()));
            }
        }

        if (!entityDetailsMap.isEmpty()) {
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Request failed for the following errors:", null, entityDetailsMap);
        } else {
            LOGGER.info("Finished checking requests for error statuses.");
        }
    }

    @Override
    public Response controlGroupWebservers(final Identifier<Group> aGroupId,
                                           final JsonControlWebServer jsonControlWebServer,
                                           final AuthenticatedUser aUser) {
        LOGGER.debug("Control all WebServers in Group requested: {}, {} by user {}", aGroupId, jsonControlWebServer, aUser.getUser().getId());
        final WebServerControlOperation command = jsonControlWebServer.toControlOperation();
        final ControlGroupWebServerRequest grpCommand = new ControlGroupWebServerRequest(aGroupId,
                WebServerControlOperation.convertFrom(command.getExternalValue()));
        groupWebServerControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response generateGroupWebservers(Identifier<Group> aGroupId, final AuthenticatedUser aUser) {
        LOGGER.info("Starting group generation of web servers for group ID {} by user {}", aGroupId, aUser.getUser().getId());
        Group group = groupService.getGroupWithWebServers(aGroupId);
        Set<WebServer> webServers = group.getWebServers();
        if (null != webServers && !webServers.isEmpty()) {
            Set<String> startedWebServers = new HashSet<>();
            for (WebServer webServer : webServers) {
                if (webServerService.isStarted(webServer)) {
                    LOGGER.warn("Failed to start generation of web servers for group ID {}: not all web servers were stopped - {} was started", aGroupId, webServer.getName());
                    startedWebServers.add(webServer.getName());
                }
            }

            if (!startedWebServers.isEmpty()) {
                LOGGER.error("Failed to start generation of web servers for group ID {}: not all web servers were stopped - {} were started", aGroupId, startedWebServers.toString());
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                        "All web servers in the group must be stopped before continuing. Operation stopped for web server " + startedWebServers.toString());
            }

            // generate and deploy the web servers
            Map<String, Future<Response>> futuresMap = new HashMap<>();
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            for (final WebServer webServer : webServers) {
                final String webServerName = webServer.getName();
                Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        return webServerServiceRest.generateAndDeployWebServer(webServerName, aUser);
                    }
                });
                futuresMap.put(webServerName, responseFuture);
            }

            checkResponsesForErrorStatus(futuresMap);
        } else {
            LOGGER.info("No web servers in group {}", aGroupId);
        }
        return ResponseBuilder.ok(group);
    }

    @Override
    public Response generateGroupJvms(final Identifier<Group> aGroupId, final AuthenticatedUser aUser) {
        LOGGER.info("Starting group generation of JVMs for group ID {} by user {}", aGroupId, aUser.getUser().getId());

        final Group group = groupService.getGroup(aGroupId);
        Set<Jvm> jvms = group.getJvms();
        if (null != jvms && !jvms.isEmpty()) {
            Set<String> starteJvms = new HashSet<>();
            for (Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    LOGGER.warn("Failed to start generation of JVMs for group ID {}: not all JVMs were stopped - {} was started", aGroupId, jvm.getJvmName());
                    starteJvms.add(jvm.getJvmName());
                }
            }

            if (!starteJvms.isEmpty()) {
                LOGGER.error("Failed to start generation of JVMs for group ID {}: not all JVMs were stopped - {} were started", aGroupId, starteJvms.toString());
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                        "All JVMs in the group must be stopped before continuing. Operation stopped for JVMs " + starteJvms.toString());
            }

            // generate and deploy the JVMs
            Map<String, Future<Response>> futuresMap = new HashMap<>();
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            for (final Jvm jvm : jvms) {
                final String jvmName = jvm.getJvmName();
                Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                    @Override
                    public Response call() throws Exception {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        return jvmServiceRest.generateAndDeployJvm(jvmName, aUser);
                    }
                });
                futuresMap.put(jvmName, responseFuture);
            }
            checkResponsesForErrorStatus(futuresMap);
        } else {
            LOGGER.info("No JVMs in group {}", aGroupId);
        }

        return ResponseBuilder.ok(group);
    }

    @Override
    public Response controlGroup(final Identifier<Group> aGroupId,
                                 final JsonControlGroup jsonControlGroup,
                                 final AuthenticatedUser aUser) {

        GroupControlOperation groupControlOperation = jsonControlGroup.toControlOperation();
        LOGGER.info("Starting control group {} with operation {} by user {}", aGroupId, groupControlOperation, aUser.getUser().getId());

        ControlGroupRequest grpCommand = new ControlGroupRequest(aGroupId, groupControlOperation);
        groupControlService.controlGroup(grpCommand, aUser.getUser());
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlGroups(final JsonControlGroup jsonControlGroup, final AuthenticatedUser authenticatedUser) {
        LOGGER.info("Control groups {} by user {}", jsonControlGroup, authenticatedUser.getUser().getId());
        groupControlService.controlGroups(new ControlGroupRequest(null, jsonControlGroup.toControlOperation()),
                authenticatedUser.getUser());
        return ResponseBuilder.ok();
    }

    protected List<MembershipDetails> createMembershipDetailsFromJvms(final List<Jvm> jvms) {
        final List<MembershipDetails> membershipDetailsList = new ArrayList<>(jvms.size());
        for (Jvm jvm : jvms) {
            final List<String> groupNames = new ArrayList<>(jvm.getGroups().size());
            for (Group group : jvm.getGroups()) {
                groupNames.add(group.getName());
            }
            membershipDetailsList.add(new MembershipDetails(jvm.getJvmName(),
                    GroupChildType.JVM,
                    groupNames));
        }
        return membershipDetailsList;
    }

    protected List<MembershipDetails> createMembershipDetailsFromWebServers(final List<WebServer> webServers) {
        final List<MembershipDetails> membershipDetailsList = new ArrayList<>(webServers.size());
        for (WebServer webServer : webServers) {
            final List<String> groupNames = new ArrayList<>(webServer.getGroups().size());
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

        LOGGER.debug("Get other group membership details of the children for group {} and child {}", id, groupChildType);
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

    @Override
    public Response getGroupJvmsResourceNames(String groupName) {
        LOGGER.debug("Get group JVMs resource names for group {}", groupName);
        return ResponseBuilder.ok(groupService.getGroupJvmsResourceTemplateNames(groupName));
    }

    @Override
    public Response getGroupWebServersResourceNames(String groupName) {
        LOGGER.debug("Get group web server resource names for group {}", groupName);
        return ResponseBuilder.ok(groupService.getGroupWebServersResourceTemplateNames(groupName));
    }

    @Context
    private MessageContext context;

    @Override
    public Response updateGroupAppResourceTemplate(final String groupName, final String appName, final String resourceTemplateName, final String content) {

        LOGGER.info("Updating the group template {} for {}", resourceTemplateName, groupName);
        LOGGER.debug(content);

        final Group group = groupService.getGroup(groupName);

        try {
            final String updatedContent = groupService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content);
            Set<Jvm> groupJvms = group.getJvms();
            Map<String, Future<Response>> futureContents = new HashMap<>();
            if (null != groupJvms) {
                LOGGER.info("Updating the templates for all the JVMs in group {}", groupName);
                final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                for (final Jvm jvm : groupJvms) {
                    final String jvmName = jvm.getJvmName();
                    LOGGER.info("Updating JVM {} template {}", jvmName, resourceTemplateName);
                    Future<Response> futureContent = executorService.submit(new Callable<Response>() {
                        @Override
                        public Response call() throws Exception {
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            return applicationServiceRest.updateResourceTemplate(appName, resourceTemplateName, jvmName, groupName, updatedContent);
                        }
                    });
                    futureContents.put(jvmName, futureContent);
                }
                checkResponsesForErrorStatus(futureContents);
            } else {
                LOGGER.info("No JVMs to update in group {}", groupName);
            }

            LOGGER.info("Update SUCCESSFUL");
            return ResponseBuilder.ok(updatedContent);

        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.PERSISTENCE_ERROR, e.getMessage()));
        } catch (ResourceFileGeneratorException e) {
            LOGGER.error("Fail to generate the resource file {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(FaultType.BAD_STREAM, "Fail to generate the resource file " + resourceTemplateName, e));
        }
    }

    @Override
    public Response generateAndDeployGroupAppFile(final String groupName, final String fileName, final String appName, final AuthenticatedUser aUser, final String hostName) {

        LOGGER.info("Generate and deploy group app file {} for group {} by user {} to host {}", fileName, groupName, aUser.getUser().getId(), hostName);

        Group group = groupService.getGroup(groupName);
        final String groupAppMetaData = groupService.getGroupAppResourceTemplateMetaData(groupName, fileName);
        ResourceTemplateMetaData metaData;
        try {
            // cannot call getTokenizedMetaData here - the app resource could be associated to a JVM and use JVM attributes
            metaData = resourceService.getMetaData(groupAppMetaData);
            if (metaData.getEntity().getDeployToJvms()) {
                // deploy to all jvms in group
                performGroupAppDeployToJvms(groupName, fileName, aUser, group, appName, applicationServiceRest, hostName, metaData.isHotDeploy());
            } else {
                ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                        .setGroupName(groupName)
                        .setWebAppName(appName)
                        .setResourceName(fileName)
                        .build();
                resourceService.validateSingleResourceForGeneration(resourceIdentifier);
                if (hostName != null && !hostName.isEmpty()) {
                    // deploy to particular host
                    performGroupAppDeployToHost(groupName, fileName, appName, hostName, metaData.isHotDeploy());
                } else {
                    // deploy to all hosts in group
                    performGroupAppDeployToHosts(groupName, fileName, appName, metaData.isHotDeploy());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to map meta data for resource template {} in group {} :: meta data: {} ", fileName, groupName, groupAppMetaData, e);
            throw new InternalErrorException(FaultType.BAD_STREAM, "Failed to map meta data for resource template " + fileName + " in group " + groupName, e);
        }
        return ResponseBuilder.ok(group);
    }

    /**
     * This method deploys group app config template to only one host
     *
     * @param groupName name of the group we can find the webapp under
     * @param fileName  name of the file that needs to be deployed to the host
     * @param appName   name of the application which needs to be deployed
     * @param hostName  name of the host to which we want the file to be deployed to
     * @param hotDeploy meta data attribute that specifies if the resource can be hot deployed to a JVM
     */
    private void performGroupAppDeployToHost(final String groupName, final String fileName, final String appName, final String hostName, boolean hotDeploy) {
        Map<String, Future<Response>> futureMap = new HashMap<>();
        final Group group = groupService.getGroup(groupName);
        Set<Jvm> jvms = new HashSet<>();
        new ArrayList<Jvm>(group.getJvms()).stream().filter(jvm -> jvm.getHostName().equalsIgnoreCase(hostName)).forEach(jvms::add);
        if (!jvms.isEmpty()) {

            checkJvmsStatesBeforeDeployAppResource(fileName, group, hotDeploy, jvms);

            Future<Response> response = createFutureResponseForAppDeploy(groupName, fileName, appName, null, hostName);
            if (response != null) {
                futureMap.put(hostName, response);
                checkResponsesForErrorStatus(futureMap);
            }
        }
        if (!futureMap.isEmpty()) {
            checkResponsesForErrorStatus(futureMap);
        }

    }

    private void performGroupAppDeployToHosts(final String groupName, final String fileName, final String appName, boolean hotDeploy) {
        Map<String, Future<Response>> futureMap = new HashMap<>();
        final Group group = groupService.getGroup(groupName);
        Set<Jvm> jvms = group.getJvms();
        if (null != jvms && !jvms.isEmpty()) {

            checkJvmsStatesBeforeDeployAppResource(fileName, group, hotDeploy, jvms);

            List<String> deployedHosts = new ArrayList<>(jvms.size());
            for (final Jvm jvm : jvms) {
                final String hostName = jvm.getHostName();
                if (!deployedHosts.contains(hostName)) {
                    deployedHosts.add(hostName);
                    Future<Response> response = createFutureResponseForAppDeploy(groupName, fileName, appName, jvm, null);
                    if (response != null)
                        futureMap.put(hostName, response);
                }
            }
        }
        if (!futureMap.isEmpty()) {
            checkResponsesForErrorStatus(futureMap);
        }
    }

    void performGroupAppDeployToJvms(final String groupName, final String fileName, final AuthenticatedUser aUser, final Group group,
                                     final String appName, final ApplicationServiceRest appServiceRest, final String hostName, boolean hotDeploy) {
        final Set<Jvm> groupJvms = group.getJvms();
        Set<Jvm> jvms = getJvmsByHostname(hostName, groupJvms);
        if (null != jvms && !jvms.isEmpty()) {
            checkJvmsStatesBeforeDeployAppResource(fileName, group, hotDeploy, jvms);
            Map<String, Future<Response>> futureMap = executeGroupAppDeployToJvms(groupName, fileName, aUser, appName, appServiceRest, jvms);
            checkResponsesForErrorStatus(futureMap);
        }
    }

    private Set<Jvm> getJvmsByHostname(String hostName, Set<Jvm> groupJvms) {
        Set<Jvm> jvms;
        if (hostName != null && !hostName.isEmpty()) {
            LOGGER.debug("got hostname {} deploying template to host jvms only", hostName);
            jvms = new HashSet<>();
            for (Jvm jvm : groupJvms) {
                if (jvm.getHostName().equalsIgnoreCase(hostName)) {
                    jvms.add(jvm);
                }
            }
        } else {
            LOGGER.debug("got no hostname deploying to all group jvms");
            jvms = groupJvms;
        } return jvms;
    }

    private Map<String, Future<Response>> executeGroupAppDeployToJvms(final String groupName, final String fileName, final AuthenticatedUser aUser, final String appName, final ApplicationServiceRest appServiceRest, Set<Jvm> jvms) {
        final String groupAppTemplateContent = groupService.getGroupAppResourceTemplate(groupName, appName, fileName, false, new ResourceGroup());
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final  Map<String, Future<Response>> futureMap = new HashMap<>();
        for (Jvm jvm : jvms) {
            final String jvmName = jvm.getJvmName();
            Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    appServiceRest.updateResourceTemplate(appName, fileName, jvmName, groupName, groupAppTemplateContent);
                    return appServiceRest.deployConf(appName, groupName, jvmName, fileName, aUser);
                }
            });
            futureMap.put(jvmName, responseFuture);
        }
        return futureMap;
    }

    private void checkJvmsStatesBeforeDeployAppResource(String fileName, Group group, boolean hotDeploy, Set<Jvm> jvms) {
        List<String> jvmsStarted = new ArrayList<>();
        for (Jvm jvm : jvms) {
            if (jvm.getState().isStartedState()) {
                if (hotDeploy) {
                    LOGGER.info("JVM {} is started, but hot deploy for {} is true. Continuing with deploy ...", jvm.getJvmName(), fileName);
                } else {
                    jvmsStarted.add(jvm.getJvmName());
                }
            }
        }

        if (!jvmsStarted.isEmpty()){
            String deployMsg = MessageFormat.format("Failed to deploy file {0} for group {1}: not all JVMs were stopped - the following JVMs were started and the resource was not configured with hotDeploy=true: {2}", fileName, group.getName(), jvmsStarted);
            LOGGER.error(deployMsg);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, deployMsg);
        }
    }

    /**
     * Creates future object for application template deploy.
     *
     * @param groupName name of the group under which the app exists
     * @param fileName  name of the file that needs to be deployed
     * @param appName   name of the app which contains the template
     * @param jvm       name of the jvm to which the app needs to deploy the template to
     * @param hostName  name of the host where the resources need to be deployed to
     * @return returns a Future<Response> object if successful.
     */
    private Future<Response> createFutureResponseForAppDeploy(final String groupName, final String fileName, final String appName, final Jvm jvm, final String hostName) {
        final Application application = applicationService.getApplication(appName);
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Future<Response> responseFuture = executorService.submit(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                SecurityContextHolder.getContext().setAuthentication(auth);
                CommandOutput commandOutput;
                if (jvm != null) {
                    LOGGER.debug("got jvm object with id {}, creating command output with jvm", jvm.getId().getId());
                    commandOutput = groupService.deployGroupAppTemplate(groupName, fileName, application, jvm);
                } else {
                    LOGGER.debug("got jvm as null creating app templates for hostname {}", hostName);
                    commandOutput = groupService.deployGroupAppTemplate(groupName, fileName, application, hostName);
                }
                if (commandOutput.getReturnCode().wasSuccessful()) {
                    return ResponseBuilder.ok(commandOutput);
                } else {
                    return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(FaultType.REMOTE_COMMAND_FAILURE, commandOutput.toString()));
                }
            }
        });
        return responseFuture;
    }

    @Override
    public Response previewGroupAppResourceTemplate(String groupName, String resourceTemplateName, String template) {
        LOGGER.debug("Preview group app resource {} in group {}", resourceTemplateName, groupName);
        LOGGER.debug(template);
        try {
            return ResponseBuilder.ok(groupService.previewGroupAppResourceTemplate(groupName, resourceTemplateName, template, resourceService.generateResourceGroup()));
        } catch (RuntimeException e) {
            LOGGER.error("Failed to preview the application template {} for {}", resourceTemplateName, groupName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_TEMPLATE, e.getMessage(), e));
        }
    }

    @Override
    public Response getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, boolean tokensReplaced) {
        LOGGER.debug("Get group app resource template {} for app {} in group {} : tokens replaced={}", resourceTemplateName, appName, groupName, tokensReplaced);
        return ResponseBuilder.ok(groupService.getGroupAppResourceTemplate(groupName, appName, resourceTemplateName, tokensReplaced, tokensReplaced ? resourceService.generateResourceGroup() : new ResourceGroup()));
    }

    @Override
    public Response getGroupAppResourceNames(String groupName) {
        LOGGER.debug("Get group app resource names {}", groupName);
        return ResponseBuilder.ok(groupService.getGroupAppsResourceTemplateNames(groupName));
    }

    @Override
    public Response getStartedWebServersAndJvmsCount() {
        LOGGER.debug("Get started Web Servers and JVMs count");
        final List<Group> groupList = groupService.getGroups();
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>(groupList.size());
        for (final Group group : groupList) {
            final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(group.getName())
                    .setJvmStartedCount(jvmService.getJvmStartedCount(group.getName()))
                    .setJvmCount(jvmService.getJvmCount(group.getName()))
                    .setWebServerStartedCount(webServerService.getWebServerStartedCount(group.getName()))
                    .setWebServerCount(webServerService.getWebServerCount(group.getName())).build();
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStartedAndStoppedWebServersAndJvmsCount() {
        LOGGER.debug("Get started and stopped Web Servers and JVMs count");
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>(groupService.getGroups().size());
        for (final Group group : groupService.getGroups()) {
            final GroupServerInfo groupServerInfo = getGroupServerInfo(group.getName());
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStartedWebServersAndJvmsCount(final String groupName) {
        LOGGER.debug("Get started Web Servers and JVMs count for group {}", groupName);
        final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(groupName)
                .setJvmStartedCount(jvmService.getJvmStartedCount(groupName))
                .setJvmCount(jvmService.getJvmCount(groupName))
                .setWebServerStartedCount(webServerService.getWebServerStartedCount(groupName))
                .setWebServerCount(webServerService.getWebServerCount(groupName)).build();
        return ResponseBuilder.ok(groupServerInfo);
    }

    @Override
    public Response getStartedAndStoppedWebServersAndJvmsCount(final String groupName) {
        LOGGER.debug("Get started and stopped Web Servers and JVMs coount in group {}", groupName);
        final GroupServerInfo groupServerInfo = getGroupServerInfo(groupName);
        return ResponseBuilder.ok(groupServerInfo);
    }

    /**
     * Get a group's children servers info (e.g. jvm count, web server count etc...)
     *
     * @param groupName the group name
     * @return {@link GroupServerInfo}
     */
    protected GroupServerInfo getGroupServerInfo(final String groupName) {
        return new GroupServerInfoBuilder().setGroupName(groupName)
                .setJvmStartedCount(jvmService.getJvmStartedCount(groupName))
                .setJvmStoppedCount(jvmService.getJvmStoppedCount(groupName))
                .setJvmForciblyStoppedCount(jvmService.getJvmForciblyStoppedCount(groupName))
                .setJvmCount(jvmService.getJvmCount(groupName))
                .setWebServerStartedCount(webServerService.getWebServerStartedCount(groupName))
                .setWebServerStoppedCount(webServerService.getWebServerStoppedCount(groupName))
                .setWebServerCount(webServerService.getWebServerCount(groupName)).build();
    }

    @Override
    public Response getStoppedWebServersAndJvmsCount() {
        LOGGER.debug("Get stopped Web Servers and JVMs count");
        final List<GroupServerInfo> groupServerInfos = new ArrayList<>(groupService.getGroups().size());
        for (final Group group : groupService.getGroups()) {
            final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(group.getName())
                    .setJvmStoppedCount(jvmService.getJvmStoppedCount(group.getName()))
                    .setJvmForciblyStoppedCount(jvmService.getJvmForciblyStoppedCount(group.getName()))
                    .setJvmCount(jvmService.getJvmCount(group.getName()))
                    .setWebServerStoppedCount(webServerService.getWebServerStoppedCount(group.getName()))
                    .setWebServerCount(webServerService.getWebServerCount(group.getName())).build();
            groupServerInfos.add(groupServerInfo);
        }
        return ResponseBuilder.ok(groupServerInfos);
    }

    @Override
    public Response getStoppedWebServersAndJvmsCount(final String groupName) {
        LOGGER.debug("Get stopped Web Servers and JVMs count in group {}", groupName);
        final GroupServerInfo groupServerInfo = new GroupServerInfoBuilder().setGroupName(groupName)
                .setJvmStoppedCount(jvmService.getJvmStoppedCount(groupName))
                .setJvmForciblyStoppedCount(jvmService.getJvmForciblyStoppedCount(groupName))
                .setJvmCount(jvmService.getJvmCount(groupName))
                .setWebServerStoppedCount(webServerService.getWebServerStoppedCount(groupName))
                .setWebServerCount(webServerService.getWebServerCount(groupName)).build();
        return ResponseBuilder.ok(groupServerInfo);
    }

    @Override
    public Response areAllJvmsStopped(final String groupName) {
        LOGGER.debug("Are all JVMs stopped in group {}", groupName);
        HashMap<String, String> resultTrue = new HashMap<>();
        resultTrue.put("allStopped", Boolean.TRUE.toString());
        Group group = groupService.getGroup(groupName);
        Set<Jvm> jvms = group.getJvms();
        if (null != jvms && !jvms.isEmpty()) {
            for (final Jvm jvm : jvms) {
                if (jvm.getState().isStartedState()) {
                    HashMap<String, String> notStopped = new HashMap<>();
                    notStopped.put("allStopped", Boolean.FALSE.toString());
                    notStopped.put("entityNotStopped", jvm.getJvmName());
                    return ResponseBuilder.ok(notStopped);
                }
            }
            return ResponseBuilder.ok(resultTrue);
        } else {
            return ResponseBuilder.ok(resultTrue);
        }
    }

    @Override
    public Response areAllWebServersStopped(final String groupName) {
        LOGGER.debug("Are all web servers stopped in group {}", groupName);
        HashMap<String, String> resultTrue = new HashMap<>();
        resultTrue.put("allStopped", Boolean.TRUE.toString());
        Group group = groupService.getGroup(groupName);
        group = groupService.getGroupWithWebServers(group.getId());
        Set<WebServer> webServers = group.getWebServers();
        if (null != webServers && !webServers.isEmpty()) {
            for (final WebServer webServer : webServers) {
                if (webServerService.isStarted(webServer)) {
                    HashMap<String, String> notStopped = new HashMap<>();
                    notStopped.put("allStopped", Boolean.FALSE.toString());
                    notStopped.put("entityNotStopped", webServer.getName());
                    return ResponseBuilder.ok(notStopped);
                }
            }
            return ResponseBuilder.ok(resultTrue);
        } else {
            return ResponseBuilder.ok(resultTrue);
        }
    }

    @Override
    public Response getHosts(final String groupName) {
        return ResponseBuilder.ok(groupService.getHosts(groupName));
    }

    @Override
    public Response getAllHosts() {
        return ResponseBuilder.ok(groupService.getAllHosts());
    }

    @Override
    public Response getGroupState(final String groupName) {
        return ResponseBuilder.ok(groupStateNotificationService.getGroupState(groupName));
    }

    @Override
    public Response getGroupStates() {
        return ResponseBuilder.ok(groupStateNotificationService.getGroupStates());
    }

}