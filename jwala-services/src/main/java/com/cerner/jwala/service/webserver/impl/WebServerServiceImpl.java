package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class WebServerServiceImpl implements WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceImpl.class);
    private static final String INSTALL_SERVICE_WSBAT_TEMPLATE_TPL_PATH = "/install-service-http.bat.tpl";
    public static final String INSTALL_SERVICE_SCRIPT_NAME = "install-service-http.bat";

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    private final WebServerPersistenceService webServerPersistenceService;

    private final ResourceService resourceService;

    private InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService;

    private final String templatePath;

    private final BinaryDistributionLockManager binaryDistributionLockManager;

    public WebServerServiceImpl(final WebServerPersistenceService webServerPersistenceService,
                                final ResourceService resourceService,
                                @Qualifier("webServerInMemoryStateManagerService")
                                final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService,
                                final String templatePath,
                                final BinaryDistributionLockManager binaryDistributionLockManager) {
        this.webServerPersistenceService = webServerPersistenceService;
        this.inMemoryStateManagerService = inMemoryStateManagerService;
        this.templatePath = templatePath;
        this.resourceService = resourceService;
        this.binaryDistributionLockManager = binaryDistributionLockManager;
        initInMemoryStateService();
    }

    private void initInMemoryStateService() {
        for (WebServer webServer : webServerPersistenceService.getWebServers()) {
            inMemoryStateManagerService.put(webServer.getId(), webServer.getState());
        }
    }

    @Override
    @Transactional
    public WebServer createWebServer(final CreateWebServerRequest createWebServerRequest,
                                     final User aCreatingUser) {
        createWebServerRequest.validate();
        if (null != jvmPersistenceService.findJvmByExactName(createWebServerRequest.getName())) {
            LOGGER.error("Jvm already exists with this name {}", createWebServerRequest.getName());
            throw new WebServerServiceException("Jvm already exists with this name "+ createWebServerRequest.getName());
        }
        final List<Group> groups = new LinkedList<>();
        for (Identifier<Group> id : createWebServerRequest.getGroups()) {
            groups.add(new Group(id, null));
        }
        final WebServer webServer = new WebServer(null,
                groups,
                createWebServerRequest.getName(),
                createWebServerRequest.getHost(),
                createWebServerRequest.getPort(),
                createWebServerRequest.getHttpsPort(),
                createWebServerRequest.getStatusPath(),
                null,
                createWebServerRequest.getSvrRoot(),
                createWebServerRequest.getDocRoot(),
                createWebServerRequest.getState(),
                createWebServerRequest.getErrorStatus());

        final WebServer wsReturnValue = webServerPersistenceService.createWebServer(webServer, aCreatingUser.getId());
        inMemoryStateManagerService.put(wsReturnValue.getId(), wsReturnValue.getState());
        return wsReturnValue;
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) {
        return webServerPersistenceService.getWebServer(aWebServerId);
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final String aWebServerName) {
        return webServerPersistenceService.findWebServerByName(aWebServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> getWebServers() {
        return webServerPersistenceService.getWebServers();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<WebServer> getWebServersPropagationNew() {
        return webServerPersistenceService.getWebServers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final Identifier<Group> aGroupId) {
        return webServerPersistenceService.findWebServersBelongingTo(aGroupId);
    }

    @Override
    @Transactional
    public WebServer updateWebServer(final UpdateWebServerRequest anUpdateWebServerCommand,
                                     final User anUpdatingUser) {
        anUpdateWebServerCommand.validate();
        if (null != jvmPersistenceService.findJvmByExactName(anUpdateWebServerCommand.getNewName())) {
            LOGGER.error("Jvm already exists with this name {}", anUpdateWebServerCommand.getNewName());
            throw new WebServerServiceException("Jvm already exists with this name "+ anUpdateWebServerCommand.getNewName());
        }
        final List<Group> groups = new LinkedList<>();
        for (Identifier<Group> id : anUpdateWebServerCommand.getNewGroupIds()) {
            groups.add(new Group(id, null));
        }
        final Identifier<WebServer> id = anUpdateWebServerCommand.getId();
        final WebServer webServer = new WebServer(id,
                groups,
                anUpdateWebServerCommand.getNewName(),
                anUpdateWebServerCommand.getNewHost(),
                anUpdateWebServerCommand.getNewPort(),
                anUpdateWebServerCommand.getNewHttpsPort(),
                anUpdateWebServerCommand.getNewStatusPath(),
                webServerPersistenceService.getWebServer(id).getHttpConfigFile(),
                anUpdateWebServerCommand.getNewSvrRoot(),
                anUpdateWebServerCommand.getNewDocRoot(),
                anUpdateWebServerCommand.getState(),
                anUpdateWebServerCommand.getErrorStatus());

        return webServerPersistenceService.updateWebServer(webServer, anUpdatingUser.getId());
    }

    @Override
    @Transactional
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {
        webServerPersistenceService.removeWebServer(aWebServerId);
        inMemoryStateManagerService.remove(aWebServerId);
    }

    @Override
    public boolean isStarted(WebServer webServer) {
        final WebServerReachableState state = webServer.getState();
        return !WebServerReachableState.WS_UNREACHABLE.equals(state) && !WebServerReachableState.WS_NEW.equals(state);
    }

    @Override
    @Transactional
    public void updateErrorStatus(final Identifier<WebServer> id, final String errorStatus) {
        webServerPersistenceService.updateErrorStatus(id, errorStatus);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(final Identifier<WebServer> id, final WebServerReachableState state, final String errorStatus) {
        webServerPersistenceService.updateState(id, state, errorStatus);
        inMemoryStateManagerService.put(id, state);
    }

    @Override
    public String generateInstallServiceScript(WebServer webServer) {
        try {
            // NOTE: install_serviceWS.bat is internal to Jwala that is why the template is not in Db.
            return resourceService.generateResourceFile(INSTALL_SERVICE_SCRIPT_NAME, FileUtils.readFileToString(new File(templatePath + INSTALL_SERVICE_WSBAT_TEMPLATE_TPL_PATH)),
                    resourceService.generateResourceGroup(), webServer, ResourceGeneratorType.TEMPLATE);
        } catch (final IOException ioe) {
            throw new WebServerServiceException("Error generating " + INSTALL_SERVICE_SCRIPT_NAME+ "!", ioe);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(String webServerName) {
        return webServerPersistenceService.getResourceTemplateNames(webServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public String getResourceTemplate(final String webServerName, final String resourceTemplateName,
                                      final boolean tokensReplaced, final ResourceGroup resourceGroup) {
        final String template = webServerPersistenceService.getResourceTemplate(webServerName, resourceTemplateName);
        if (tokensReplaced) {
            WebServer webServer = webServerPersistenceService.findWebServerByName(webServerName);
            return resourceService.generateResourceFile(resourceTemplateName, template, resourceService.generateResourceGroup(), webServer, ResourceGeneratorType.TEMPLATE);
        }
        return template;
    }

    @Override
    public String getResourceTemplateMetaData(String aWebServerName, String resourceTemplateName) {
        return webServerPersistenceService.getResourceTemplateMetaData(aWebServerName, resourceTemplateName);
    }

    @Override
    @Transactional
    public void uploadWebServerConfig(WebServer webServer, String templateName, String templateContent, String metaDataStr, String groupName, User user) {
        try {

            ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(templateName, webServer, metaDataStr);
            ResourceIdentifier resourceId = new ResourceIdentifier.Builder()
                    .setResourceName(metaData.getDeployFileName())
                    .setGroupName(groupName)
                    .setWebServerName(webServer.getName()).build();
            resourceService.createResource(resourceId, metaData, IOUtils.toInputStream(templateContent));

        } catch (IOException e) {
            LOGGER.error("Failed to map meta data when uploading web server config {} for {}", templateName, webServer, e);
            throw new InternalErrorException(FaultType.BAD_STREAM, "Unable to map the meta data for template " + templateName, e);
        }
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        try {
            webServerPersistenceService.updateResourceTemplate(wsName, resourceTemplateName, template);
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update resource template {}", resourceTemplateName, e);
            return null;
        }
        return webServerPersistenceService.getResourceTemplate(wsName, resourceTemplateName);
    }

    @Override
    @Transactional(readOnly = true)
    public String previewResourceTemplate(final String fileName, final String webServerName, final String groupName, final String template) {
        return resourceService.generateResourceFile(fileName, template, resourceService.generateResourceGroup(),
                webServerPersistenceService.findWebServerByName(webServerName), ResourceGeneratorType.PREVIEW);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWebServerStartedCount(final String groupName) {
        return webServerPersistenceService.getWebServerStartedCount(groupName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWebServerCount(final String groupName) {
        return webServerPersistenceService.getWebServerCount(groupName);
    }

    @Override
    public Long getWebServerStoppedCount(final String groupName) {
        return webServerPersistenceService.getWebServerStoppedCount(groupName);
    }

    @Override
    public WebServer generateAndDeployFile(String webServerName, String fileName, User user) {
        WebServer webServer = getWebServer(webServerName);
        binaryDistributionLockManager.writeLock(webServerName + "-" + webServer.getId().getId().toString());
        try {
            // check the web server state
            if (isStarted(getWebServer(webServerName))) {
                LOGGER.error("The target Web Server {} must be stopped before attempting to update the resource file", webServerName);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "The target Web Server must be stopped before attempting to update the resource file");
            }
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setWebServerName(webServerName)
                    .setResourceName(fileName)
                    .build();
            resourceService.validateSingleResourceForGeneration(resourceIdentifier);
            resourceService.generateAndDeployFile(resourceIdentifier, webServerName, fileName, webServer.getHost());
        } finally {
            binaryDistributionLockManager.writeUnlock(webServerName + "-" + webServer.getId().getId().toString());
        }
        return webServer;
    }
}
