package com.cerner.jwala.ws.rest.v1.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

public class ApplicationServiceRestImpl implements ApplicationServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceRestImpl.class);
    @Autowired
    private BinaryDistributionControlService binaryDistributionControlService;
    private ApplicationService service;
    private ResourceService resourceService;
    private final GroupService groupService;

    public ApplicationServiceRestImpl(ApplicationService applicationService,
                                      ResourceService resourceService,
                                      final GroupService groupService) {
        service = applicationService;
        this.resourceService = resourceService;
        this.groupService = groupService;
    }

    @Override
    public Response getApplication(Identifier<Application> anAppId) {
        LOGGER.debug("Get App by id: {}", anAppId);
        final Application app = service.getApplication(anAppId);
        return ResponseBuilder.ok(app);
    }

    @Override
    public Response getApplicationByName(final String name) {
        return ResponseBuilder.ok(service.getApplication(name));
    }

    @Override
    public Response getApplications(Identifier<Group> aGroupId) {
        LOGGER.debug("Get Apps requested with groupId: {}", aGroupId != null ? aGroupId : "null");
        final List<Application> apps;
        if (aGroupId != null) {
            apps = service.findApplications(aGroupId);
        } else {
            apps = service.getApplications();
        }
        return ResponseBuilder.ok(apps);
    }

    @Override
    public Response findApplicationsByJvmId(Identifier<Jvm> aJvmId) {
        LOGGER.debug("Find Apps requested with aJvmId: {}", aJvmId != null ? aJvmId : "null");
        if (aJvmId != null) {
            final List<Application> apps = service.findApplicationsByJvmId(aJvmId);
            return ResponseBuilder.ok(apps);
        } else {
            final List<Application> apps = service.getApplications();
            return ResponseBuilder.ok(apps);
        }
    }

    @Override
    public Response createApplication(final JsonCreateApplication anAppToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Create Application requested: {}", anAppToCreate);
        try {
            Application created = service.createApplication(anAppToCreate.toCreateCommand(), aUser.getUser());
            return ResponseBuilder.created(created);
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_APPLICATION, eee.getMessage(), eee));
        }
    }

    @Override
    public Response updateApplication(final JsonUpdateApplication anAppToUpdate, final AuthenticatedUser aUser)
            throws Exception {
        LOGGER.info("Update Application requested: {}", anAppToUpdate);
        try {
            Application updated = service.updateApplication(anAppToUpdate.toUpdateCommand(), aUser.getUser());
            return ResponseBuilder.ok(updated);
        } catch (EntityExistsException eee) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_APPLICATION, eee.getMessage(), eee));
        }
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove, final AuthenticatedUser aUser) {
        LOGGER.info("Delete application requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove, aUser.getUser());
        return ResponseBuilder.ok();
    }


    @Override
    public Response deployWebArchive(final Identifier<Application> anAppToGet, final AuthenticatedUser aUser) {
        LOGGER.info("Deploying web archive for app ID {}", anAppToGet);
        Application app = service.getApplication(anAppToGet);
        final Group group = app.getGroup();
        Set<Jvm> jvms = group.getJvms();
        final String appName = app.getName();
        final String groupName = group.getName();
        if (null != jvms && !jvms.isEmpty()) {
            service.copyApplicationWarToGroupHosts(app);
            service.deployApplicationResourcesToGroupHosts(groupName, app, resourceService.generateResourceGroup());
        } else {
            LOGGER.info("Skip deploying application {}, no JVM's in group {}", appName, groupName);
        }

        return ResponseBuilder.ok(app);
    }

    @Override
    public Response deployWebArchive(final Identifier<Application> anAppToGet, String hostName) {
        LOGGER.info("Deploying web archive for app ID {}", anAppToGet);
        Application app = service.getApplication(anAppToGet);
        service.copyApplicationWarToHost(app, hostName);
        return null;
    }

    @Override
    public Response getResourceNames(final String appName, final String jvmName) {
        LOGGER.debug("get resource names for {}@{}", appName, jvmName);
        return ResponseBuilder.ok(service.getResourceTemplateNames(appName, jvmName));
    }

    @Override
    public Response updateResourceTemplate(final String appName,
                                           final String resourceTemplateName,
                                           final String jvmName,
                                           final String groupName,
                                           final String content) {
        LOGGER.info("Update resource template {} for app {} associated to JVM {} in group {}", resourceTemplateName, appName, jvmName, groupName);
        LOGGER.debug(content);

        try {
            if (jvmName == null) {
                // TODO: Discuss with the team or users if updating a resource if a web app under a group means updating the resource of web apps under the JVMs as well.
                // Note: my 2 cents with the above comment is that it should be optional, e.g. the application should give the user
                //       means to indicate if the resource of a web app assigned to JVMs should be updated also.
                return ResponseBuilder.ok(groupService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content));
            }
            return ResponseBuilder.ok(service.updateResourceTemplate(appName, resourceTemplateName, content, jvmName, groupName));
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.debug("Failed to update resource template {}", resourceTemplateName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.PERSISTENCE_ERROR, e.getMessage()));
        }
    }

    @Override
    public Response deployConf(final String appName, final String groupName, final String jvmName,
                               final String resourceTemplateName, final AuthenticatedUser authUser) {
        LOGGER.info("Deploying the application conf file {} for app {} to JVM {} in group {} by ", resourceTemplateName, appName, jvmName, groupName, authUser.getUser().getId());
        return ResponseBuilder.ok(service.deployConf(appName, groupName, jvmName, resourceTemplateName, resourceService.generateResourceGroup(), authUser.getUser()));
    }

    @Override
    public Response previewResourceTemplate(final String appName, final String groupName, final String jvmName,
                                            final String fileName,
                                            final String template) {
        LOGGER.debug("Preview resource template for app {} in group {} for JVM {} with content {}", appName, groupName, jvmName, template);
        try {
            return ResponseBuilder.ok(service.previewResourceTemplate(fileName, appName, groupName, jvmName, template, resourceService.generateResourceGroup()));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing template.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }

    @Override
    public Response deployConf(final String appName, final AuthenticatedUser aUser, final String hostName) {
        LOGGER.info("Deploying application {} initiated by user {}", appName, aUser.getUser().getId());
        service.deployConf(appName, hostName, aUser.getUser());
        return ResponseBuilder.ok(appName);
    }

    @Override
    public Response checkIfFileExists(final String filePath, final AuthenticatedUser aUser, final String hostName) {
        return ResponseBuilder.ok(binaryDistributionControlService.checkFileExists(hostName, filePath));
    }


}
