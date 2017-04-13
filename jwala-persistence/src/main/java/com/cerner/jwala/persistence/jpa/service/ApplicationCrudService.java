package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;

import java.util.List;

public interface ApplicationCrudService extends CrudService<JpaApplication> {

    JpaApplication createApplication(CreateApplicationRequest createApplicationRequest, JpaGroup jpaGroup);

    JpaApplication updateApplication(UpdateApplicationRequest updateApplicationRequest, JpaApplication jpaApp, JpaGroup jpaGroup);

    void removeApplication(final Identifier<Application> anAppId);

    JpaApplication getExisting(final Identifier<Application> anAppId);

    List<String> getResourceTemplateNames(String appName, String jvmName);

    String getResourceTemplate(final String appName, final String resourceTemplateName, JpaJvm jvm);

    String getResourceTemplate(String appName, String resourceTemplateName, String jvmName, String groupName);

    void updateResourceTemplate(final String appName, final String resourceTemplateName, String template, JpaJvm jvm);

    void createConfigTemplate(JpaApplication app, String resourceTemplateName, String metaData, String resourceTemplateContent, JpaJvm jvm);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, JpaJvm jpaJvm);

    Application getApplication(final Identifier<Application> aApplicationId) throws NotFoundException;

    List<Application> getApplications();

    List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplicationsBelongingTo(String groupName);

    List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId);

    Application findApplication(String appName, String groupName, String jvmName);

    Application getApplication(String name);

    String getMetaData(String appName, String jvmName, String groupName, String resourceTemplateName);

    /**
     *
     * @param groupName
     * @param appName
     * @return
     */
    Application findApplication(String groupName, String appName);

    /**
     *
     * @param groupName
     * @param appName
     * @param fileName
     * @return
     */
    boolean checkAppResourceFileName(String groupName, String appName, String fileName);

    void updateResourceMetaData(String webAppName, String resourceName, String metaData, JpaJvm jpaJvm);
}
