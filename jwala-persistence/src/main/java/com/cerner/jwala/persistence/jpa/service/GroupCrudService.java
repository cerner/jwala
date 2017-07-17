package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;

import java.util.List;

public interface GroupCrudService extends CrudService<JpaGroup> {

    JpaGroup createGroup(CreateGroupRequest createGroupRequest);

    void updateGroup(UpdateGroupRequest updateGroupRequest);

    JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    JpaGroup getGroup(final String name) throws NotFoundException;

    List<JpaGroup> getGroups();

    List<JpaGroup> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
    
    Long getGroupId(String name);

    /**
     * Link a web server to to a collection of groups.
     * @param webServer the web server to link.
     */
    void linkWebServer(WebServer webServer);

    /**
     * Link a newly created web server to a collection of groups.
     * @param id id of the newly created web server to link.
     * @param webServer wrapper for the web server details.
     */
    void linkWebServer(Identifier<WebServer> id, WebServer webServer);

    void uploadGroupJvmTemplate(UploadJvmTemplateRequest uploadRequest, JpaGroup group);

    void uploadGroupWebServerTemplate(UploadWebServerTemplateRequest uploadRequest, JpaGroup group);

    List getGroupJvmsResourceTemplateNames(String groupName);

    List getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName);

    void updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    void updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);

    ConfigTemplate populateGroupAppTemplate(String groupName, String appName, String templateFileName, String metaData, String templateContent);

    List<String> getGroupAppsResourceTemplateNames(String groupName);

    List<String> getGroupAppsResourceTemplateNames(String groupName, String appName);

    String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName);

    String getGroupAppResourceTemplateMetaData(String groupName, String fileName);

    void updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content);

    String getGroupJvmResourceTemplateMetaData(String groupName, String fileName);

    String getGroupWebServerResourceTemplateMetaData(String groupName, String resourceTemplateName);

    /**
     * This method checks if the group jvm template contains a file name/template name.
     * @param groupName name of the group in which the file needs to be searched in
     * @param fileName name of the file to be searched
     * @return true if the file exists else false
     */
    boolean checkGroupJvmResourceFileName(String groupName, String fileName);

    /**
     * This method checks if the group webserver template contains a file name/template name.
     * @param groupName name of the group in which the file needs to be searched in
     * @param fileName name of the file to be searched
     * @return true if the file exists else false
     */
    boolean checkGroupAppResourceFileName(String groupName, String fileName);

    /**
     * This method checks if the group webserver template contains a file name/template name.
     * @param groupName name of the group in which the file needs to be searched in
     * @param fileName name of the file to be searched
     * @return true if the file exists else false
     */
    boolean checkGroupWebServerResourceFileName(String groupName, String fileName);

    void updateGroupWebServerResourceMetaData(String groupName, String resourceName, String metaData);

    void updateGroupJvmResourceMetaData(String groupName, String resourceName, String metaData);

    void updateGroupAppResourceMetaData(String groupName, String webAppName, String resourceName, String metaData);

    String getGroupAppResourceTemplateMetaDataWithAppname(String groupName, String templateName, String appName);
}
