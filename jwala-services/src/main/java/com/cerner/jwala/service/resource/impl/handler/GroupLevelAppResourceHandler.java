package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.exception.GroupLevelAppResourceHandlerException;
import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Handler for a group level application resource identified by a "resource identifier" {@link ResourceIdentifier}
 * <p>
 * Created by Jedd Cuison on 7/21/2016
 */
public class GroupLevelAppResourceHandler extends ResourceHandler {

    private static final String WAR_FILE_EXTENSION = ".war";
    private static final String MSG_CAN_ONLY_HAVE_ONE_WAR = "A web application can only have 1 war file. To change it, delete the war file first before uploading a new one.";

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupLevelAppResourceHandler.class);

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
        ResourceTemplateMetaData metaDataCopy = metaData;
        CreateResourceResponseWrapper createResourceResponseWrapper = null;
        if (canHandle(resourceIdentifier)) {
            final String groupName = resourceIdentifier.groupName;
            final Group group = groupPersistenceService.getGroup(groupName);
            final ConfigTemplate createdConfigTemplate;

            if (metaDataCopy.getContentType().equals(MediaType.APPLICATION_ZIP) &&
                    templateContent.toLowerCase(Locale.US).endsWith(WAR_FILE_EXTENSION)) {
                final Application app = applicationPersistenceService.getApplication(resourceIdentifier.webAppName);
                if (StringUtils.isEmpty(app.getWarName())) {
                    applicationPersistenceService.updateWarInfo(resourceIdentifier.webAppName, metaDataCopy.getDeployFileName(), templateContent);
                    metaDataCopy = updateApplicationWarMetaData(resourceIdentifier, metaDataCopy, app);
                } else {
                    throw new ResourceServiceException(MSG_CAN_ONLY_HAVE_ONE_WAR);
                }
            }

            createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(groupName, resourceIdentifier.webAppName,
                    metaDataCopy.getDeployFileName(), metaDataCopy.getJsonData(), templateContent);

            createJvmTemplateFromAppResource(resourceIdentifier, templateContent, metaDataCopy, groupName, group);

            createResourceResponseWrapper = new CreateResourceResponseWrapper(createdConfigTemplate);
        } else if (successor != null) {
            createResourceResponseWrapper = successor.createResource(resourceIdentifier, metaDataCopy, templateContent);
        }
        return createResourceResponseWrapper;
    }

    private ResourceTemplateMetaData updateApplicationWarMetaData(ResourceIdentifier resourceIdentifier, ResourceTemplateMetaData metaDataCopy, Application app) {
        boolean isUnpackWar = app.isUnpackWar();
        metaDataCopy = new ResourceTemplateMetaData(
                metaDataCopy.getTemplateName(),
                metaDataCopy.getContentType(),
                metaDataCopy.getDeployFileName(),
                metaDataCopy.getDeployPath(),
                metaDataCopy.getEntity(),
                isUnpackWar,
                metaDataCopy.isOverwrite(),
                metaDataCopy.isHotDeploy());

        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metaDataCopy);
            metaDataCopy.setJsonData(jsonData);
        } catch (IOException e) {
            final String errMsg = MessageFormat.format("Failed to update the war meta data for app {0} on {1} resource creation", resourceIdentifier.webAppName, metaDataCopy.getDeployFileName(), e);
            LOGGER.error(errMsg);
            throw new GroupLevelAppResourceHandlerException(errMsg);
        }

        return metaDataCopy;
    }

    private void createJvmTemplateFromAppResource(ResourceIdentifier resourceIdentifier, String templateContent, ResourceTemplateMetaData metaDataCopy, String groupName, Group group) {
        // Can't we just find the application using the group name and target app name instead of getting all the applications
        // then iterating it to compare with the target app name ???
        // If we can do that then TODO: Refactor this to return only one application and remove the iteration!
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(groupName);

        for (final Application application : applications) {
            if (metaDataCopy.getEntity().getDeployToJvms() && application.getName().equals(resourceIdentifier.webAppName)) {
                for (final Jvm jvm : group.getJvms()) {
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaDataCopy.getTemplateName(),
                            metaDataCopy.getDeployFileName(), jvm.getJvmName(), metaDataCopy.getJsonData(), templateContent
                    );
                    JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
                    applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
                }
            }
        }
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
    public String updateResourceMetaData(final ResourceIdentifier resourceIdentifier, final String resourceName, final String metaData) {
        if (canHandle(resourceIdentifier)) {
            final String updatedMetaData = groupPersistenceService.updateGroupAppResourceMetaData(resourceIdentifier.groupName, resourceIdentifier.webAppName, resourceName, metaData);
            updateApplicationUnpackWar(resourceIdentifier.webAppName, resourceName, metaData);
            updateMetaDataForChildJVMResources(resourceIdentifier, resourceName, metaData);
            return updatedMetaData;
        } else {
            return successor.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
        }
    }

    private void updateApplicationUnpackWar(final String webAppName, final String resourceName, final String jsonMetaData) {
        Application application = applicationPersistenceService.getApplication(webAppName);
        ResourceTemplateMetaData warMetaData = null;
        final String appName = application.getName();
        try {
            warMetaData = new ObjectMapper().readValue(jsonMetaData, ResourceTemplateMetaData.class);
            applicationPersistenceService.updateApplication(new UpdateApplicationRequest(
                    application.getId(),
                    application.getGroup().getId(),
                    application.getWebAppContext(),
                    appName,
                    application.isSecure(),
                    application.isLoadBalanceAcrossServers(),
                    warMetaData.isUnpack()
            ));
        } catch (IOException e) {
            final String errorMsg = MessageFormat.format("Failed to parse meta data for war {0} in application {1} during an update of the meta data", resourceName, appName);
            LOGGER.error(errorMsg,e);
            throw new GroupLevelAppResourceHandlerException(errorMsg);
        }
    }

    private void updateMetaDataForChildJVMResources(final ResourceIdentifier resourceIdentifier, final String resourceName, final String metaData) {
        Set<Jvm> jvmSet = groupPersistenceService.getGroup(resourceIdentifier.groupName).getJvms();
        for (Jvm jvm : jvmSet) {
            List<String> resourceNames = applicationPersistenceService.getResourceTemplateNames(resourceIdentifier.webAppName, jvm.getJvmName());
            if (resourceNames.contains(resourceName)) {
                applicationPersistenceService.updateResourceMetaData(resourceIdentifier.webAppName, resourceName, metaData, jvm.getJvmName(), resourceIdentifier.groupName);
            }
        }
    }

    @Override
    public Object getSelectedValue(ResourceIdentifier resourceIdentifier) {
        if (canHandle(resourceIdentifier)) {
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
