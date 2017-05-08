package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import com.cerner.jwala.persistence.jpa.domain.builder.JpaWebServerBuilder;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.media.MediaService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class WebServerServiceImpl implements WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceImpl.class);
    private static final String INSTALL_SERVICE_WSBAT_TEMPLATE_TPL_PATH = "/install-service-http.bat.tpl";
    public static final String INSTALL_SERVICE_SCRIPT_NAME = "install-service-http.bat";

    private final WebServerPersistenceService webServerPersistenceService;

    private final ResourceService resourceService;

    private InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService;

    private final String templatePath;

    private final BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    private WebServerControlService webServerControlService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    private final ModelMapper modelMapper = new ModelMapper();

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
        validateCreateWebServer(createWebServerRequest);

        final List<Long> groupIdList = new ArrayList<>();
        for (Identifier<Group> id : createWebServerRequest.getGroups()) {
            groupIdList.add(id.getId());
        }

        final JpaWebServer jpaWebServer = new JpaWebServer();

        final List<JpaGroup> jpaGroupList = groupPersistenceService.findGroups(groupIdList);
        jpaWebServer.setGroups(jpaGroupList);

        jpaWebServer.setName(createWebServerRequest.getName());
        jpaWebServer.setHost(createWebServerRequest.getHost());
        jpaWebServer.setPort(createWebServerRequest.getPort());
        jpaWebServer.setHttpsPort(createWebServerRequest.getHttpsPort());
        jpaWebServer.setStatusPath(createWebServerRequest.getStatusPath().getPath());
        jpaWebServer.setState(createWebServerRequest.getState());

        final JpaMedia jpaApacheHttpdMedia = createWebServerRequest.getApacheHttpdMediaId() == null ? null :
                mediaService.find(Long.valueOf(createWebServerRequest.getApacheHttpdMediaId()));
        jpaWebServer.setApacheHttpdMedia(jpaApacheHttpdMedia);

        final JpaWebServer createdJpaWebServer = webServerPersistenceService.createWebServer(jpaWebServer);

        // associate the web server to the group
        for (JpaGroup wsGroup : jpaGroupList) {
            final List<JpaWebServer> webServers = wsGroup.getWebServers();
            webServers.add(createdJpaWebServer);
            wsGroup.setWebServers(webServers);
        }

        inMemoryStateManagerService.put(new Identifier<>(createdJpaWebServer.getId()), createdJpaWebServer.getState());
        return new JpaWebServerBuilder(createdJpaWebServer).build();
    }

    private void validateCreateWebServer(CreateWebServerRequest createWebServerRequest) {
        try {
            jvmPersistenceService.findJvmByExactName(createWebServerRequest.getName());
            String message = MessageFormat.format("Jvm already exists with this name {0}", createWebServerRequest.getName());
            LOGGER.error(message);
            throw new WebServerServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No jvm name conflict, ignore no result exception for creating webserver", pe);
        }
        try {
            webServerPersistenceService.findWebServerByName(createWebServerRequest.getName());
            String message = MessageFormat.format("Webserver already exists with this name {0}", createWebServerRequest.getName());
            LOGGER.error(message);
            throw new WebServerServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No webserver name conflict, ignore no result exception for creating webserver", pe);
        }
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

        WebServer originalWebServer = getWebServer(anUpdateWebServerCommand.getId());
        if (!anUpdateWebServerCommand.getNewName().equalsIgnoreCase(originalWebServer.getName()) && !WebServerReachableState.WS_NEW.equals(originalWebServer.getState())) {
            throw new WebServerServiceException(MessageFormat.format("Web Server {0} is in {1} state, can only rename new web servers",
                    originalWebServer.getName(), originalWebServer.getState().toStateLabel()));
        }

        if (!originalWebServer.getName().equalsIgnoreCase(anUpdateWebServerCommand.getNewName())) {
            validateUpdateWebServer(anUpdateWebServerCommand);
        }

        final List<Long> groupIdList = new ArrayList<>();
        for (Identifier<Group> id : anUpdateWebServerCommand.getNewGroupIds()) {
            groupIdList.add(id.getId());
        }

        final JpaWebServer jpaWebServer = webServerPersistenceService.findWebServer(anUpdateWebServerCommand.getId().getId());

        final List<JpaGroup> jpaGroupList = groupPersistenceService.findGroups(groupIdList);
        jpaWebServer.setGroups(jpaGroupList);

        jpaWebServer.setName(anUpdateWebServerCommand.getNewName());
        jpaWebServer.setHost(anUpdateWebServerCommand.getNewHost());
        jpaWebServer.setPort(anUpdateWebServerCommand.getNewPort());
        jpaWebServer.setHttpsPort(anUpdateWebServerCommand.getNewHttpsPort());
        jpaWebServer.setStatusPath(anUpdateWebServerCommand.getNewStatusPath().getPath());

        final JpaMedia jpaApacheHttpdMedia = anUpdateWebServerCommand.getApacheHttpdMediaId() == null ? null :
                mediaService.find(Long.valueOf(anUpdateWebServerCommand.getApacheHttpdMediaId()));
        jpaWebServer.setApacheHttpdMedia(jpaApacheHttpdMedia);

        final JpaWebServer updatedWebServer = webServerPersistenceService.updateWebServer(jpaWebServer);

        // associate the web server to the group
        for (JpaGroup wsGroup : jpaGroupList) {
            final List<JpaWebServer> webServers = wsGroup.getWebServers();
            webServers.add(updatedWebServer);
            wsGroup.setWebServers(webServers);
        }

        return new JpaWebServerBuilder(updatedWebServer).build();
    }

    private void validateUpdateWebServer(UpdateWebServerRequest anUpdateWebServerCommand) {
        try {
            jvmPersistenceService.findJvmByExactName(anUpdateWebServerCommand.getNewName());
            String message = MessageFormat.format("Jvm already exists with this name {0}", anUpdateWebServerCommand.getNewName());
            LOGGER.error(message);
            throw new WebServerServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No jvm name conflict, ignore no result exception for creating webserver", pe);
        }
        try {
            webServerPersistenceService.findWebServerByName(anUpdateWebServerCommand.getNewName());
            String message = MessageFormat.format("WebServer already exists with this name {0}", anUpdateWebServerCommand.getNewName());
            LOGGER.error(message);
            throw new WebServerServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No webserver name conflict, ignore no result exception for creating webserver", pe);
        }
    }

    @Override
    @Transactional
    public void deleteWebServer(final Identifier<WebServer> id, final boolean hardDelete, final User user) {
        LOGGER.info("Deleting web server with id = {} and hardDelete = {}", id, hardDelete);
        final WebServer webServer = webServerPersistenceService.getWebServer(id);

        if (!hardDelete && !WebServerReachableState.WS_NEW.equals(webServer.getState())) {
            final String msg = MessageFormat.format("Cannot delete web server {0} since it has already been deployed",
                    webServer.getName());
            LOGGER.error(msg);
            throw new WebServerServiceException(msg);
        }

        if (hardDelete) {
            LOGGER.info("Deleting web server service {}", webServer.getName());

            if (isStarted(webServer)) {
                final String msg = MessageFormat.format("Please stop web server {0} first before attempting to delete it",
                        webServer.getName());
                LOGGER.warn(msg); // this is not a system error hence we only log it as a warning even though we throw
                // an exception
                throw new WebServerServiceException(msg);
            }

            if (!WebServerReachableState.WS_NEW.equals(webServer.getState())) {
                final CommandOutput commandOutput = webServerControlService.controlWebServer(new ControlWebServerRequest(webServer.getId(),
                        WebServerControlOperation.DELETE_SERVICE), user);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    final String msg = MessageFormat.format("Failed to delete the web server service {0}! CommandOutput = {1}",
                            webServer.getName(), commandOutput);
                    LOGGER.error(msg);
                    throw new WebServerServiceException(msg);
                }
            }
        }

        webServerPersistenceService.removeWebServer(id);
        inMemoryStateManagerService.remove(id);
    }

    @Override
    public boolean isStarted(WebServer webServer) {
        final WebServerReachableState state = webServer.getState();
        return !WebServerReachableState.WS_UNREACHABLE.equals(state) && !WebServerReachableState.FORCED_STOPPED.equals(state)
                && !WebServerReachableState.WS_NEW.equals(state);
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
            throw new WebServerServiceException("Error generating " + INSTALL_SERVICE_SCRIPT_NAME + "!", ioe);
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
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setWebServerName(webServerName)
                .setResourceName(fileName)
                .build();

        try {
            checkWebServerStateBeforeDeploy(webServer, resourceIdentifier);

            resourceService.validateSingleResourceForGeneration(resourceIdentifier);
            resourceService.generateAndDeployFile(resourceIdentifier, webServerName, fileName, webServer.getHost());
        } catch (IOException e) {
            String errorMsg = MessageFormat.format("Failed to retrieve meta data when generating and deploying file {0} for Web Server {1}", fileName, webServerName);
            LOGGER.error(errorMsg, e);
            throw new WebServerServiceException(errorMsg);
        } finally {
            binaryDistributionLockManager.writeUnlock(webServerName + "-" + webServer.getId().getId().toString());
        }

        return webServer;
    }

    private void checkWebServerStateBeforeDeploy(WebServer webServer, ResourceIdentifier resourceIdentifier) throws IOException {
        final String metaDataStr = resourceService.getResourceContent(resourceIdentifier).getMetaData();
        ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(resourceIdentifier.resourceName, webServer, metaDataStr);
        if (isStarted(webServer) && !metaData.isHotDeploy()) {
                String errorMsg = MessageFormat.format("The target Web Server {0} must be stopped or the resource must be configured to be hotDeploy=true before attempting to deploy the resource {1}", resourceIdentifier.webServerName, resourceIdentifier.resourceName);
                LOGGER.error(errorMsg);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, errorMsg);
        }
        LOGGER.info("Web Server {} is started, but resource {} is configured to be hot deployed. Continuing with deploy ...", resourceIdentifier.webServerName, resourceIdentifier.resourceName);
    }
}
