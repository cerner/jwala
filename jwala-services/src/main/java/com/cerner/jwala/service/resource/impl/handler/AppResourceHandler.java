package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Handler for an application resource identified by a "resource identifier" {@link ResourceIdentifier}
 * <p>
 * Created by Jedd Cuison on 7/21/2016
 */
public class AppResourceHandler extends ResourceHandler {

    private final JvmPersistenceService jvmPersistenceService;
    private final ApplicationPersistenceService applicationPersistenceService;

    public AppResourceHandler(final ResourceDao resourceDao, final JvmPersistenceService jvmPersistenceService,
                              final ApplicationPersistenceService applicationPersistenceService,
                              final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getAppResource(resourceIdentifier.resourceName, resourceIdentifier.webAppName,
                    resourceIdentifier.jvmName);
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
            final Application application = applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
            final UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                    metaData.getDeployFileName(), resourceIdentifier.jvmName, metaData.getJsonData(), templateContent);
            final JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvmPersistenceService.findJvmByExactName(resourceIdentifier.jvmName).getId(), false);
            createResourceResponseWrapper = new CreateResourceResponseWrapper(applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm));
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
                StringUtils.isNotEmpty(resourceIdentifier.webAppName) &&
                StringUtils.isNotEmpty(resourceIdentifier.jvmName) &&
                StringUtils.isEmpty(resourceIdentifier.webServerName);
    }

    @Override
    public String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData) {
        if (canHandle(resourceIdentifier)) {
            return applicationPersistenceService.updateResourceMetaData(resourceIdentifier.webAppName, resourceName, metaData, resourceIdentifier.jvmName, resourceIdentifier.groupName);
        } else {
            return successor.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
        }
    }

    @Override
    public Object getSelectedValue(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)) {
            Application application = applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
            application.setParentJvm(jvmPersistenceService.findJvmByExactName(resourceIdentifier.jvmName));
            return application;
        } else {
            return successor.getSelectedValue(resourceIdentifier);
        }
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)) {
            return applicationPersistenceService.getResourceTemplateNames(resourceIdentifier.webAppName, resourceIdentifier.jvmName);
        } else {
            return successor.getResourceNames(resourceIdentifier);
        }
    }
}
