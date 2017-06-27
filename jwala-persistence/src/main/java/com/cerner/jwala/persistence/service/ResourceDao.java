package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.*;

import java.util.List;

/**
 * DAO Contract for resource related methods.
 *
 * Created by Jedd Cuison on 6/3/2016.
 */
public interface ResourceDao {

    /**
     * Delete a web server resource.
     * @param templateName the template name
     * @param webServerName the web server name
     * @return the number of resources deleted
     */
    int deleteWebServerResource(String templateName, String webServerName);

    /**
     * Delete a group level web server resource.
     * @param templateName the template name
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelWebServerResource(String templateName, String groupName);

    /**
     * Delete a JVM resource.
     * @param templateName the template name
     * @param jvmName the JVM name
     * @return the number of resources deleted
     */
    int deleteJvmResource(String templateName, String jvmName);

    /**
     * Delete a group level JVM resource.
     * @param templateName the template name
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelJvmResource(String templateName, String groupName);

    /**
     * Delete an application resource
     * @param templateName the template name
     * @param appName the application name
     * @param jvmName the jvm name
     * @return the number of resources deleted
     */
    int deleteAppResource(String templateName, String appName, String jvmName);

    /**
     * Delete a group level application resource.
     * @param appName the application name
     * @param groupName the group name
     * @param templateName the template name
     * @return the number of resources deleted
     */
    int deleteGroupLevelAppResource(String appName, String groupName, String templateName);

    /**
     * Delete web server resources.
     * @param templateNameList list of template names
     * @param webServerName the web server name
     * @return the number of resources deleted
     */
    int deleteWebServerResources(List<String> templateNameList, String webServerName);

    /**
     * Delete group level web server resources.
     * @param templateNameList the template name list
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelWebServerResources(List<String> templateNameList, String groupName);

    /**
     * Delete JVM resources.
     * @param templateNameList the template name list
     * @param jvmName the JVM name
     * @return the number of resources deleted
     */
    int deleteJvmResources(List<String> templateNameList, String jvmName);

    /**
     * Delete group level JVM resources.
     * @param templateNameList the template name list
     * @param groupName the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelJvmResources(List<String> templateNameList, String groupName);

    /**
     * Delete application resources.
     * @param templateNameList the template name list
     * @param appName the application name
     * @param jvmName the jvm name
     * @return the number of resources deleted
     */
    int deleteAppResources(List<String> templateNameList, String appName, String jvmName);

    /**
     * Delete group level application resources.
     * @param appName the application name
     * @param groupName the group name
     * @param templateNameList the template name list
     * @return the number of resources deleted
     */
    int deleteGroupLevelAppResources(String appName, String groupName, List<String> templateNameList);

    /**
     * Delete the external properties resource
     * @return the number of records deleted (should be 0 or 1)
     */
    int deleteExternalProperties();

    /**
     * Get web server resource
     * @param resourceName the resource name
     * @param webServerName the web server name
     * @return {@link JpaWebServerConfigTemplate}
     */
    JpaWebServerConfigTemplate getWebServerResource(String resourceName, String webServerName);

    /**
     * Get a JVM resource
     * @param resourceName the resource name
     * @param jvmName the JVM name
     * @return {@link JpaJvmConfigTemplate}
     */
    JpaJvmConfigTemplate getJvmResource(String resourceName, String jvmName);

    /**
     * Get an application resource
     * @param resourceName the resource name
     * @param appName the application name
     * @return {@link JpaApplicationConfigTemplate}
     */
    JpaApplicationConfigTemplate getAppResource(String resourceName, String appName, String jvmName);

    /**
     * Get a group level web server resource
     * @param resourceName the resource name
     * @param groupName the group name
     * @return {@link JpaGroupWebServerConfigTemplate}
     */
    JpaGroupWebServerConfigTemplate getGroupLevelWebServerResource(String resourceName, String groupName);

    /**
     * Get a group level JVM resource
     * @param resourceName resource name
     * @param groupName group name
     * @return {@link JpaGroupJvmConfigTemplate}
     */
    JpaGroupJvmConfigTemplate getGroupLevelJvmResource(String resourceName, String groupName);

    /**
     * Get a group level web application resource
     * @param resourceName resource name
     * @param appName the application name
     *@param groupName group name  @return {@link JpaGroupAppConfigTemplate}
     */
    JpaGroupAppConfigTemplate getGroupLevelAppResource(String resourceName, String appName, String groupName);

    List<String> getGroupLevelAppResourceNames(String groupName, String webAppName);

    /**
     * Get the content of the external properties resource
     * @param resourceName the name of the file that was uploaded with the external properties
     * @return the external properties template
     */
    JpaResourceConfigTemplate getExternalPropertiesResource(String resourceName);

    /**
     * Return the list of templates for this entity
     * @param identifier the entity selected
     * @param entityType the type of the entity (JVM, Web Server, Application, Group level JVMs, External Properties, etc.)
     * @return a list of the template names
     */
    List<String> getResourceNames(ResourceIdentifier identifier, EntityType entityType);

    /**
     * Create a new resource template
     * @param entityId the ID of the JVM, Web Server, Application, etc.
     * @param groupId the group ID of the entity
     * @param appId the application ID of the entity
     * @param entityType the enumerated type of the entity
     * @param resourceFileName the name of the resource
     * @param templateContent the content of the template
     * @param metaData the meta data of the resource
     * @return the saved resource
     */
    JpaResourceConfigTemplate createResource(Long entityId, Long groupId, Long appId, EntityType entityType, String resourceFileName, String templateContent, String metaData);

    /**
     * Update the content of the resource template
     * @param resourceIdentifier the ID information of the resource
     * @param entityType the type of the entity (JVM, Web Server, Application, Group level JVMs, External Properties, etc.)
     * @param templateContent the updated content of the template
     */
    void updateResource(ResourceIdentifier resourceIdentifier, EntityType entityType, String templateContent);

        void updateResourceGroup(JpaApplication aplication, JpaGroup jpaGroup);
}

