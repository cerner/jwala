package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;
import com.cerner.jwala.common.request.jvm.*;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

import static com.cerner.jwala.control.AemControl.Properties.*;

public class JvmServiceImpl implements JvmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmServiceImpl.class);
    private static final String MEDIA_TYPE_TEXT = "text";

    private final BinaryDistributionLockManager binaryDistributionLockManager;
    private final String topicServerStates;
    private final JvmPersistenceService jvmPersistenceService;
    private final GroupPersistenceService groupPersistenceService;
    private final ApplicationService applicationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupStateNotificationService groupStateNotificationService;
    private final ResourceService resourceService;
    private final ClientFactoryHelper clientFactoryHelper;
    private final JvmControlService jvmControlService;
    private final HistoryFacadeService historyFacadeService;
    private final BinaryDistributionService binaryDistributionService;
    private final FileUtility fileUtility;

    @Autowired
    private JvmStateService jvmStateService;

    @Autowired
    private WebServerPersistenceService webServerPersistenceService;

    public JvmServiceImpl(final JvmPersistenceService jvmPersistenceService,
                          final GroupPersistenceService groupPersistenceService,
                          final ApplicationService applicationService,
                          final SimpMessagingTemplate messagingTemplate,
                          final GroupStateNotificationService groupStateNotificationService,
                          final ResourceService resourceService,
                          final ClientFactoryHelper clientFactoryHelper,
                          final String topicServerStates,
                          final JvmControlService jvmControlService,
                          final BinaryDistributionService binaryDistributionService,
                          final BinaryDistributionLockManager binaryDistributionLockManager,
                          final HistoryFacadeService historyFacadeService,
                          final FileUtility fileUtility) {
        this.jvmPersistenceService = jvmPersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        this.applicationService = applicationService;
        this.messagingTemplate = messagingTemplate;
        this.groupStateNotificationService = groupStateNotificationService;
        this.resourceService = resourceService;
        this.clientFactoryHelper = clientFactoryHelper;
        this.jvmControlService = jvmControlService;
        this.topicServerStates = topicServerStates;
        this.binaryDistributionService = binaryDistributionService;
        this.binaryDistributionLockManager = binaryDistributionLockManager;
        this.historyFacadeService = historyFacadeService;
        this.fileUtility = fileUtility;
    }


    protected Jvm createJvm(final CreateJvmRequest aCreateJvmRequest) {
        validateCreateJvm(aCreateJvmRequest);
        return jvmPersistenceService.createJvm(aCreateJvmRequest);
    }

    private void validateCreateJvm(CreateJvmRequest aCreateJvmRequest) {
        try {
            webServerPersistenceService.findWebServerByName(aCreateJvmRequest.getJvmName());
            String message = MessageFormat.format("Webserver already exists with this name {0}", aCreateJvmRequest.getJvmName());
            LOGGER.error(message);
            throw new JvmServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No webserver name conflict, ignore no result exception for creating jvm", pe);
        }
        try {
            jvmPersistenceService.findJvmByExactName(aCreateJvmRequest.getJvmName());
            String message = MessageFormat.format("Jvm already exists with this name {0}", aCreateJvmRequest.getJvmName());
            LOGGER.error(message);
            throw new JvmServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No Jvm name conflict, ignore no result exception for creating jvm", pe);
        }
    }

    protected Jvm createAndAssignJvm(final CreateJvmAndAddToGroupsRequest aCreateAndAssignRequest,
                                     final User aCreatingUser) {
        aCreateAndAssignRequest.validate();

        // The commands are validated in createJvm() and groupPersistenceService.addJvmToGroup()
        final Jvm newJvm = createJvm(aCreateAndAssignRequest.getCreateCommand());

        if (!aCreateAndAssignRequest.getGroups().isEmpty()) {
            final Set<AddJvmToGroupRequest> addJvmToGroupRequests = aCreateAndAssignRequest.toAddRequestsFor(newJvm.getId());
            addJvmToGroups(addJvmToGroupRequests);
        }

        return getJvm(newJvm.getId());
    }

    @Override
    @Transactional
    public Jvm createJvm(CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, User user) {
        // create the JVM in the database
        final Jvm jvm = createAndAssignJvm(createJvmAndAddToGroupsRequest, user);

        // inherit the templates from the group
        if (null != jvm.getGroups() && !jvm.getGroups().isEmpty()) {
            final Group parentGroup = jvm.getGroups().iterator().next();
            createDefaultTemplates(jvm.getJvmName(), parentGroup);
            if (jvm.getGroups().size() > 1) {
                LOGGER.warn("Multiple groups were associated with the JVM, but the JVM was created using the templates from group "
                        + parentGroup.getName());
            }
        }

        return jvm;
    }

    @Override
    @Transactional
    public void createDefaultTemplates(final String jvmName, Group parentGroup) {
        final String groupName = parentGroup.getName();
        // get the group JVM templates
        List<String> templateNames = groupPersistenceService.getGroupJvmsResourceTemplateNames(groupName);
        for (final String templateName : templateNames) {
            String templateContent = getGroupJvmResourceTemplate(groupName, templateName, resourceService.generateResourceGroup(), false);
            String metaDataStr = groupPersistenceService.getGroupJvmResourceTemplateMetaData(groupName, templateName);
            try {
                ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(templateName,
                        jvmPersistenceService.findJvmByExactName(jvmName), metaDataStr);
                final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                        .setResourceName(metaData.getDeployFileName())
                        .setJvmName(jvmName)
                        .setGroupName(groupName)
                        .build();
                resourceService.createResource(resourceIdentifier, metaData, IOUtils.toInputStream(templateContent, StandardCharsets.UTF_8));

            } catch (IOException e) {
                LOGGER.error("Failed to map meta data for JVM {} in group {}", jvmName, groupName, e);
                throw new InternalErrorException(FaultType.BAD_STREAM, "Failed to map meta data for JVM " + jvmName + " in group " + groupName, e);
            }
        }

        // get the group App templates
        templateNames = groupPersistenceService.getGroupAppsResourceTemplateNames(groupName);
        for (String templateName : templateNames) {
            String metaDataStr = groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, templateName);
            try {
                ResourceTemplateMetaData metaData = resourceService.getMetaData(metaDataStr);
                if (metaData.getEntity().getDeployToJvms()) {
                    final String template = resourceService.getAppTemplate(groupName, metaData.getEntity().getTarget(),
                            templateName);
                    final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                            .setResourceName(metaData.getTemplateName()).setJvmName(jvmName)
                            .setWebAppName(metaData.getEntity().getTarget()).build();
                    resourceService.createResource(resourceIdentifier, metaData, new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8)));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to map meta data while creating JVM for template {} in group {}",
                        templateName, groupName, e);
                throw new InternalErrorException(FaultType.BAD_STREAM, "Failed to map data for template " +
                        templateName + " in group " + groupName, e);
            }
        }
    }

    @Transactional
    private String getGroupJvmResourceTemplate(final String groupName,
                                               final String resourceTemplateName,
                                               final ResourceGroup resourceGroup,
                                               final boolean tokensReplaced) {

        final String template = groupPersistenceService.getGroupJvmResourceTemplate(groupName, resourceTemplateName);
        if (tokensReplaced) {
            // TODO returns the tokenized version of a dummy JVM, but make sure that when deployed each instance is tokenized per JVM
            final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
            if (jvms != null && !jvms.isEmpty()) {
                return resourceService.generateResourceFile(resourceTemplateName, template, resourceGroup, jvms.iterator().next(), ResourceGeneratorType.TEMPLATE);
            }
        }
        return template;
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final Identifier<Jvm> aJvmId) {
        return jvmPersistenceService.getJvm(aJvmId);
    }

    @Override
    @Transactional(readOnly = true)
    public Jvm getJvm(final String jvmName) {
        return jvmPersistenceService.findJvmByExactName(jvmName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Jvm> getJvms() {

        return jvmPersistenceService.getJvms();
    }

    @Override
    public List<Jvm> getJvmsByGroupName(String name) {
        return jvmPersistenceService.getJvmsByGroupName(name);
    }

    @Override
    @Transactional
    public Jvm updateJvm(final UpdateJvmRequest updateJvmRequest, final boolean updateJvmPassword) {

        updateJvmRequest.validate();
        Jvm originalJvm = getJvm(updateJvmRequest.getId());
        if (!originalJvm.getJvmName().equalsIgnoreCase(updateJvmRequest.getNewJvmName())) {
            validateUpdateJvm(updateJvmRequest);
            if (!originalJvm.getState().toStateLabel().equals(JvmState.JVM_NEW.toStateLabel())) {
                throw new JvmServiceException("JVM " + originalJvm.getJvmName() + " is in  " + originalJvm.getState().toStateLabel() + " state, can only rename NEW jvm's");
            }
        }
        jvmPersistenceService.removeJvmFromGroups(updateJvmRequest.getId());

        addJvmToGroups(updateJvmRequest.getAssignmentCommands());

        return jvmPersistenceService.updateJvm(updateJvmRequest, updateJvmPassword);
    }

    private void validateUpdateJvm(UpdateJvmRequest updateJvmRequest) {
        try {
            webServerPersistenceService.findWebServerByName(updateJvmRequest.getNewJvmName());
            String message = MessageFormat.format("Webserver already exists with this name {0}", updateJvmRequest.getNewJvmName());
            LOGGER.error(message);
            throw new JvmServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No webserver name conflict, ignore no result exception for creating jvm", pe);
        }
        try {
            jvmPersistenceService.findJvmByExactName(updateJvmRequest.getNewJvmName());
            String message = MessageFormat.format("Jvm already exists with this name {0}", updateJvmRequest.getNewJvmName());
            LOGGER.error(message);
            throw new JvmServiceException(message);
        } catch (NoResultException pe) {
            LOGGER.debug("No Jvm name conflict, ignore no result exception for creating jvm", pe);
        }
    }

    @Override
    @Transactional
    public void deleteJvm(final Identifier<Jvm> id, boolean hardDelete, final User user) {
        LOGGER.info("Deleting JVM with id = {} and hardDelete = {}", id, hardDelete);
        final Jvm jvm = jvmPersistenceService.getJvm(id);

        if (!hardDelete && !JvmState.JVM_NEW.equals(jvm.getState())) {
            final String msg = MessageFormat.format("Cannot delete JVM {0} since it has already been deployed",
                    jvm.getJvmName());
            LOGGER.error(msg);
            throw new JvmServiceException(msg);
        }

        if (hardDelete) {
            LOGGER.info("Deleting JVM service {}", jvm.getJvmName());

            if (!JvmState.JVM_NEW.equals(jvm.getState()) && !JvmState.JVM_STOPPED.equals(jvm.getState()) &&
                    !JvmState.FORCED_STOPPED.equals(jvm.getState())) {
                final String msg = MessageFormat.format("Please stop JVM {0} first before attempting to delete it",
                        jvm.getJvmName());
                LOGGER.warn(msg); // this is not a system error hence we only log it as a warning even though we throw
                // an exception
                throw new JvmServiceException(msg);
            }

            if (!JvmState.JVM_NEW.equals(jvm.getState())) {
                final CommandOutput commandOutput = jvmControlService.controlJvm(new ControlJvmRequest(jvm.getId(),
                        JvmControlOperation.DELETE_SERVICE), user);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    final String msg = MessageFormat.format("Failed to delete the JVM service {0}! CommandOutput = {1}",
                            jvm.getJvmName(), commandOutput);
                    LOGGER.error(msg);
                    throw new JvmServiceException(msg);
                }
            }

        }

        jvmPersistenceService.removeJvm(id);
    }

    @Override
    public void deleteJvmService(Jvm jvm, User user) {
        if (!jvm.getState().equals(JvmState.JVM_NEW)) {
            ControlJvmRequest controlJvmRequest = ControlJvmRequestFactory.create(JvmControlOperation.DELETE_SERVICE, jvm);
            CommandOutput commandOutput = jvmControlService.controlJvm(controlJvmRequest, user);
            final String jvmName = jvm.getJvmName();
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Delete of windows service {} was successful", jvmName);
            } else if (ExecReturnCode.JWALA_EXIT_CODE_SERVICE_DOES_NOT_EXIST == commandOutput.getReturnCode().getReturnCode()) {
                LOGGER.info("No such service found for {} during delete. Continuing with request.", jvmName);
            } else {
                String standardError =
                        commandOutput.getStandardError().isEmpty() ?
                                commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Deleting windows service {} failed :: ERROR: {}", jvmName, standardError);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                        standardError.isEmpty() ?
                                CommandOutputReturnCode.fromReturnCode(
                                        commandOutput.getReturnCode().getReturnCode())
                                        .getDesc() : standardError);
            }
        }
    }


    private void addJvmToGroups(final Set<AddJvmToGroupRequest> someAddCommands) {
        for (final AddJvmToGroupRequest command : someAddCommands) {
            LOGGER.info("Adding jvm {} to group {}", command.getJvmId(), command.getGroupId());
            groupPersistenceService.addJvmToGroup(command);
        }
    }

    @Override
    public Jvm generateAndDeployJvm(String jvmName, User user) {
        boolean didSucceed = false;
        Jvm jvm = getJvm(jvmName);
        LOGGER.debug("Start generateAndDeployJvm for {} by user {}", jvmName, user.getId());

        historyFacadeService.write(jvm.getHostName(), jvm.getGroups(), "Starting to generate remote JVM " +
                jvm.getJvmName() + " on host " + jvm.getHostName(), EventType.USER_ACTION_INFO, user.getId());

        //add write lock for multiple write
        binaryDistributionLockManager.writeLock(jvmName + "-" + jvm.getId().toString());

        try {
            if (jvm.getState().isStartedState()) {
                final String errorMessage = "The remote JVM " + jvm.getJvmName() + " must be stopped before attempting to generate the JVM";
                LOGGER.info(errorMessage);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, errorMessage);
            }

            validateJvmAndAppResources(jvm);

            checkForJdkBinaries(jvm);

            distributeBinaries(jvm);

            // create the scripts directory if it doesn't exist
            createScriptsDirectory(jvm);

            // copy the install and deploy scripts
            deployScriptsToUserJwalaScriptsDir(jvm, user);

            // delete the service
            deleteJvmService(jvm, user);

            // create the jar file
            //
            final String jvmConfigJar = generateJvmConfigJar(jvm);

            // copy the jar file
            secureCopyJvmConfigJar(jvm, jvmConfigJar, user);

            // call script to backup and tar the current directory and
            // then untar the new tar, needs jar
            deployJvmConfigJar(jvm, user, jvmConfigJar);

            // copy the individual jvm templates to the destination
            deployJvmResourceFiles(jvm, user);

            // deploy any application context xml's in the group
            deployApplicationContextXMLs(jvm, user);

            // re-install the service
            installJvmWindowsService(jvm, user);

            // set the state to stopped
            updateState(jvm.getId(), JvmState.JVM_STOPPED);

            didSucceed = true;
        } catch (CommandFailureException | IOException e) {
            LOGGER.error("Failed to generate the JVM config for {}", jvm.getJvmName(), e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Failed to generate the JVM config: " + jvm.getJvmName(), e);
        } finally {
            binaryDistributionLockManager.writeUnlock(jvmName + "-" + jvm.getId().toString());
            LOGGER.debug("End generateAndDeployJvm for {} by user {}", jvmName, user.getId());

            final EventType eventType = didSucceed ? EventType.SYSTEM_INFO : EventType.SYSTEM_ERROR;

            String historyMessage = didSucceed ? "Remote generation of jvm " + jvm.getJvmName() + " to host " + jvm.getHostName() + " succeeded" :
                    "Remote generation of jvm " + jvm.getJvmName() + " to host " + jvm.getHostName() + " failed";

            historyFacadeService.write(jvm.getHostName(), jvm.getGroups(), historyMessage, eventType, user.getId());
        }
        return jvm;
    }

    private void checkForJdkBinaries(Jvm jvm) {
        if (jvm.getJdkMedia() == null) {
            final String jvmName = jvm.getJvmName();
            LOGGER.error("No JDK version specified for JVM {}. Stopping the JV generation.", jvmName);
            throw new InternalErrorException(FaultType.JVM_JDK_NOT_SPECIFIED, "No JDK version specified for JVM " + jvmName + ". Stopping the JVM generation.");
        }
    }

    private void validateJvmAndAppResources(Jvm jvm) {
        String jvmName = jvm.getJvmName();
        Map<String, List<String>> jvmAndAppResourcesExceptions = new HashMap<>();

        // validate the JVM resources
        try {
            final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setJvmName(jvmName)
                    .setResourceName("*")
                    .build();
            resourceService.validateAllResourcesForGeneration(resourceIdentifier);
        } catch (InternalErrorException iee) {
            LOGGER.info("Catching known JVM resource generation exception, and now validating application resources");
            LOGGER.debug("This JVM resource generation exception should have already been logged previously", iee);
            jvmAndAppResourcesExceptions.putAll(iee.getErrorDetails());
        }

        // now validate and app resources for the JVM
        List<Group> groupList = jvmPersistenceService.findGroupsByJvm(jvm.getId());
        for (Group group : groupList) {
            List<Application> applications = applicationService.findApplications(group.getId());
            if (applications != null) {
                for (Application app : applications) {
                    final String appName = app.getName();
                    List<String> templateNames = applicationService.getResourceTemplateNames(appName, jvmName);
                    if (templateNames != null) {
                        for (String templateName : templateNames) {
                            try {
                                final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                                        .setResourceName(templateName)
                                        .setGroupName(group.getName())
                                        .setJvmName(jvmName)
                                        .setWebAppName(appName)
                                        .build();
                                resourceService.validateSingleResourceForGeneration(resourceIdentifier);
                            } catch (InternalErrorException iee) {
                                LOGGER.info("Catching known app resource generation exception, and now consolidating with the JVM resource exceptions");
                                LOGGER.debug("This application resource generation exception should have already been logged previously", iee);
                                jvmAndAppResourcesExceptions.putAll(iee.getErrorDetails());
                            }
                        }
                    }
                }
            }
        }

        if (!jvmAndAppResourcesExceptions.isEmpty()) {
            throw new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Failed to generate the resources for JVM " + jvmName, null, jvmAndAppResourcesExceptions);
        }
    }

    private void distributeBinaries(Jvm jvm) {
        final String hostName = jvm.getHostName();
        try {
            binaryDistributionLockManager.writeLock(hostName);
            binaryDistributionService.distributeUnzip(hostName);
            binaryDistributionService.distributeJdk(jvm);
        } finally {
            binaryDistributionLockManager.writeUnlock(hostName);
        }
    }

    protected void createScriptsDirectory(Jvm jvm) throws CommandFailureException {
        final String scriptsDir = ApplicationProperties.get(PropertyKeys.REMOTE_SCRIPT_DIR);
        final CommandOutput commandOutput = jvmControlService.executeCreateDirectoryCommand(jvm, scriptsDir);
        ExecReturnCode resultReturnCode = commandOutput.getReturnCode();
        if (!resultReturnCode.wasSuccessful()) {
            LOGGER.error("Creating scripts directory {} FAILED ", scriptsDir);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, commandOutput.getStandardError().isEmpty() ? CommandOutputReturnCode.fromReturnCode(resultReturnCode.getReturnCode()).getDesc() : commandOutput.getStandardError());
        }

    }

    protected void deployScriptsToUserJwalaScriptsDir(Jvm jvm, User user) throws CommandFailureException, IOException {
        final ControlJvmRequest secureCopyRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.SCP);
        final String commandsScriptsPath = ApplicationProperties.get("commands.scripts-path");

        final String deployConfigJarPath = commandsScriptsPath + '/' + DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME;
        final String jvmName = jvm.getJvmName();
        final String userId = user.getId();
        final String scriptsDir = ApplicationProperties.get(PropertyKeys.REMOTE_SCRIPT_DIR);

        final String stagingArea = scriptsDir + '/' + jvmName;

        createParentDir(jvm, stagingArea);
        final String failedToCopyMessage = "Failed to secure copy ";
        final String duringCreationMessage = " during the creation of ";

        // copy the unjar script
        final String destinationDeployJarPath = stagingArea + '/' + DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME;
        final boolean alwaysOverwriteScripts = true;
        if (!jvmControlService.secureCopyFile(secureCopyRequest, deployConfigJarPath, destinationDeployJarPath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + deployConfigJarPath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // copy the install service script
        final String installServicePath = commandsScriptsPath + '/' + INSTALL_SERVICE_SCRIPT_NAME;
        final String destinationInstallServicePath = stagingArea + '/' + INSTALL_SERVICE_SCRIPT_NAME;

        if (!jvmControlService.secureCopyFile(secureCopyRequest, installServicePath, destinationInstallServicePath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + installServicePath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }
        final String deleteServicePath = commandsScriptsPath + "/" + DELETE_SERVICE_SCRIPT_NAME;
        final String destinationDeleteServicePath = stagingArea + "/" + DELETE_SERVICE_SCRIPT_NAME;

        // copy the delete service script
        if (!jvmControlService.secureCopyFile(secureCopyRequest, deleteServicePath, destinationDeleteServicePath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + deleteServicePath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // copy the start service script
        final String startServicePath = commandsScriptsPath + "/" + START_SCRIPT_NAME;
        final String destinationStartServicePath = stagingArea + "/" + START_SCRIPT_NAME;

        if (!jvmControlService.secureCopyFile(secureCopyRequest, startServicePath, destinationStartServicePath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + startServicePath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // copy the stop service script
        final String stopServicePath = commandsScriptsPath + "/" + STOP_SCRIPT_NAME;
        final String destinationStopServicePath = stagingArea + "/" + STOP_SCRIPT_NAME;

        if (!jvmControlService.secureCopyFile(secureCopyRequest, stopServicePath, destinationStopServicePath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + stopServicePath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // copy the thread dump script
        final String threadDumpPath = commandsScriptsPath + "/" + THREAD_DUMP_SCRIPT_NAME;
        final String destinationThreadDumpPath = stagingArea + "/" + THREAD_DUMP_SCRIPT_NAME;

        if (!jvmControlService.secureCopyFile(secureCopyRequest, threadDumpPath, destinationThreadDumpPath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + threadDumpPath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // copy the heap dump script
        final String heapDumpPath = commandsScriptsPath + "/" + HEAP_DUMP_SCRIPT_NAME;
        final String destinationHeapDumpPath = stagingArea + "/" + HEAP_DUMP_SCRIPT_NAME;

        if (!jvmControlService.secureCopyFile(secureCopyRequest, heapDumpPath, destinationHeapDumpPath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + heapDumpPath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        // copy the status service script
        final String serviceStatusPath = commandsScriptsPath + "/" + SERVICE_STATUS_SCRIPT_NAME;
        final String destinationServiceStatusPath = stagingArea + "/" + SERVICE_STATUS_SCRIPT_NAME;

        if (!jvmControlService.secureCopyFile(secureCopyRequest, serviceStatusPath, destinationServiceStatusPath, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + serviceStatusPath + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

        //TODO move to constant
        final String linuxJvmService = "/linux/jvm-service.sh";
        final CommandOutput commandOutput = jvmControlService.executeCreateDirectoryCommand(jvm, stagingArea + "/linux");
        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("created {} directory successfully", stagingArea + "/linux");
        } else {
            final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
            LOGGER.error("create command failed with error trying to create parent directory {} on {} :: ERROR: {}", stagingArea + "/linux", jvm.getHostName(), standardError);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
        }

        if (!jvmControlService.secureCopyFile(secureCopyRequest, commandsScriptsPath + linuxJvmService, stagingArea + linuxJvmService, userId, alwaysOverwriteScripts).getReturnCode().wasSuccessful()) {
            String message = failedToCopyMessage + commandsScriptsPath + linuxJvmService + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }
        // make sure the scripts are executable
        if (!jvmControlService.executeChangeFileModeCommand(jvm, "a+x", stagingArea, "*.sh").getReturnCode().wasSuccessful()) {
            String message = "Failed to change the file permissions in " + stagingArea + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }
        //TODO fix constants
        if (!jvmControlService.executeChangeFileModeCommand(jvm, "a+x", stagingArea + "/linux", "jvm-service.sh").getReturnCode().wasSuccessful()) {
            String message = "Failed to change the file permissions in " + stagingArea + linuxJvmService + duringCreationMessage + jvmName;
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

    }

    protected String generateJvmConfigJar(Jvm jvm) throws CommandFailureException {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Start generateJvmConfigJar ");
        ManagedJvmBuilder managedJvmBuilder =
                new ManagedJvmBuilder().
                        jvm(jvmPersistenceService.findJvmByExactName(jvm.getJvmName())).
                        fileUtility(fileUtility).
                        resourceService(resourceService).
                        build();
        LOGGER.debug("End generateJvmConfigJar, timetaken {} ms", (System.currentTimeMillis() - startTime));
        return managedJvmBuilder.getStagingDir().getAbsolutePath();
    }

    private void createParentDir(final Jvm jvm, final String parentDir) throws CommandFailureException {
        final CommandOutput commandOutput = jvmControlService.executeCreateDirectoryCommand(jvm, parentDir);
        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("created {} directory successfully", parentDir);
        } else {
            final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
            LOGGER.error("create command failed with error trying to create parent directory {} on {} :: ERROR: {}", parentDir, jvm.getHostName(), standardError);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
        }
    }

    private void secureCopyJvmConfigJar(Jvm jvm, String jvmConfigJar, User user) throws CommandFailureException {
        long startTime = System.currentTimeMillis();
        String configTarName = jvm.getJvmName() + ".jar";
        final String scriptsDir = ApplicationProperties.get(PropertyKeys.REMOTE_SCRIPT_DIR);
        String jvmJarFile = ApplicationProperties.get("paths.generated.resource.dir") + File.separator + jvm.getJvmName() + File.separator + configTarName;
        String destination = scriptsDir + "/" + configTarName;
        LOGGER.info("Copy config jar {} to {} ", jvmJarFile, destination);
        final boolean alwaysOverwriteJvmConfigJar = true;
        secureCopyFileToJvm(jvm, jvmJarFile, destination, user, alwaysOverwriteJvmConfigJar);
        LOGGER.info("Copy of config jar successful: {} in {}ms ", jvmConfigJar, System.currentTimeMillis() - startTime);
    }

    private void deployJvmConfigJar(Jvm jvm, User user, String jvmJar) throws CommandFailureException {
        ControlJvmRequest controlJvmRequest = ControlJvmRequestFactory.create(JvmControlOperation.DEPLOY_JVM_ARCHIVE, jvm);
        CommandOutput execData = jvmControlService.controlJvm(controlJvmRequest, user);
        execData.getStandardOutput();
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Deployment of config jar was successful: {}", jvmJar);
        } else {
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error(
                    "Deploy command completed with error trying to extract and back up JVM config {} :: ERROR: {}",
                    jvm.getJvmName(), standardError);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(execData.getReturnCode().getReturnCode()).getDesc() : standardError);
        }

        // make sure the start/stop scripts are executable
        String instancesDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATH_INSTANCES_DIR);
        String tomcatDirName = ApplicationProperties.getRequired(PropertyKeys.REMOTE_TOMCAT_DIR_NAME);

        final String targetAbsoluteDir = instancesDir + '/' + jvm.getJvmName() + '/' + tomcatDirName + "/bin";
        if (!jvmControlService.executeCheckFileExistsCommand(jvm, targetAbsoluteDir).getReturnCode().wasSuccessful()) {
            LOGGER.debug("JVM not generated yet.. ");
        }
        if (!jvmControlService.executeChangeFileModeCommand(jvm, "a+x", targetAbsoluteDir, "*.sh").getReturnCode().wasSuccessful()) {
            String message = "Failed to change the file permissions in " + targetAbsoluteDir + " for jvm " + jvm.getJvmName();
            LOGGER.error(message);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
        }

    }

    private void deployJvmResourceFiles(Jvm jvm, User user) throws IOException, CommandFailureException {
        final Map<String, ScpDestination> generatedFiles = generateResourceFiles(jvm.getJvmName());
        if (generatedFiles != null) {
            for (Map.Entry<String, ScpDestination> entry : generatedFiles.entrySet()) {
                final ScpDestination scpDestination = entry.getValue();
                secureCopyFileToJvm(jvm, entry.getKey(), scpDestination.destPath, user, scpDestination.overwrite);
            }
        }
    }

    private void installJvmWindowsService(Jvm jvm, User user) {
        ControlJvmRequest controlJvmRequest = ControlJvmRequestFactory.create(JvmControlOperation.INSTALL_SERVICE, jvm);
        CommandOutput execData = jvmControlService.controlJvm(controlJvmRequest, user);
        if (execData.getReturnCode().wasSuccessful()) {
            LOGGER.info("Install of windows service {} was successful", jvm.getJvmName());
        } else {
            updateState(jvm.getId(), JvmState.JVM_FAILED);
            String standardError =
                    execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
            LOGGER.error("Installing windows service {} failed :: ERROR: {}", jvm.getJvmName(), standardError);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Installing windows service failed for " + INSTALL_SERVICE_SCRIPT_NAME + ".  Please refer to the history window.");
        }
    }

    /**
     * This method copies a given file to the destination location on the remote machine.
     *
     * @param jvm             Jvm which requires the file
     * @param sourceFile      The source file, which needs to be copied.
     * @param destinationFile The destination file, where the source file should be copied.
     * @param user
     * @throws CommandFailureException If the command fails, this exception contains the details of the failure.
     */
    private void secureCopyFileToJvm(final Jvm jvm, final String sourceFile, final String destinationFile, User user, boolean overwrite) throws CommandFailureException {
        final String parentDir;
        if (destinationFile.startsWith("~")) {
            parentDir = destinationFile.substring(0, destinationFile.lastIndexOf('/'));
        } else {
            parentDir = new File(destinationFile).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        }
        createParentDir(jvm, parentDir);
        final ControlJvmRequest controlJvmRequest = new ControlJvmRequest(jvm.getId(), JvmControlOperation.SCP);
        final CommandOutput commandOutput = jvmControlService.secureCopyFile(controlJvmRequest, sourceFile, destinationFile, user.getId(), overwrite);
        if (commandOutput.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully copied {} to destination location {} on {}", sourceFile, destinationFile, jvm.getHostName());
        } else {
            final String standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
            LOGGER.error("Copy command failed with error trying to copy file {} to {} :: ERROR: {}", sourceFile, jvm.getHostName(), standardError);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError.isEmpty() ? CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc() : standardError);
        }
    }

    @Override
    public Jvm generateAndDeployFile(String jvmName, String fileName, User user) {
        Jvm jvm = getJvm(jvmName);
        // only one at a time per jvm
        binaryDistributionLockManager.writeLock(jvmName + "-" + jvm.getId().getId().toString());
        try {
            if (jvm.getState().isStartedState()) {
                LOGGER.error("The target JVM {} must be stopped before attempting to update the resource files", jvm.getJvmName());
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE,
                        "The target JVM must be stopped before attempting to update the resource files");
            }
            ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                    .setResourceName(fileName)
                    .setJvmName(jvmName)
                    .build();
            resourceService.validateSingleResourceForGeneration(resourceIdentifier);
            resourceService.generateAndDeployFile(resourceIdentifier, jvm.getJvmName(), fileName, jvm.getHostName());
        } finally {
            binaryDistributionLockManager.writeUnlock(jvmName + "-" + jvm.getId().getId().toString());
            LOGGER.debug("End generateAndDeployFile for {} by user {}", jvmName, user.getId());
        }
        return jvm;
    }

    @Override
    @Transactional
    public void performDiagnosis(Identifier<Jvm> aJvmId, final User user) {
        Jvm jvm = jvmPersistenceService.getJvm(aJvmId);
        historyFacadeService.write(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), "Diagnose and resolve state",
                EventType.USER_ACTION_INFO, user.getId());
        final JvmHttpRequestResult jvmHttpRequestResult = pingAndUpdateJvmState(jvm);

        if (StringUtils.isNotEmpty(jvmHttpRequestResult.details)) {
            final EventType eventType = jvmHttpRequestResult.details.isEmpty() || jvmHttpRequestResult.jvmState.equals(JvmState.JVM_STOPPED) ||
                    jvmHttpRequestResult.jvmState.equals(JvmState.FORCED_STOPPED) ? EventType.SYSTEM_INFO :
                    EventType.SYSTEM_ERROR;
            historyFacadeService.write(jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), jvmHttpRequestResult.details,
                    eventType, user.getId());
        }
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     *
     * @param jvm   the jvm
     * @param state {@link JvmState}
     * @param msg   a message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setState(final Jvm jvm,
                          final JvmState state,
                          final String msg) {
        jvmPersistenceService.updateState(jvm.getId(), state, msg);
        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(jvm.getId(), state, DateTime.now(), StateType.JVM));
        groupStateNotificationService.retrieveStateAndSend(jvm.getId(), Jvm.class);
    }

    @Override
    @Transactional
    public String previewResourceTemplate(String fileName, String jvmName, String groupName, String template) {
        return resourceService.generateResourceFile(fileName, template, resourceService.generateResourceGroup(), jvmPersistenceService.findJvm(jvmName, groupName), ResourceGeneratorType.PREVIEW);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmPersistenceService.getResourceTemplateNames(jvmName);
    }

    @Override
    @Transactional
    public String getResourceTemplate(final String jvmName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = jvmPersistenceService.getResourceTemplate(jvmName, resourceTemplateName);
        if (tokensReplaced) {
            return resourceService.generateResourceFile(resourceTemplateName, template, resourceService.generateResourceGroup(), jvmPersistenceService.findJvmByExactName(jvmName), ResourceGeneratorType.TEMPLATE);
        }
        return template;
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        String retVal = null;
        try {
            retVal = jvmPersistenceService.updateResourceTemplate(jvmName, resourceTemplateName, template);
        } catch (ResourceTemplateUpdateException | NonRetrievableResourceTemplateContentException e) {
            LOGGER.error("Failed to update the template {}", resourceTemplateName, e);
        }
        return retVal;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        jvmPersistenceService.updateState(id, state, "");
        messagingTemplate.convertAndSend(topicServerStates, new CurrentState<>(id, state, DateTime.now(), StateType.JVM));
    }

    @Override
    @Transactional
    public JvmHttpRequestResult pingAndUpdateJvmState(final Jvm jvm) {
        ClientHttpResponse response = null;
        JvmState jvmState = jvm.getState();
        String responseDetails = StringUtils.EMPTY;
        try {
            response = clientFactoryHelper.requestGet(jvm.getStatusUri());
            LOGGER.info(">>> Response = {} from jvm {}", response.getStatusCode(), jvm.getId().getId());
            jvmState = JvmState.JVM_STARTED;
            if (response.getStatusCode() == HttpStatus.OK) {
                jvmStateService.updateState(jvm, jvmState, StringUtils.EMPTY);
            } else {
                // As long as we get a response even if it's not a 200 it means that the JVM is alive
                jvmStateService.updateState(jvm, jvmState, StringUtils.EMPTY);
                responseDetails = MessageFormat.format("Request {0} sent expecting a response code of {1} but got {2} instead",
                        jvm.getStatusUri(), HttpStatus.OK.value(), response.getRawStatusCode());

            }
        } catch (final IOException ioe) {
            LOGGER.info(ioe.getMessage(), ioe);
            jvmStateService.updateState(jvm, JvmState.JVM_STOPPED, StringUtils.EMPTY);
            responseDetails = MessageFormat.format("Request {0} sent and got: {1}", jvm.getStatusUri(), ioe.getMessage());
            jvmState = JvmState.JVM_STOPPED;
        } catch (RuntimeException rte) {
            LOGGER.error(rte.getMessage(), rte);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return new JvmHttpRequestResult(jvmState, responseDetails);
    }

    @Override
    @Transactional
    public void deployApplicationContextXMLs(Jvm jvm, User user) {
        List<Group> groupList = jvmPersistenceService.findGroupsByJvm(jvm.getId());
        if (groupList != null) {
            for (Group group : groupList) {
                List<Application> apps = applicationService.findApplications(group.getId());
                if (apps != null) {
                    for (Application app : apps) {
                        for (String templateName : applicationService.getResourceTemplateNames(app.getName(), jvm.getJvmName())) {
                            LOGGER.info("Deploying application xml {} for JVM {} in group {}", templateName, jvm.getJvmName(), group.getName());
                            applicationService.deployConf(app.getName(), group.getName(), jvm.getJvmName(), templateName, resourceService.generateResourceGroup(), user);
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getJvmStartedCount(final String groupName) {
        return jvmPersistenceService.getJvmStartedCount(groupName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getJvmCount(final String groupName) {
        return jvmPersistenceService.getJvmCount(groupName);
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        return jvmPersistenceService.getJvmStoppedCount(groupName);
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        return jvmPersistenceService.getJvmForciblyStoppedCount(groupName);
    }

    private Map<String, ScpDestination> generateResourceFiles(final String jvmName) throws IOException {
        Map<String, ScpDestination> generatedFiles = new HashMap<>();
        final List<JpaJvmConfigTemplate> jpaJvmConfigTemplateList = jvmPersistenceService.getConfigTemplates(jvmName);
        for (final JpaJvmConfigTemplate jpaJvmConfigTemplate : jpaJvmConfigTemplateList) {
            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
            String resourceTemplateMetaDataString = "";
            resourceTemplateMetaDataString = resourceService.generateResourceFile(jpaJvmConfigTemplate.getTemplateName(),
                    jpaJvmConfigTemplate.getMetaData(),
                    resourceGroup,
                    jvm,
                    ResourceGeneratorType.METADATA);
            final ResourceTemplateMetaData resourceTemplateMetaData = resourceService.getMetaData(resourceTemplateMetaDataString);
            final String deployFileName = resourceTemplateMetaData.getDeployFileName();
            if (resourceTemplateMetaData.getContentType().getType().equalsIgnoreCase(MEDIA_TYPE_TEXT) ||
                    MediaType.APPLICATION_XML.equals(resourceTemplateMetaData.getContentType())) {
                final String generatedResourceStr = resourceService.generateResourceFile(jpaJvmConfigTemplate.getTemplateName(), jpaJvmConfigTemplate.getTemplateContent(),
                        resourceGroup, jvm, ResourceGeneratorType.TEMPLATE);
                generatedFiles.put(createConfigFile(ApplicationProperties.get("paths.generated.resource.dir") + '/' + jvmName, deployFileName, generatedResourceStr),
                        new ScpDestination(resourceTemplateMetaData.getDeployPath() + '/' + deployFileName, resourceTemplateMetaData.isOverwrite()));
            } else {
                generatedFiles.put(jpaJvmConfigTemplate.getTemplateContent(),
                        new ScpDestination(resourceTemplateMetaData.getDeployPath() + '/' + deployFileName, resourceTemplateMetaData.isOverwrite()));
            }
        }
        return generatedFiles;
    }

    /**
     * This method creates a temp file .tpl file, with the generatedResourceString as the input data for the file.
     *
     * @param generatedResourcesTempDir
     * @param configFileName            The file name that apprears at the destination.
     * @param generatedResourceString   The contents of the file.
     * @return the location of the newly created temp file
     * @throws IOException
     */
    protected String createConfigFile(String generatedResourcesTempDir, final String configFileName, final String generatedResourceString) throws IOException {
        File templateFile = new File(generatedResourcesTempDir + '/' + configFileName);
        String content = generatedResourceString;
        if (configFileName.endsWith(".bat")) {
            content = generatedResourceString.replaceAll("\n", "\r\n");
        }
        FileUtils.writeStringToFile(templateFile, content);
        return templateFile.getAbsolutePath();
    }

    @Override
    @Transactional
    public void deleteJvm(final String name, final String userName) {
        final Jvm jvm = getJvm(name);
        if (!jvm.getState().isStartedState()) {
            LOGGER.info("Removing JVM from the database and deleting the service for jvm {}", name);
            if (!jvm.getState().equals(JvmState.JVM_NEW)) {
                deleteJvmService(jvm, new User(userName));
            }
            jvmPersistenceService.removeJvm(jvm.getId());
        } else {
            LOGGER.error("The target JVM {} must be stopped before attempting to delete it", jvm.getJvmName());
            throw new JvmServiceException("The target JVM must be stopped before attempting to delete it");
        }
    }

    private class ScpDestination {
        private final boolean overwrite;
        private final String destPath;

        ScpDestination(String destPath, boolean overwrite) {
            this.destPath = destPath;
            this.overwrite = overwrite;
        }
    }
}