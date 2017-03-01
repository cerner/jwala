package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handler for a group level JVM resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by Jedd Cuison on 7/21/2016
 */
public class GroupLevelJvmResourceHandler extends ResourceHandler {

    private final GroupPersistenceService groupPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;

    public GroupLevelJvmResourceHandler(final ResourceDao resourceDao, final GroupPersistenceService groupPersistenceService,
                                        final JvmPersistenceService jvmPersistenceService, final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.groupPersistenceService = groupPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getGroupLevelJvmResource(resourceIdentifier.resourceName, resourceIdentifier.groupName);
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
            final Set<Jvm> jvms = groupPersistenceService.getGroup(resourceIdentifier.groupName).getJvms();
            ConfigTemplate createdJpaJvmConfigTemplate = null;

            for (final Jvm jvm : jvms) {
                UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvm, metaData.getTemplateName(),
                        templateContent, metaData.getJsonData());
                uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());

                // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
                // configuration template.
                createdJpaJvmConfigTemplate = jvmPersistenceService.uploadJvmConfigTemplate(uploadJvmTemplateRequest);
            }
            final List<UploadJvmTemplateRequest> uploadJvmTemplateRequestList = new ArrayList<>();
            UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(null, metaData.getTemplateName(),
                    templateContent, metaData.getJsonData());
            uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
            uploadJvmTemplateRequestList.add(uploadJvmTemplateRequest);
            groupPersistenceService.populateGroupJvmTemplates(resourceIdentifier.groupName, uploadJvmTemplateRequestList);
            createResourceResponseWrapper = new CreateResourceResponseWrapper(createdJpaJvmConfigTemplate);
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
                "*".equalsIgnoreCase(resourceIdentifier.jvmName) &&
               StringUtils.isEmpty(resourceIdentifier.webServerName) &&
               StringUtils.isEmpty(resourceIdentifier.webAppName);
    }

    @Override
    public String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData) {
        if (canHandle(resourceIdentifier)) {
            final String updatedMetaData = groupPersistenceService.updateGroupJvmResourceMetaData(resourceIdentifier.groupName, resourceName, metaData);
            Set<Jvm> jvmSet = groupPersistenceService.getGroup(resourceIdentifier.groupName).getJvms();
            for (Jvm jvm : jvmSet) {
                jvmPersistenceService.updateResourceMetaData(jvm.getJvmName(), resourceName, metaData);
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
