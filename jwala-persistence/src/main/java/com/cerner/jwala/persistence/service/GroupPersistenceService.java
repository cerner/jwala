package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.group.RemoveJvmFromGroupRequest;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;

import java.util.List;
import java.util.Map;

public interface GroupPersistenceService {

    Group updateGroup(UpdateGroupRequest updateGroupRequest) throws NotFoundException;

    Group createGroup(CreateGroupRequest createGroupRequest);

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroupWithWebServers(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroupWithWebServers(String groupName) throws NotFoundException;

    Group getGroup(final String name) throws NotFoundException;

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    void removeGroup(String name) throws NotFoundException;

    Group addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest) throws NotFoundException;

    Group removeJvmFromGroup(RemoveJvmFromGroupRequest removeJvmFromGroupRequest) throws NotFoundException;

    Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException;

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

    Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands);

    Group populateGroupWebServerTemplates(String groupName, Map<String, UploadWebServerTemplateRequest> uploadWSTemplateCommands);

    List<String> getGroupJvmsResourceTemplateNames(String groupName);

    List<String> getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName);

    String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);

    ConfigTemplate populateGroupAppTemplate(String groupName, String appName, String templateFileName, String metaData, String templateContent);

    List<String> getGroupAppsResourceTemplateNames(String groupName);

    List<String> getGroupAppsResourceTemplateNames(String groupName, String appName);

    String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName);

    String getGroupAppResourceTemplateMetaData(String groupName, String fileName);

    String updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content);

    String getGroupJvmResourceTemplateMetaData(String groupName, String fileName);

    String getGroupWebServerResourceTemplateMetaData(String groupName, String resourceTemplateName);

    /**
     *
     * @param groupName
     * @param fileName
     * @return
     */
    boolean checkGroupJvmResourceFileName(String groupName, String fileName);

    /**
     *
     * @param groupName
     * @param fileName
     * @return
     */
    boolean checkGroupAppResourceFileName(String groupName, String fileName);

    /**
     *
     * @param groupName
     * @param fileName
     * @return
     */
    boolean checkGroupWebServerResourceFileName(String groupName, String fileName);

    /**
     * Get hosts of a group.
     * @param groupName the group's name
     * @return all the host names of a group
     */
    List<String> getHosts(String groupName);

    String updateGroupWebServerResourceMetaData(String groupName, String resourceName, String metaData);

    String updateGroupJvmResourceMetaData(String groupName, String resourceName, String metaData);

    String updateGroupAppResourceMetaData(String groupName, String webAppName, String resourceName, String metaData);

    List<JpaGroup> findGroups(List<Long> idList);

    String getGroupAppResourceTemplateMetaDataWithAppName(String groupName, String templateName, String appName);
}
