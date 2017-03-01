package com.cerner.jwala.service.resource;

import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public interface ResourceService {

    String encryptUsingPlatformBean(String cleartext);

    /**
     * Creates a template file and it's corresponding JSON meta data file.
     * A template file is used when generating the actual resource file what will be deployed with the application.
     *
     * @param metaDataInputStream the template meta data in JSON.
     *                            example:
     *                            {
     *                            "name": "My Context XML",
     *                            "templateName": "my-context.tpl",
     *                            "contentType": "application/xml",
     *                            "configFileName":"mycontext.xml",
     *                            "relativeDir":"/conf",
     *                            "entity": {
     *                            "type": "jvm",
     *                            "group": "HEALTH CHECK 4.0",
     *                            "target": "HEALTH-CHECK-4.0-someHost4900-2"
     *                            }
     *                            }
     * @param templateData        the template data
     * @param targetName
     * @param user
     *
     * NOTE: This is a legacy method!
     */
    CreateResourceResponseWrapper createTemplate(InputStream metaDataInputStream, InputStream templateData, String targetName, User user);

    /**
     * Generates the ResourceGroup class object, which contains all the jvms, webapps, webservers and groups information.
     *
     * @return the ResourceGroup object
     */
    ResourceGroup generateResourceGroup();

    /**
     * Maps data to the template specified by the template parameter.
     *
     * @param template      the template parameter.
     * @param resourceGroup resourcegroup object
     * @param selectedValue the selectedvalue
     * @return the generated resource file string
     */
    <T> String generateResourceFile(final String fileName, final String template, final ResourceGroup resourceGroup, T selectedValue, ResourceGeneratorType resourceGeneratorType);

    /**
     * Get an application's resource names.
     *
     * @param groupName the group where the app belongs to
     * @param appName   the application name
     * @return List of resource names.
     */
    List<String> getApplicationResourceNames(String groupName, String appName);

    /**
     * Gets an application's resource template.
     *
     * @param groupName    the group the application belongs to
     * @param appName      the application name
     * @param templateName the template name
     * @return the template
     */
    String getAppTemplate(String groupName, String appName, String templateName);

    Map<String, String> checkFileExists(String groupName, String jvmName, String webappName, String webserverName, String fileName);

    boolean checkJvmFileExists(String groupName, String jvmName, String fileName);

    /**
     * Delete a web server resource.
     *
     * @param templateName  the template name
     * @param webServerName the web server name
     * @return the number of resources deleted
     */
    int deleteWebServerResource(String templateName, String webServerName);

    /**
     * Delete a group level web server resource.
     *
     * @param templateName the template name
     * @param groupName    the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelWebServerResource(String templateName, String groupName);

    /**
     * Delete a JVM resource.
     *
     * @param templateName the template name
     * @param jvmName      the JVM name
     * @return the number of resources deleted
     */
    int deleteJvmResource(String templateName, String jvmName);

    /**
     * Delete a group level JVM resource.
     *
     * @param templateName the template name
     * @param groupName    the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelJvmResource(String templateName, String groupName);

    /**
     * Delete an application resource
     *
     * @param templateName the template name
     * @param appName      the application name
     * @param jvmName      the jvm name
     * @return the number of resources deleted
     */
    int deleteAppResource(String templateName, String appName, String jvmName);

    /**
     * Delete a group level application resource.
     *
     * @param appName
     * @param templateName the template name
     * @return the number of resources deleted
     */
    @Deprecated
    int deleteGroupLevelAppResource(String appName, String templateName);

    /**
     * Delete web server resources.
     *
     * @param templateNameList list of template names
     * @param webServerName    the web server name
     * @return the number of resources deleted
     */
    int deleteWebServerResources(List<String> templateNameList, String webServerName);

    /**
     * Delete group level web server resources.
     *
     * @param templateNameList the template name list
     * @param groupName        the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelWebServerResources(List<String> templateNameList, String groupName);

    /**
     * Delete JVM resources.
     *
     * @param templateNameList the template name list
     * @param jvmName          the JVM name
     * @return the number of resources deleted
     */
    int deleteJvmResources(List<String> templateNameList, String jvmName);

    /**
     * Delete group level JVM resources.
     *
     * @param templateNameList the template name list
     * @param groupName        the group name
     * @return the number of resources deleted
     */
    int deleteGroupLevelJvmResources(List<String> templateNameList, String groupName);

    /**
     * Delete application resources.
     *
     * @param templateNameList the template name list
     * @param appName          the application name
     * @param jvmName          the jvm name
     * @return the number of resources deleted
     */
    int deleteAppResources(List<String> templateNameList, String appName, String jvmName);

    /**
     * Delete group level application resources.
     *
     * @param appName          the application name
     * @param groupName        the group name
     * @param templateNameList the template name list
     * @return the number of resources deleted
     */
    int deleteGroupLevelAppResources(String appName, String groupName, List<String> templateNameList);

    /**
     * Delete the external properties resource
     *
     * @return the number of deleted records
     */
    int deleteExternalProperties();

    /**
     * Get a resource's content and its meta data
     *
     * @param resourceIdentifier {@link ResourceIdentifier} which identifies the resource
     * @return {@link ResourceContent}
     */
    ResourceContent getResourceContent(ResourceIdentifier resourceIdentifier);

    /**
     * Update the resource content
     *
     * @param resourceIdentifier the resource identifier
     * @param templateContent    the template content
     * @return return the updated content
     */
    String updateResourceContent(ResourceIdentifier resourceIdentifier, String templateContent);

    /**
     * Update the resource meta data
     *
     * @param resourceIdentifier the resource identifier
     * @param resourceName
     *@param metaData the template meta data  @return return the updated content
     */
    String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData);

    /**
     * Get all of the properties that were uploaded by an outside application/user
     *
     * @return the external properties
     */
    Properties getExternalProperties();

    /**
     * Get the name of the template files for a resource
     *
     * @param resourceIdentifier
     * @return a list of the names associated with an entity
     */
    List<String> getResourceNames(ResourceIdentifier resourceIdentifier);

    /**
     * Get the external properties file as a download
     *
     * @return the external properties as a file
     */
    File getExternalPropertiesAsFile() throws IOException;

    /**
     * Create a resource
     *
     * @param metaData     the meta data {@link ResourceTemplateMetaData}
     * @param templateData the template
     * @return {@link CreateResourceResponseWrapper}
     */
    CreateResourceResponseWrapper createResource(ResourceIdentifier resourceIdentifier, ResourceTemplateMetaData metaData,
                                                 InputStream templateData);

    /**
     * Upload a resource file
     *
     * @param resourceTemplateMetaData the meta data {@link ResourceTemplateMetaData}
     * @param resourceDataIn           the resource data input stream
     * @return the path where the file was uploaded to
     */
    String uploadResource(ResourceTemplateMetaData resourceTemplateMetaData, InputStream resourceDataIn);

    /**
     * Preview the resource content
     *
     * @param resourceHierarchyParam the group, JVM, web server, and web application names that identify the resource
     * @param content                the untokenized template content
     * @return the tokenized template content
     */
    String previewResourceContent(ResourceIdentifier resourceHierarchyParam, String content);

    /**
     * Get the external properties as a string
     *
     * @return the external properties as a string
     */
    String getExternalPropertiesAsString();

    <T> ResourceTemplateMetaData getTokenizedMetaData(String fileName, T entity, String metaDataStr) throws IOException;

    ResourceTemplateMetaData getMetaData(String jsonMetaData) throws IOException;

    void validateAllResourcesForGeneration(ResourceIdentifier resourceIdentifier);

    void validateSingleResourceForGeneration(ResourceIdentifier resourceIdentifier);

    <T> CommandOutput generateAndDeployFile(ResourceIdentifier resourceIdentifier, String name, String fileName, String hostName);

    /**
     * Get mime type
     * @param fileContents a buffered inputstream that contains the contents of a file
     * @return {@link org.apache.tika.mime.MediaType}
     */
    String getResourceMimeType(BufferedInputStream fileContents);
}
