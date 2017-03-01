package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.app.*;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;

import java.util.List;

public interface ApplicationPersistenceService {

    Application createApplication(CreateApplicationRequest createApplicationRequest);

    Application updateApplication(final UpdateApplicationRequest updateApplicationRequest);

    void removeApplication(final Identifier<Application> anAppToRemove);

    List<String> getResourceTemplateNames(String appName, String jvmName);

    String getResourceTemplate(final String appName, final String resourceTemplateName, final String jvmName, final String groupName);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, JpaJvm jpaJvm);

    List<Application> getApplications();

    Application getApplication(Identifier<Application> aApplicationId);

    Application getApplication(String name);

    List<Application> findApplicationsBelongingTo(Identifier<Group> groupId);

    List<Application> findApplicationsBelongingTo(String groupName);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> jvmId);

    Application findApplication(String appName, String groupName, String jvmName);

    void createApplicationConfigTemplateForJvm(String jvmName, Application app, Identifier<Group> groupId, String metaData,
                                               String resourceTypeTemplate);

    String getMetaData(String appName, String jvmName, String groupName, String templateName);

    /**
     * Check if the application contains the resource name.
     * @param groupName the name of the group, which contains the webapp
     * @param appName the name of the webapp, which contains the resource file
     * @param fileName the filename of the resource
     * @return true if the file already exists, else returns false
     */
    boolean checkAppResourceFileName(String groupName, String appName, String fileName);

    /**
     * Update the application's war name and war path.
     * @param appName the application name
     * @param warName the war name
     * @param warPath the war path
     * @return number of rows updated
     */
    Application updateWarInfo(String appName, String warName, String warPath);

    /**
     * Sets the application's war name and path to null.
     * @param appName the application name
     * @return the number of rows updated
     */
    Application deleteWarInfo(String appName);

    String updateResourceMetaData(String webAppName, String resourceName, String metaData, String jvmName, String groupName);
}
