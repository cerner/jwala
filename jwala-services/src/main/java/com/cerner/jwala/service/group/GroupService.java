package com.cerner.jwala.service.group;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;

import java.util.List;

public interface GroupService {

    Group createGroup(final CreateGroupRequest aCreateGroupCommand, final User aCreatingUser);

    Group getGroup(final Identifier<Group> aGroupId);

    Group getGroupWithWebServers(final Identifier<Group> aGroupId);

    Group getGroup(final String name);

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aGroupNameFragment);

    Group updateGroup(final UpdateGroupRequest anUpdateGroupCommand, final User anUpdatingUser);

    void removeGroup(final Identifier<Group> aGroupId);

    void removeGroup(String name);

    Group addJvmToGroup(final AddJvmToGroupRequest addJvmToGroupRequest, final User anAddingUser);

    Group addJvmsToGroup(final AddJvmsToGroupRequest addJvmsToGroupRequest, final User anAddingUser);

    Group removeJvmFromGroup(final RemoveJvmFromGroupRequest removeJvmFromGroupRequest, final User aRemovingUser);

    /**
     * Gets the connection details of JVMs under a group specified by id.
     *
     * @param id the group id
     * @return JVMs that are members of more than one group.
     */
    List<Jvm> getOtherGroupingDetailsOfJvms(final Identifier<Group> id);

    /**
     * Gets the connection details of Web Servers under a group specified by id.
     *
     * @param id the group id
     * @return Web Servers that are members of more than one group.
     */
    List<WebServer> getOtherGroupingDetailsOfWebServers(final Identifier<Group> id);

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

    List<String> getGroupJvmsResourceTemplateNames(String groupName);

    List<String> getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName, ResourceGroup resourceGroup, boolean tokensReplaced);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced, ResourceGroup resourceGroup);

    @Deprecated
    String previewGroupWebServerResourceTemplate(String fileName, String groupName, String template, ResourceGroup resourceGroup);

    String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);

    List<String> getGroupAppsResourceTemplateNames(String groupName);

    List<String> getGroupAppsResourceTemplateNames(String groupName, String appName);

    String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, boolean tokensReplaced, ResourceGroup resourceGroup);

    String getGroupAppResourceTemplateMetaData(String groupName, String fileName, String appName);

    String getGroupJvmResourceTemplateMetaData(String groupName, String fileName);

    String getGroupWebServerResourceTemplateMetaData(String groupName, String fileName);

    String updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content);

    String previewGroupAppResourceTemplate(String groupName, String resourceTemplateName, String template, ResourceGroup resourceGroup, String appName);

    CommandOutput deployGroupAppTemplate(String groupName, String fileName, Application application, Jvm jvm);

    /**
     * This method deploys a group application config template to a particular host.
     *
     * @param groupName     name of the group in which the application exists
     * @param fileName      name of the file that needs to be deployed
     * @param application   this is the application object
     * @param hostName      this is the host name, where we want to deploy the config file
     * @return returns a commandoutput object
     */
    CommandOutput deployGroupAppTemplate(String groupName, String fileName, Application application, String hostName);

    /**
     * Get hosts of a group.
     *
     * @param groupName the group's name
     * @return all the host names of a group
     */
    List<String> getHosts(String groupName);

    /**
     * Return all the unique host names configured for all the groups
     *
     * @return a list of all the unique host names
     */
    List<String> getAllHosts();

    Group generateAndDeployGroupJvmFile(final String groupName, final String fileName, final User user);

}