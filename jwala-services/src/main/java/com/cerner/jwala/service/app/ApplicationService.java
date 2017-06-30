package com.cerner.jwala.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.service.exception.ApplicationServiceException;

import java.util.List;

public interface ApplicationService {
    /**
     *  Get  application by Id
     * @param aApplicationId
     * @return
     */
    public Application getApplication(Identifier<Application> aApplicationId);

    /**
     * Get  Application by Name
     * @param name
     * @return
     */
    public Application getApplication(String name);

    /**
     *  Update Application
     * @param anAppToUpdate
     * @param user
     * @return
     */
    public Application updateApplication(UpdateApplicationRequest anAppToUpdate, User user) throws ApplicationServiceException;

    /**
     * Create Application
     * @param anAppToCreate
     * @param user
     * @return
     */
    public Application createApplication(CreateApplicationRequest anAppToCreate, User user);

    /**
     *  Remove Application
     * @param anAppIdToRemove
     * @param user
     */
    void removeApplication(Identifier<Application> anAppIdToRemove, User user);

    /**
     * Get list of Applications
     * @return
     */
    public List<Application> getApplications();

    /**
     * Get List of applications by group Id
     * @param groupId
     * @return
     */
    public List<Application> findApplications(Identifier<Group> groupId);

    /**
     *  Find Application by Id
     * @param jvmId
     * @return
     */
    public List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId);

    /**
     *  Get resource template names for a Jvm and Application by name
     * @param appName
     * @param jvmName
     * @return
     */
    public List<String> getResourceTemplateNames(final String appName, String jvmName);

    /**
     * Update resouce template
     * @param appName
     * @param resourceTemplateName
     * @param template
     * @param jvmName
     * @param groupName
     * @return
     */
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName);

    /**
     * Deploy a configuration file.
     *  @param appName              - the application name.
     * @param groupName
     * @param jvmName              - the jvm name where the application resides.
     * @param resourceTemplateName - the resource template in which the configuration file is based on.
     * @param resourceGroup
     * @param user                 - the user.    @return {@link CommandOutput}
     */
    public CommandOutput deployConf(String appName, String groupName, String jvmName, String resourceTemplateName, ResourceGroup resourceGroup, User user);

    /**
     * upload Application template
     * @param command
     * @return
     */
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest command);

    /**
     * Gets a preview of a resource file.
     *
     * @param appName   application name
     * @param groupName group name
     * @param jvmName   JVM name
     * @param template  the template to preview.
     * @param resourceGroup
     * @return The resource file preview.
     */
    public String previewResourceTemplate(String fileName, String appName, String groupName, String jvmName, String template, ResourceGroup resourceGroup);

    /**
     * method to copy application war to group hosts
     * @param application
     */
    public void copyApplicationWarToGroupHosts(Application application);

    /**
     * method to copy application war to host
     * @param application
     * @param hostName
     */
    public void copyApplicationWarToHost(Application application, String hostName);

    /**
     *
     * @param groupName
     * @param app
     * @param resourceGroup
     */
    public void deployApplicationResourcesToGroupHosts(String groupName, Application app, ResourceGroup resourceGroup);

    /**
     * method to execute backup
     * @param entity
     * @param host
     * @param source
     * @return
     * @throws CommandFailureException
     */
    /**
     * Method to deploy application configuration
     * @param appName
     * @param hostName
     * @param user
     */
    public void deployConf(String appName, String hostName, User user);
}
