package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Handler for a group level application resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by Jedd Cuison on 7/21/2016
 */
public class GroupLevelAppResourceHandler extends ResourceHandler {

    private static final String WAR_FILE_EXTENSION = ".war";
    private static final String MSG_CAN_ONLY_HAVE_ONE_WAR = "A web application can only have 1 war file. To change it, delete the war file first before uploading a new one.";

    private final GroupPersistenceService groupPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;
    private final ApplicationPersistenceService applicationPersistenceService;

    public GroupLevelAppResourceHandler(final ResourceDao resourceDao,
                                        final GroupPersistenceService groupPersistenceService,
                                        final JvmPersistenceService jvmPersistenceService,
                                        final ApplicationPersistenceService applicationPersistenceService,
                                        final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.groupPersistenceService = groupPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getGroupLevelAppResource(resourceIdentifier.resourceName, resourceIdentifier.webAppName,
                    resourceIdentifier.groupName);
        } else if (successor != null) {
            configTemplate = successor.fetchResource(resourceIdentifier);
        }
        return configTemplate;
    }

    @Override
    public CreateResourceResponseWrapper
    createResource(final ResourceIdentifier resourceIdentifier,
                                                        final ResourceTemplateMetaData metaData,
                                                        final String templateContent) {
        CreateResourceResponseWrapper createResourceResponseWrapper = null;
        if (canHandle(resourceIdentifier)) {
            final String groupName = resourceIdentifier.groupName;
            final Group group = groupPersistenceService.getGroup(groupName);
            final ConfigTemplate createdConfigTemplate;

            if (metaData.getContentType().equals(MediaType.APPLICATION_ZIP) &&
                    templateContent.toLowerCase(Locale.US).endsWith(WAR_FILE_EXTENSION)) {
                final Application app = applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
                if (StringUtils.isEmpty(app.getWarName())) {
                    applicationPersistenceService.updateWarInfo(resourceIdentifier.webAppName, metaData.getTemplateName(), templateContent);
                } else {
                    throw new ResourceServiceException(MSG_CAN_ONLY_HAVE_ONE_WAR);
                }
            }

            createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(groupName, resourceIdentifier.webAppName,
                    metaData.getDeployFileName(), metaData.getJsonData(), templateContent);

            // Can't we just get the application using the group name and target app name instead of getting all the applications
            // then iterating it to compare with the target app name ???
            // If we can do that then TODO: Refactor this to return only one application and remove the iteration!
            final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(groupName);

            for (final Application application : applications) {
                if (metaData.getEntity().getDeployToJvms() && application.getName().equals(resourceIdentifier.webAppName)) {
                    for (final Jvm jvm : group.getJvms()) {
                        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                                metaData.getDeployFileName(), jvm.getJvmName(), metaData.getJsonData(), templateContent
                        );
                        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
                        applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
                    }
                }
            }

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
               StringUtils.isNotEmpty(resourceIdentifier.webAppName) &&
               StringUtils.isNotEmpty(resourceIdentifier.groupName) &&
               StringUtils.isEmpty(resourceIdentifier.webServerName) &&
               StringUtils.isEmpty(resourceIdentifier.jvmName);
    }

    @Override
    public String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData) {
        if (canHandle(resourceIdentifier)) {
            final String updatedMetaData = groupPersistenceService.updateGroupAppResourceMetaData(resourceIdentifier.groupName, resourceIdentifier.webAppName, resourceName, metaData);
            Set<Jvm> jvmSet = groupPersistenceService.getGroup(resourceIdentifier.groupName).getJvms();
            for (Jvm jvm : jvmSet) {
                List<String> resourceNames = applicationPersistenceService.getResourceTemplateNames(resourceIdentifier.webAppName, jvm.getJvmName());
                if (resourceNames.contains(resourceName)){
                    applicationPersistenceService.updateResourceMetaData(resourceIdentifier.webAppName, resourceName, metaData, jvm.getJvmName(), resourceIdentifier.groupName);
                }
            }
            return updatedMetaData;
        } else {
            return successor.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
        }
    }

    @Override
    public Object getSelectedValue(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)){
            return applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
        } else {
            return successor.getSelectedValue(resourceIdentifier);
        }
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)) {
            return resourceDao.getGroupLevelAppResourceNames(resourceIdentifier.groupName, resourceIdentifier.webAppName);
        } else {
            return successor.getResourceNames(resourceIdentifier);
        }
    }
}
