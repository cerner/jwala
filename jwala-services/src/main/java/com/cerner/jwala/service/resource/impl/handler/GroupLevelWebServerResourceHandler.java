package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handler for a group level web server resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by Jedd Cuison on 7/21/2016
 */
public class GroupLevelWebServerResourceHandler extends ResourceHandler {

    private final GroupPersistenceService groupPersistenceService;
    private final WebServerPersistenceService webServerPersistenceService;
    private final ResourceContentGeneratorService resourceContentGeneratorService;

    public GroupLevelWebServerResourceHandler(final ResourceDao resourceDao,
                                              final GroupPersistenceService groupPersistenceService,
                                              final WebServerPersistenceService webServerPersistenceService,
                                              final ResourceContentGeneratorService resourceContentGeneratorService,
                                              final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.resourceContentGeneratorService = resourceContentGeneratorService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getGroupLevelWebServerResource(resourceIdentifier.resourceName, resourceIdentifier.groupName);
        } else if (successor != null) {
            configTemplate = successor.fetchResource(resourceIdentifier);
        }
        return configTemplate;
    }

    @Override
    public CreateResourceResponseWrapper createResource(final ResourceIdentifier resourceIdentifier,
                                                        final ResourceTemplateMetaData metaData,
                                                        final String templateContent) {
        CreateResourceResponseWrapper createResourceResponseWrapper = null;
        if (canHandle(resourceIdentifier)) {

            final Group group = groupPersistenceService.getGroupWithWebServers(resourceIdentifier.groupName);
            final Set<WebServer> webServers = group.getWebServers();
            final Map<String, UploadWebServerTemplateRequest> uploadWebServerTemplateRequestMap = new HashMap<>();
            ConfigTemplate createdConfigTemplate = null;

            for (final WebServer webServer : webServers) {

                UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                        metaData.getTemplateName(), metaData.getJsonData(), templateContent) {
                    @Override
                    public String getConfFileName() {
                        return metaData.getDeployFileName();
                    }
                };

                // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
                // configuration template. Note that ResourceGroup is null since we only need the web server paths and
                // application properties for mapping.
                final String generatedDeployPath = resourceContentGeneratorService.generateContent(metaData.getTemplateName(), metaData.getDeployPath(), null, webServer, ResourceGeneratorType.METADATA);
                createdConfigTemplate = webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebServerTemplateRequest,
                        generatedDeployPath + "/" + metaData.getDeployFileName(), null);
            }

            UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(null,
                    metaData.getTemplateName(), metaData.getJsonData(), templateContent) {
                @Override
                public String getConfFileName() {
                    return metaData.getDeployFileName();
                }
            };
            uploadWebServerTemplateRequestMap.put(metaData.getDeployFileName(), uploadWebServerTemplateRequest);
            groupPersistenceService.populateGroupWebServerTemplates(group.getName(), uploadWebServerTemplateRequestMap);
            createResourceResponseWrapper = new CreateResourceResponseWrapper(createdConfigTemplate);
        } else if (successor != null) {
            createResourceResponseWrapper = successor.createResource(resourceIdentifier, metaData, templateContent);
        }
        return createResourceResponseWrapper;
    }

    @Override
    public void deleteResource(final ResourceIdentifier resourceIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canHandle(final ResourceIdentifier resourceIdentifier) {
        return StringUtils.isNotEmpty(resourceIdentifier.resourceName) &&
               StringUtils.isNotEmpty(resourceIdentifier.groupName) &&
                "*".equalsIgnoreCase(resourceIdentifier.webServerName) &&
               StringUtils.isEmpty(resourceIdentifier.jvmName) &&
               StringUtils.isEmpty(resourceIdentifier.webAppName);
    }

    @Override
    public String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData) {
        if (canHandle(resourceIdentifier)) {
            final String updatedMetaData = groupPersistenceService.updateGroupWebServerResourceMetaData(resourceIdentifier.groupName, resourceName, metaData);
            Set<WebServer> webServersSet = groupPersistenceService.getGroupWithWebServers(resourceIdentifier.groupName).getWebServers();
            for (WebServer webServer : webServersSet){
                webServerPersistenceService.updateResourceMetaData(webServer.getName(), resourceName, metaData);
            }
            return updatedMetaData;
        } else {
            return successor.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
        }
    }

    @Override
    public Object getSelectedValue(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)){
            return null;
        } else {
            return successor.getSelectedValue(resourceIdentifier);
        }
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)){
            throw new UnsupportedOperationException();
        } else {
            return successor.getResourceNames(resourceIdentifier);
        }
    }
}
