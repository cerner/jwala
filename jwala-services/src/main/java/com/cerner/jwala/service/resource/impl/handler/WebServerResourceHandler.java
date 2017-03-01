package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;

import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Handler for a web server resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by Jedd Cuison on 7/21/2016
 */
public class WebServerResourceHandler extends ResourceHandler {

    private final WebServerPersistenceService webServerPersistenceService;
    private final ResourceContentGeneratorService resourceContentGeneratorService;

    public WebServerResourceHandler(final ResourceDao resourceDao,
                                    final WebServerPersistenceService webServerPersistenceService,
                                    final ResourceContentGeneratorService resourceContentGeneratorService,
                                    final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.webServerPersistenceService = webServerPersistenceService;
        this.successor = successor;
        this.resourceContentGeneratorService = resourceContentGeneratorService;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getWebServerResource(resourceIdentifier.resourceName, resourceIdentifier.webServerName);
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
            final WebServer webServer = webServerPersistenceService.findWebServerByName(resourceIdentifier.webServerName);
            final UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                    metaData.getTemplateName(), metaData.getJsonData(), templateContent) {
                @Override
                public String getConfFileName() {
                    return metaData.getDeployFileName();
                }
            };
            final String generatedDeployPath = resourceContentGeneratorService.generateContent(metaData.getDeployFileName(), metaData.getDeployPath(), null, webServer, ResourceGeneratorType.METADATA);
            createResourceResponseWrapper = new CreateResourceResponseWrapper(webServerPersistenceService
                    .uploadWebServerConfigTemplate(uploadWebArchiveRequest, generatedDeployPath + "/" + metaData.getDeployFileName(), null));
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
               StringUtils.isNotEmpty(resourceIdentifier.webServerName) &&
               !"*".equalsIgnoreCase(resourceIdentifier.webServerName) &&
               StringUtils.isEmpty(resourceIdentifier.webAppName) &&
               StringUtils.isEmpty(resourceIdentifier.jvmName);
    }

    @Override
    public String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData) {
        if (canHandle(resourceIdentifier)) {
            return webServerPersistenceService.updateResourceMetaData(resourceIdentifier.webServerName, resourceName, metaData);
        } else {
            return successor.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
        }
    }

    @Override
    public Object getSelectedValue(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)){
            return webServerPersistenceService.findWebServerByName(resourceIdentifier.webServerName);
        } else {
            return successor.getSelectedValue(resourceIdentifier);
        }
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)){
            return webServerPersistenceService.getResourceTemplateNames(resourceIdentifier.webServerName);
        } else {
            return successor.getResourceNames(resourceIdentifier);
        }
    }
}
