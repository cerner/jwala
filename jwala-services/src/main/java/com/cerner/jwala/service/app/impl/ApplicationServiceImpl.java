package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class ApplicationServiceImpl implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ExecutorService executorService;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    BinaryDistributionControlService distributionControlService;

    private final ResourceService resourceService;

    private final BinaryDistributionService binaryDistributionService;

    private GroupPersistenceService groupPersistenceService;

    private final HistoryFacadeService historyFacadeService;
    Object lockObject = new Object();

    public ApplicationServiceImpl(final ApplicationPersistenceService applicationPersistenceService,
                                  final JvmPersistenceService jvmPersistenceService,
                                  final GroupPersistenceService groupPersistenceService,
                                  final ResourceService resourceService,
                                  final BinaryDistributionService binaryDistributionService,
                                  final HistoryFacadeService historyFacadeService,
                                  final BinaryDistributionLockManager binaryDistributionLockManager) {
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        this.historyFacadeService = historyFacadeService;
        this.resourceService = resourceService;
        this.binaryDistributionService = binaryDistributionService;
        this.binaryDistributionLockManager = binaryDistributionLockManager;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("resources.thread-task-executor.pool.size", "25")));
    }


    @Transactional(readOnly = true)
    @Override
    public Application getApplication(Identifier<Application> aApplicationId) {
        return applicationPersistenceService.getApplication(aApplicationId);
    }

    @Transactional(readOnly = true)
    @Override
    public Application getApplication(final String name) {
        return applicationPersistenceService.getApplication(name);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> getApplications() {
        return applicationPersistenceService.getApplications();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplications(Identifier<Group> groupId) {
        return applicationPersistenceService.findApplicationsBelongingTo(groupId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId) {
        return applicationPersistenceService.findApplicationsBelongingToJvm(jvmId);
    }

    @Transactional
    @Override
    public Application updateApplication(UpdateApplicationRequest updateApplicationRequest, User anUpdatingUser) {
        updateApplicationRequest.validate();

        final Application application = applicationPersistenceService.updateApplication(updateApplicationRequest);
        updateApplicationWarMetaData(updateApplicationRequest, application);
        return application;
    }

    private void updateApplicationWarMetaData(UpdateApplicationRequest updateApplicationRequest, Application application) {
        final String appWarName = application.getWarName();
        if (!appWarName.isEmpty()) {
            final String appName = application.getName();
            try {
                String originalJsonMetaData = groupPersistenceService.getGroupAppResourceTemplateMetaData(application.getGroup().getName(), appWarName);
                ResourceTemplateMetaData originalMetaData = resourceService.getMetaData(originalJsonMetaData);
                ResourceTemplateMetaData updateMetaData = new ResourceTemplateMetaData(
                        originalMetaData.getTemplateName(),
                        originalMetaData.getContentType(),
                        originalMetaData.getDeployFileName(),
                        originalMetaData.getDeployPath(),
                        originalMetaData.getEntity(),
                        updateApplicationRequest.isUnpackWar(),
                        originalMetaData.isOverwrite(),
                        originalMetaData.isHotDeploy());
                String updateJsonMetaData = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(updateMetaData);
                groupPersistenceService.updateGroupAppResourceMetaData(application.getGroup().getName(), appName, appWarName, updateJsonMetaData);
            } catch (JsonGenerationException e) {
                final String jsonGenerationExceptionMsg = MessageFormat.format("Failed to generate JSON meta data for web app {0} and resource {1}", appName, appWarName);
                LOGGER.error(jsonGenerationExceptionMsg, e);
                throw new ApplicationServiceException(jsonGenerationExceptionMsg);
            } catch (JsonMappingException e) {
                final String jsonMappingExceptionMsg = MessageFormat.format("Failed to map JSON meta data for web app {0} and resource {1}", appName, appWarName);
                LOGGER.error(jsonMappingExceptionMsg, e);
                throw new ApplicationServiceException(jsonMappingExceptionMsg);
            } catch (IOException e) {
                final String ioExceptionMsg = MessageFormat.format("Failed to update the war meta data for application {0} and war {1}", appName, appWarName);
                LOGGER.error(ioExceptionMsg, e);
                throw new ApplicationServiceException(ioExceptionMsg);
            }
        }
    }

    @Transactional
    @Override
    public Application createApplication(final CreateApplicationRequest createApplicationRequest,
                                         final User aCreatingUser) {

        createApplicationRequest.validate();

        return applicationPersistenceService.createApplication(createApplicationRequest);
    }

    @Transactional
    @Override
    public void removeApplication(Identifier<Application> anAppIdToRemove, User user) {
        applicationPersistenceService.removeApplication(anAppIdToRemove);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(final String appName, final String jvmName) {
        return applicationPersistenceService.getResourceTemplateNames(appName, jvmName);
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName) {
        return applicationPersistenceService.updateResourceTemplate(appName, resourceTemplateName, template, jvmName, groupName);
    }

    @Override
    @Transactional
    // TODO: Have an option to do a hot deploy or not.
    public CommandOutput deployConf(final String appName, final String groupName, final String jvmName,
                                    final String resourceTemplateName, ResourceGroup resourceGroup, User user) {
        String lockKey = generateKey(groupName, jvmName, appName, resourceTemplateName);
        try {
            // only one at a time
            binaryDistributionLockManager.writeLock(lockKey);
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setResourceName(resourceTemplateName)
                    .setGroupName(groupName)
                    .setWebAppName(appName)
                    .setJvmName(jvmName)
                    .build();
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            checkJvmStateBeforeDeploy(jvm, resourceIdentifier);
            return resourceService.generateAndDeployFile(resourceIdentifier, appName + "-" + jvmName, resourceTemplateName, jvm.getHostName());
        } catch (ResourceFileGeneratorException e) {
            LOGGER.error("Fail to generate the resource file {}", resourceTemplateName, e);
            throw new DeployApplicationConfException(e);
        } finally {
            binaryDistributionLockManager.writeUnlock(lockKey);
        }
    }

    private void checkJvmStateBeforeDeploy(Jvm jvm, ResourceIdentifier resourceIdentifier) {
        try {
            String metaDataStr = resourceService.getResourceContent(resourceIdentifier).getMetaData();
            boolean hotDeploy = resourceService.getTokenizedMetaData(resourceIdentifier.resourceName, jvm, metaDataStr).isHotDeploy();
            if (jvm.getState().isStartedState()) {
                if (hotDeploy) {
                    LOGGER.info("JVM {} is started, but resource {} is configured with hotDeploy=true. Continuing with deploy ...", jvm.getJvmName(), resourceIdentifier.resourceName);
                } else {
                    String deployMsg = MessageFormat.format("The JVM {0} must be stopped or the resource {1} must be configured with hotDeploy=true before the resource can be deployed", jvm.getJvmName(), resourceIdentifier.resourceName);
                    LOGGER.error(deployMsg);
                    throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, deployMsg);
                }
            }
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Failed to parse the meta data of resource {0} for JVM {1}", resourceIdentifier.resourceName, jvm.getJvmName());
            LOGGER.error(errMsg, e);
            throw new ApplicationServiceException(errMsg);
        }
    }

    /**
     * Method to generate lock key;
     *
     * @return
     */
    private String generateKey(String... keys) {
        final StringBuilder keyBuilder = new StringBuilder(keys.length);
        for (String key : keys) {
            keyBuilder.append(key);
        }
        return keyBuilder.toString();
    }

    @Override
    @Transactional
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest) {
        uploadAppTemplateRequest.validate();
        Jvm jvm = jvmPersistenceService.findJvmByExactName(uploadAppTemplateRequest.getJvmName());
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
        return applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
    }

    @Override
    @Transactional
    public String previewResourceTemplate(String fileName, String appName, String groupName, String jvmName, String template, ResourceGroup resourceGroup) {
        final Application application;
        if (StringUtils.isNotEmpty(jvmName)) {
            application = applicationPersistenceService.findApplication(appName, groupName, jvmName);
            application.setParentJvm(jvmPersistenceService.findJvmByExactName(jvmName));
        } else {
            application = applicationPersistenceService.getApplication(appName);
        }
        return resourceService.generateResourceFile(fileName, template, resourceGroup, application, ResourceGeneratorType.PREVIEW);
    }

    @Override
    @Transactional
    public void copyApplicationWarToGroupHosts(Application application) {
        Group group = groupPersistenceService.getGroup(application.getGroup().getId());
        final Set<Jvm> theJvms = group.getJvms();
        if (theJvms != null && !theJvms.isEmpty()) {
            Set<String> hostNames = new HashSet<>();
            for (Jvm jvm : theJvms) {
                final String host = jvm.getHostName().toLowerCase(Locale.US);
                if (!hostNames.contains(host)) {
                    hostNames.add(host);
                }
            }
            copyAndExecuteCommand(application, hostNames);
        }
    }

    @Override
    public void deployApplicationResourcesToGroupHosts(String groupName, Application app, ResourceGroup resourceGroup) {
        List<String> appResourcesNames = groupPersistenceService.getGroupAppsResourceTemplateNames(groupName);
        final List<Jvm> jvms = jvmPersistenceService.getJvmsByGroupName(groupName);
        if (null != appResourcesNames && !appResourcesNames.isEmpty()) {
            for (String resourceTemplateName : appResourcesNames) {
                String metaDataStr = groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplateName);
                try {
                    ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(resourceTemplateName, app, metaDataStr);
                    if (jvms != null && !jvms.isEmpty() && !metaData.getEntity().getDeployToJvms()) {
                        // still need to iterate through the JVMs to get the host names
                        Set<String> hostNames = new HashSet<>();
                        for (Jvm jvm : jvms) {
                            final String host = jvm.getHostName().toLowerCase(Locale.US);
                            if (!hostNames.contains(host)) {
                                hostNames.add(host);
                                executeDeployGroupAppTemplate(groupName, resourceTemplateName, app, jvm.getHostName());
                            }
                        }

                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to map meta data for template {} in group {}", resourceTemplateName, groupName, e);
                    throw new InternalErrorException(FaultType.BAD_STREAM, "Failed to read meta data for template " + resourceTemplateName + " in group " + groupName, e);
                }
            }
        }

    }

    @Override
    @Transactional
    public void copyApplicationWarToHost(Application application, String hostName) {
        if (hostName != null && !hostName.isEmpty()) {
            Set<String> hostNames = new HashSet<>();
            hostNames.add(hostName);
            copyAndExecuteCommand(application, hostNames);
        }
    }

    private void copyAndExecuteCommand(Application application, Set<String> hostNames) {
        File applicationWar = new File(application.getWarPath());
        final String sourcePath = applicationWar.getParent();
        File tempWarFile = new File(sourcePath + "/" + application.getWarName());
        Map<String, Future<CommandOutput>> futures = new HashMap<>();
        try {
            FileCopyUtils.copy(applicationWar, tempWarFile);
            final String destPath = ApplicationProperties.get("remote.jwala.webapps.dir");
            for (String hostName : hostNames) {
                Future<CommandOutput> commandOutputFuture = executeCopyCommand(application, tempWarFile, destPath, null, hostName);
                futures.put(hostName, commandOutputFuture);
            }
            for (Entry<String, Future<CommandOutput>> entry : futures.entrySet()) {
                CommandOutput execData = entry.getValue().get();
                if (execData.getReturnCode().wasSuccessful()) {
                    LOGGER.info("Copy of application war {} to {} was successful", applicationWar.getName(), entry.getKey());
                } else {
                    String errorOutput = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                    LOGGER.error("Copy of application war {} to {} FAILED::{}", applicationWar.getName(), entry.getKey(), errorOutput);
                    throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Failed to copy application war to the group host " + entry.getKey());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Creation of temporary war file for {} FAILED :: {}", application.getWarPath(), e);
            throw new InternalErrorException(FaultType.INVALID_PATH, "Failed to create temporary war file for copying to remote hosts");
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("FAILURE getting return status from copying web app war", e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Exception thrown while copying war", e);
        } finally {
            if (tempWarFile.exists()) {
                tempWarFile.delete();
            }
        }
    }

    protected Future<CommandOutput> executeCopyCommand(final Application application, final File tempWarFile, final String destPath, final Jvm jvm, final String host) {
        final String name = application.getName();
        Future<CommandOutput> commandOutputFuture = executorService.submit(new Callable<CommandOutput>() {
            @Override
            public CommandOutput call() throws Exception {
                final String parentDir = destPath;
                CommandOutput commandOutput = distributionControlService.createDirectory(host, parentDir);
                if (commandOutput.getReturnCode().wasSuccessful()) {
                    LOGGER.info("Successfully created parent dir {} on host {}", parentDir, host);
                } else {
                    final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                    LOGGER.error("Error in creating parent dir {} on host {}:: ERROR : {}", parentDir, host, standardError);
                    throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
                }
                LOGGER.info("Copying {} war to host {}", name, host);
                commandOutput = distributionControlService.secureCopyFile(host, tempWarFile.getAbsolutePath().replaceAll("\\\\", "/"), destPath);

                if (application.isUnpackWar()) {
                    final String warName = application.getWarName();
                    LOGGER.info("Unpacking war {} on host {}", warName, host);
                    String jwalaScriptPath = ApplicationProperties.get(PropertyKeys.REMOTE_SCRIPT_DIR);

                    // create the .jwala directory as the destination for the unpack-war script
                    commandOutput = distributionControlService.createDirectory(host, jwalaScriptPath);
                    if (commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.info("Successfully created the parent dir {} on host", jwalaScriptPath, host);
                    } else {
                        final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                        LOGGER.error("Error in creating parent dir {} on host {}:: ERROR : {}", jwalaScriptPath, host, standardError);
                        throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
                    }

                    final String unpackWarScriptPath = ApplicationProperties.get("commands.scripts-path") + "/" + AemControl.Properties.UNPACK_BINARY_SCRIPT_NAME;
                    final String destinationUnpackWarScriptPath = jwalaScriptPath + "/" + AemControl.Properties.UNPACK_BINARY_SCRIPT_NAME;
                    commandOutput = distributionControlService.secureCopyFile(host, unpackWarScriptPath, destinationUnpackWarScriptPath);

                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.error("Error in copying the " + unpackWarScriptPath + " to " + destinationUnpackWarScriptPath + " on " + host);
                        return commandOutput; // return immediately if the copy failed
                    }

                    // make sure the scripts are executable
                    commandOutput = distributionControlService.changeFileMode(host, "a+x", jwalaScriptPath, "*.sh");
                    if (!commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.error("Error in changing file permissions on " + jwalaScriptPath + " on host:" + host);
                        return commandOutput;
                    }

                    binaryDistributionService.distributeUnzip(host);

                    final String zipDestinationOption = FilenameUtils.removeExtension(destPath);

                    LOGGER.debug("Checking if previously unpacked: {}", zipDestinationOption);
                    commandOutput = distributionControlService.checkFileExists(host, zipDestinationOption);

                    if (commandOutput.getReturnCode().wasSuccessful()) {
                        LOGGER.debug("unpacked directory found at {}, backing it up", zipDestinationOption);
                        commandOutput = distributionControlService.backupFileWithMove(host, zipDestinationOption);

                        if (commandOutput.getReturnCode().wasSuccessful()) {
                            LOGGER.debug("successful back up of {}", zipDestinationOption);
                        } else {
                            final String standardError = "Could not back up " + zipDestinationOption;
                            LOGGER.error(standardError);
                            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
                        }
                    }

                    commandOutput = distributionControlService.unzipBinary(host, destPath, zipDestinationOption, "");
                }
                return commandOutput;
            }
        });
        return commandOutputFuture;
    }

    @Override
    public void deployConf(final String appName, final String hostName, final User user) {
        final Application application = applicationPersistenceService.getApplication(appName);
        final Group group = groupPersistenceService.getGroup(application.getGroup().getId());
        final List<String> hostNames = getDeployHostList(hostName, group, application);

        LOGGER.info("deploying templates to hosts: {}", hostNames.toString());
        historyFacadeService.write("", group, "Deploy \"" + appName + "\" resources", EventType.USER_ACTION_INFO, user.getId());

        checkForRunningJvms(group, hostNames, user);

        validateApplicationResources(appName, group);

        final Set<String> resourceSet = getWebAppOnlyResources(group, appName);
        final List<String> keys = getKeysAndAcquireWriteLock(appName, hostNames);
        try {
            final Map<String, Future<Set<CommandOutput>>> futures = deployApplicationResourcesForHosts(hostNames, group, application, resourceSet);
            waitForDeploy(appName, futures);
        } finally {
            releaseWriteLocks(keys);
        }
    }

    private void validateApplicationResources(String appName, Group group) {
        final String groupName = group.getName();
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("*")
                .setGroupName(groupName)
                .setWebAppName(appName)
                .build();
        resourceService.validateAllResourcesForGeneration(resourceIdentifier);
        LOGGER.info("Application {} in group {} passed resource validation", appName, groupName);
    }

    protected List<String> getDeployHostList(final String hostName, final Group group, final Application application) {
        final String groupName = group.getName();
        final String appName = application.getName();
        final List<String> hostNames = new ArrayList<>();
        final List<String> allHosts = groupPersistenceService.getHosts(groupName);
        if (allHosts == null || allHosts.isEmpty()) {
            LOGGER.error("No hosts found for the group: {} and application: {}", groupName, appName);
            throw new InternalErrorException(FaultType.GROUP_MISSING_HOSTS, "No host found for the application " + appName);
        }
        if (hostName == null || hostName.isEmpty()) {
            LOGGER.info("Hostname not passed, deploying to all hosts");
            for (String host : allHosts) {
                hostNames.add(host.toLowerCase(Locale.US));
            }
        } else {
            LOGGER.info("host name provided {}", hostName);
            for (final String host : allHosts) {
                if (hostName.equalsIgnoreCase(host)) {
                    hostNames.add(host.toLowerCase(Locale.US));
                }
            }
            if (hostNames.isEmpty()) {
                LOGGER.error("Hostname {} does not belong to the group {}", hostName, groupName);
                throw new InternalErrorException(FaultType.INVALID_HOST_NAME, "The hostname: " + hostName + " does not belong to the group " + groupName);
            }
        }
        return hostNames;
    }

    protected void checkForRunningJvms(final Group group, final List<String> hostNames, final User user) {
        final Set<Jvm> runningJvmList = new HashSet<>();
        final List<String> runningJvmNameList = new ArrayList<>();
        List<Jvm> jvmsInGroup = jvmPersistenceService.getJvmsByGroupName(group.getName());
        for (final Jvm jvm : jvmsInGroup) {
            if (hostNames.contains(jvm.getHostName().toLowerCase(Locale.US)) && jvm.getState().isStartedState()) {
                runningJvmList.add(jvm);
                runningJvmNameList.add(jvm.getJvmName());
            }
        }

        if (!runningJvmList.isEmpty()) {
            final String errMsg = "Make sure the following JVMs are completely stopped before deploying.";
            LOGGER.error(errMsg + " {}", runningJvmNameList);
            for (final Jvm jvm : runningJvmList) {
                historyFacadeService.write(Jvm.class.getSimpleName() + " " + jvm.getJvmName(), jvm.getGroups(),
                        "Web app resource(s) cannot be deployed on a running JVM!",
                        EventType.SYSTEM_ERROR, user.getId());
            }
            throw new ApplicationServiceException(FaultType.RESOURCE_DEPLOY_FAILURE, errMsg, runningJvmNameList);
        }
    }

    private Set<String> getWebAppOnlyResources(final Group group, String appName) {
        final String groupName = group.getName();
        final Set<String> resourceSet = new HashSet<>();
        List<String> resourceTemplates = groupPersistenceService.getGroupAppsResourceTemplateNames(groupName, appName);
        for (String resourceTemplate : resourceTemplates) {
            String metaDataStr = groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplate);
            LOGGER.debug("metadata for template: {} is {}", resourceTemplate, metaDataStr);
            try {
                ResourceTemplateMetaData metaData = resourceService.getMetaData(metaDataStr);
                if (!metaData.getEntity().getDeployToJvms()) {
                    LOGGER.info("Template {} needs to be deployed adding it to the list", resourceTemplate);
                    resourceSet.add(resourceTemplate);
                } else {
                    LOGGER.info("Not deploying {} because deployToJvms=true", resourceTemplate);
                }
            } catch (IOException e) {
                LOGGER.error("Error in templatizing the metadata file", e);
                throw new InternalErrorException(FaultType.IO_EXCEPTION, "Error in templatizing the metadata for resource " + resourceTemplate);
            }
        }
        return resourceSet;
    }

    protected Map<String, Future<Set<CommandOutput>>> deployApplicationResourcesForHosts(final List<String> hostNames,
                                                                                         final Group group,
                                                                                         final Application application,
                                                                                         final Set<String> resourceSet) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Map<String, Future<Set<CommandOutput>>> futures = new HashMap<>();
        for (final String host : hostNames) {
            Future<Set<CommandOutput>> commandOutputFutureSet = executorService.submit
                    (new Callable<Set<CommandOutput>>() {
                         @Override
                         public Set<CommandOutput> call() throws Exception {
                             SecurityContext context = SecurityContextHolder.createEmptyContext();
                             try {
                                 Set<CommandOutput> commandOutputs = new HashSet<>();
                                 context.setAuthentication(authentication);
                                 SecurityContextHolder.setContext(context);
                                 for (final String resource : resourceSet) {
                                     LOGGER.info("Deploying {} to host {}", resource, host);
                                     commandOutputs.add(executeDeployGroupAppTemplate(group.getName(), resource, application, host));
                                 }
                                 return commandOutputs;
                             } finally {
                                 SecurityContextHolder.clearContext();
                             }

                         }
                     }
                    );
            futures.put(host, commandOutputFutureSet);
        }
        return futures;
    }

    /**
     * This method executes all the commands for copying the template over to the destination for a group app config file
     * <p>
     * NOTE!!! This method has a duplicate in GroupServiceImpl. DO NOT USE GroupService just to remote this because it will
     * create an intermittent Spring circular dependency!!!
     *
     * @param groupName   name of the group in which the application can be found
     * @param fileName    name of the file that needs to deployed
     * @param application the application object for the application to deploy the config file too
     * @param hostName    name of the host which needs the application file
     * @return returns a command output object
     */
    private CommandOutput executeDeployGroupAppTemplate(final String groupName, final String fileName,
                                                        final Application application, final String hostName) {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName(fileName)
                .setGroupName(groupName)
                .setWebAppName(application.getName())
                .build();
        return resourceService.generateAndDeployFile(resourceIdentifier, application.getName(), fileName, hostName);
    }

    protected void waitForDeploy(final String appName, final Map<String, Future<Set<CommandOutput>>> futures) {
        long timeout = Long.parseLong(ApplicationProperties.get("remote.jwala.execution.timeout.seconds", "600"));
        if (futures != null) {
            for (Entry<String, Future<Set<CommandOutput>>> entry : futures.entrySet()) {
                try {
                    Set<CommandOutput> commandOutputSet = entry.getValue().get(timeout, TimeUnit.SECONDS);
                    for (CommandOutput commandOutput : commandOutputSet) {
                        if (commandOutput != null && !commandOutput.getReturnCode().wasSuccessful()) {
                            final String errorMessage = "Error in deploying resources to host " + entry.getKey() +
                                    " for application " + appName;
                            LOGGER.error(errorMessage);
                            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, errorMessage);
                        }
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOGGER.error("Error in executing deploy", e);
                    throw new InternalErrorException(FaultType.RESOURCE_DEPLOY_FAILURE, e.getMessage());
                }
            }
        }
    }

    protected List<String> getKeysAndAcquireWriteLock(String appName, List<String> hostNames) {
        List<String> keys = new ArrayList<>();
        for (final String host : hostNames) {
            final String key = appName + "/" + host;
            binaryDistributionLockManager.writeLock(key);
            keys.add(key);
        }
        return keys;
    }

    protected void releaseWriteLocks(List<String> keys) {
        for (String key : keys) {
            binaryDistributionLockManager.writeUnlock(key);
        }
    }

}
