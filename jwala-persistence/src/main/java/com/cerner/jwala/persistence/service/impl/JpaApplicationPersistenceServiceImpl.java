package com.cerner.jwala.persistence.service.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.builder.JpaAppBuilder;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class JpaApplicationPersistenceServiceImpl implements ApplicationPersistenceService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationPersistenceService.class);

    private final ApplicationCrudService applicationCrudService;
    private final GroupCrudService groupCrudService;

    @PersistenceContext(unitName = "jwala-unit")
    // Note: We will deprecate the CRUD service soon and make this class a DAO so going forward, we use the entity manager in this class for new methods.
    private EntityManager em;

    public JpaApplicationPersistenceServiceImpl(final ApplicationCrudService theAppCrudService,
                                                final GroupCrudService theGroupCrudService) {
        applicationCrudService = theAppCrudService;
        groupCrudService = theGroupCrudService;
    }

    @Override
    public Application createApplication(CreateApplicationRequest createApplicationRequest) {
        JpaGroup jpaGroup = groupCrudService.getGroup(createApplicationRequest.getGroupId());
        final JpaApplication jpaApp = applicationCrudService.createApplication(createApplicationRequest, jpaGroup);

        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public Application updateApplication(UpdateApplicationRequest updateApplicationRequest) {
        final JpaApplication jpaOriginal = applicationCrudService.getExisting(updateApplicationRequest.getId());
        final JpaGroup jpaGroup = groupCrudService.getGroup(updateApplicationRequest.getNewGroupId());
        final JpaApplication jpaApp = applicationCrudService.updateApplication(updateApplicationRequest, jpaOriginal, jpaGroup);
        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public void removeApplication(Identifier<Application> anAppId) {
        applicationCrudService.removeApplication(anAppId);
    }

    @Override
    public List<String> getResourceTemplateNames(final String appName, final String jvmName) {
        return applicationCrudService.getResourceTemplateNames(appName, jvmName);
    }

    @Override
    public String getResourceTemplate(final String appName, final String resourceTemplateName, final String jvmName, final String groupName) {
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName, jvmName, groupName);
    }

    @Override
    public String getMetaData(String appName, String jvmName, String groupName, String resourceTemplateName) {
        return applicationCrudService.getMetaData(appName, jvmName, groupName, resourceTemplateName);
    }

    @Override
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName) {
        final JpaJvm jpaJvm = getJpaJvm(jvmName, groupName);
        applicationCrudService.updateResourceTemplate(appName, resourceTemplateName, template, jpaJvm);
        return applicationCrudService.getResourceTemplate(appName, resourceTemplateName, jpaJvm);
    }

    @Override
    public String updateResourceMetaData(String webAppName, String resourceName, String metaData, String jvmName, String groupName) {
        final JpaJvm jpaJvm = getJpaJvm(jvmName, groupName);
        applicationCrudService.updateResourceMetaData(webAppName, resourceName, metaData, jpaJvm);
        return applicationCrudService.getMetaData(webAppName, jvmName, groupName, resourceName);
    }

    // Why jpaJvm ? The methods updateResourceTemplate and getResourceTemplate
    // of ApplicationCrudService requires JpaJvm. Those 2 methods were created
    // in a time where the use of DTOs or just POJOs is vague. We need to
    // get back to this when the persistence layer is refactored.
    private JpaJvm getJpaJvm(final String jvmName, final String groupName) {
        final Query q = em.createNamedQuery(JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME);
        q.setParameter(JpaJvm.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (JpaJvm) q.getSingleResult();
    }

    @Override
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, JpaJvm jpaJvm) {
        return applicationCrudService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
    }

    @Override
    public Application findApplication(String appName, String groupName, String jvmName) {
        return applicationCrudService.findApplication(appName, groupName, jvmName);
    }

    @Override
    public List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> jvmId) {
        return applicationCrudService.findApplicationsBelongingToJvm(jvmId);
    }

    @Override
    public List<Application> findApplicationsBelongingTo(Identifier<Group> groupId) {
        return applicationCrudService.findApplicationsBelongingTo(groupId);
    }

    @Override
    public List<Application> findApplicationsBelongingTo(final String groupName) {
        return applicationCrudService.findApplicationsBelongingTo(groupName);
    }

    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationCrudService.getApplication(aApplicationId);
    }

    @Override
    public Application getApplication(final String name) {
        return applicationCrudService.getApplication(name);
    }

    @Override
    public List<Application> getApplications() {
        return applicationCrudService.getApplications();
    }

    @Override
    public void createApplicationConfigTemplateForJvm(final String jvmName, final Application app, final Identifier<Group> groupId,
                                                      final String templateMetaData, final String appContextTemplate) {
        final String webAppContext = app.getWebAppContext();
        final int idx = webAppContext.lastIndexOf('/');
        final String resourceName = idx == -1 ? webAppContext : webAppContext.substring(idx + 1);
        LOGGER.info("Using resource name {} and file manager app content {}", resourceName, appContextTemplate != null);
        if (appContextTemplate != null) {
            JpaGroup jpaGroup = groupCrudService.getGroup(groupId);
            if (jpaGroup.getJvms() != null) {
                for (JpaJvm jvm : jpaGroup.getJvms()) {
                    if (jvm.getName().equals(jvmName)) {
                        applicationCrudService.createConfigTemplate(applicationCrudService.getExisting(app.getId()), resourceName + ".xml",
                                templateMetaData, appContextTemplate, jvm);
                        LOGGER.info("Creation of config template {} SUCCEEDED for {}", resourceName, jvm.getName());
                    }
                }
            }
        }
    }

    @Override
    public boolean checkAppResourceFileName(final String groupName, final String appName, final String fileName) {
        return applicationCrudService.checkAppResourceFileName(groupName, appName, fileName);
    }

    @Override
    public Application updateWarInfo(final String appName, final String warName, final String warPath, String tokenizedDeployPath) {
        LOGGER.info("Updating war info for application: {}, war: {}, war path: {}, war deploy path: {}", appName, warName, warPath, tokenizedDeployPath);
        final Query q = em.createNamedQuery(JpaApplication.QUERY_BY_NAME);
        q.setParameter(JpaApplication.QUERY_PARAM_APP_NAME, appName);
        final JpaApplication jpaApplication = (JpaApplication) q.getSingleResult();
        jpaApplication.setWarName(warName);
        jpaApplication.setWarPath(warPath);
        jpaApplication.setWarDeployPath(tokenizedDeployPath);
        return JpaAppBuilder.appFrom(jpaApplication);
    }

    @Override
    public Application deleteWarInfo(final String appName) {
        return updateWarInfo(appName, null, null, null);
    }
}
