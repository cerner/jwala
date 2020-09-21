package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.JwalaUtils;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.NameLengthRule;
import com.cerner.jwala.common.rule.group.GroupNameRule;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.exception.GroupServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

public class GroupServiceImpl implements GroupService {

    private final GroupPersistenceService groupPersistenceService;
    private ResourceService resourceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private JvmService jvmService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    public GroupServiceImpl(final GroupPersistenceService groupPersistenceService,
                            final ResourceService resourceService) {
        this.groupPersistenceService = groupPersistenceService;
        this.resourceService = resourceService;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupRequest createGroupRequest,
                             final User aCreatingUser) {
        createGroupRequest.validate();
        try {
            groupPersistenceService.getGroup(createGroupRequest.getGroupName());
            String message = MessageFormat.format("Group Name already exists: {0} ", createGroupRequest.getGroupName());
            LOGGER.error(message);
            throw new EntityExistsException(message);
        } catch (NotFoundException e) {
            LOGGER.debug("No group name conflict, ignoring not found exception for creating group ", e);
        }

        return groupPersistenceService.createGroup(createGroupRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroup(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroupWithWebServers(Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroupWithWebServers(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final String name) {
        return groupPersistenceService.getGroup(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups() {
        return groupPersistenceService.getGroups();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups(final boolean fetchWebServers) {
        return groupPersistenceService.getGroups(fetchWebServers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> findGroups(final String aGroupNameFragment) {
        new MultipleRules(new GroupNameRule(aGroupNameFragment), new NameLengthRule(aGroupNameFragment)).validate();
        return groupPersistenceService.findGroups(aGroupNameFragment);
    }

    @Override
    @Transactional
    public Group updateGroup(final UpdateGroupRequest anUpdateGroupRequest,
                             final User anUpdatingUser) {
        anUpdateGroupRequest.validate();
        Group orginalGroup = getGroup(anUpdateGroupRequest.getId());
        try {
            if (!orginalGroup.getName().equalsIgnoreCase(anUpdateGroupRequest.getNewName()) && null != groupPersistenceService.getGroup(anUpdateGroupRequest.getNewName())) {
                String message = MessageFormat.format("Group Name already exists: {0}", anUpdateGroupRequest.getNewName());
                LOGGER.error(message);
                throw new EntityExistsException(message);
            }
        } catch (NotFoundException e) {
            LOGGER.debug("No group name conflict, ignoring not found exception for creating group ", e);
        }
        return groupPersistenceService.updateGroup(anUpdateGroupRequest);
    }

    @Override
    @Transactional
    public void removeGroup(final Identifier<Group> aGroupId) {
        groupPersistenceService.removeGroup(aGroupId);
    }

    @Override
    @Transactional
    public void removeGroup(final String name) {
        groupPersistenceService.removeGroup(name);
    }

    @Override
    @Transactional
    public Group addJvmToGroup(final AddJvmToGroupRequest addJvmToGroupRequest,
                               final User anAddingUser) {

        addJvmToGroupRequest.validate();
        return groupPersistenceService.addJvmToGroup(addJvmToGroupRequest);
    }

    @Override
    @Transactional
    public Group addJvmsToGroup(final AddJvmsToGroupRequest addJvmsToGroupRequest,
                                final User anAddingUser) {

        addJvmsToGroupRequest.validate();
        for (final AddJvmToGroupRequest command : addJvmsToGroupRequest.toRequests()) {
            addJvmToGroup(command,
                    anAddingUser);
        }

        return getGroup(addJvmsToGroupRequest.getGroupId());
    }

    @Override
    @Transactional
    public Group removeJvmFromGroup(final RemoveJvmFromGroupRequest removeJvmFromGroupRequest,
                                    final User aRemovingUser) {

        removeJvmFromGroupRequest.validate();
        return groupPersistenceService.removeJvmFromGroup(removeJvmFromGroupRequest);
    }

    @Override
    @Transactional
    public List<Jvm> getOtherGroupingDetailsOfJvms(Identifier<Group> id) {
        final List<Jvm> otherGroupConnectionDetails = new LinkedList<>();
        final Group group = groupPersistenceService.getGroup(id, false);
        final Set<Jvm> jvms = group.getJvms();

        for (Jvm jvm : jvms) {
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (jvm.getGroups() != null && !jvm.getGroups().isEmpty()) {
                for (Group liteGroup : jvm.getGroups()) {
                    if (!id.getId().equals(liteGroup.getId().getId())) {
                        tmpGroup.add(liteGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new Jvm(jvm.getId(), jvm.getJvmName(), tmpGroup));
                }
            }
        }
        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public List<WebServer> getOtherGroupingDetailsOfWebServers(Identifier<Group> id) {
        final List<WebServer> otherGroupConnectionDetails = new ArrayList<>();
        final Group group = groupPersistenceService.getGroup(id, true);
        final Set<WebServer> webServers = group.getWebServers();

        for (WebServer webServer : webServers) {
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (webServer.getGroups() != null && !webServer.getGroups().isEmpty()) {
                for (Group webServerGroup : webServer.getGroups()) {
                    if (!id.getId().equals(webServerGroup.getId().getId())) {
                        tmpGroup.add(webServerGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new WebServer(webServer.getId(),
                            webServer.getGroups(),
                            webServer.getName()));
                }
            }
        }

        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateRequests, User user, boolean overwriteExisting) {
        return groupPersistenceService.populateJvmConfig(aGroupId, uploadJvmTemplateRequests, user, overwriteExisting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupJvmsResourceTemplateNames(String groupName) {
        List<String> retVal = new ArrayList<>();
        final List<String> groupJvmsResourceTemplateNames = groupPersistenceService.getGroupJvmsResourceTemplateNames(groupName);
        for (String jvmResourceName : groupJvmsResourceTemplateNames) {
            retVal.add(jvmResourceName);
        }
        return retVal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupWebServersResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupWebServersResourceTemplateNames(groupName);
    }

    @Override
    @Transactional
    public String getGroupJvmResourceTemplate(final String groupName,
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
    public String getGroupJvmResourceTemplateMetaData(String groupName, String fileName) {
        return groupPersistenceService.getGroupJvmResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    @Transactional
    public String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content);
    }

    @Override
    @Transactional
    public String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content);
    }

    @Override
    @Transactional
    public String previewGroupWebServerResourceTemplate(String fileName, String groupName, String template, ResourceGroup resourceGroup) {
        final Group group = groupPersistenceService.getGroup(groupName);
        Set<WebServer> webservers = groupPersistenceService.getGroupWithWebServers(group.getId()).getWebServers();
        if (webservers != null && !webservers.isEmpty()) {
            final WebServer webServer = webservers.iterator().next();
            return resourceService.generateResourceFile(fileName, template, resourceGroup, webServer, ResourceGeneratorType.PREVIEW);
        }
        return template;
    }

    @Override
    @Transactional
    public String getGroupWebServerResourceTemplate(final String groupName,
                                                    final String resourceTemplateName,
                                                    final boolean tokensReplaced,
                                                    final ResourceGroup resourceGroup) {
        final String template = groupPersistenceService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName);
        if (tokensReplaced) {
            final Group group = groupPersistenceService.getGroup(groupName);
            Set<WebServer> webservers = groupPersistenceService.getGroupWithWebServers(group.getId()).getWebServers();
            if (webservers != null && !webservers.isEmpty()) {
                final WebServer webServer = webservers.iterator().next();
                return resourceService.generateResourceFile(resourceTemplateName, template, resourceGroup, webServer, ResourceGeneratorType.TEMPLATE);
            }
        }
        return template;
    }

    @Override
    public String getGroupWebServerResourceTemplateMetaData(String groupName, String fileName) {
        return groupPersistenceService.getGroupWebServerResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupAppsResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupAppsResourceTemplateNames(groupName);
    }

    @Override
    public List<String> getGroupAppsResourceTemplateNames(String groupName, String appName) {
        return groupPersistenceService.getGroupAppsResourceTemplateNames(groupName, appName);
    }

    @Override
    @Transactional
    public String updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content);
    }

    @Override
    @Transactional
    public String previewGroupAppResourceTemplate(String groupName, String resourceTemplateName, String template, ResourceGroup resourceGroup, String appName) {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
        Jvm jvm = jvms != null && !jvms.isEmpty() ? jvms.iterator().next() : null;
        try {
            Application app = applicationService.getApplication(appName);
            app.setParentJvm(jvm);
            return resourceService.generateResourceFile(resourceTemplateName, template, resourceGroup, app,
                    ResourceGeneratorType.PREVIEW);
        } catch (ResourceFileGeneratorException resourceFileGeneratorException) {
            throw resourceFileGeneratorException;
        } catch (Exception exception) {
            LOGGER.error("Failed to generate preview for template {} in  group {}", resourceTemplateName, groupName,
                    exception);
            throw new ApplicationException("Template token replacement failed.", exception);
        }
    }

    @Override
    public String getGroupAppResourceTemplateMetaData(String groupName, String fileName, String appName) {
        return groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, fileName, appName);
    }

    @Override
    @Transactional
    public String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, boolean tokensReplaced, ResourceGroup resourceGroup) {
        final String template = groupPersistenceService.getGroupAppResourceTemplate(groupName, appName, resourceTemplateName);
        if (tokensReplaced) {
            try {
                Application app = applicationService.getApplication(appName);
                return resourceService.generateResourceFile(resourceTemplateName, template, resourceGroup, app, ResourceGeneratorType.TEMPLATE);
            } catch (ResourceFileGeneratorException rfge) {
                LOGGER.error("Failed to generate and deploy file {} to Web App {}", resourceTemplateName, appName, rfge);
                Map<String, List<String>> errorDetails = new HashMap<>();
                errorDetails.put(appName, Collections.singletonList(rfge.getMessage()));
                throw new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Failed to generate and deploy file " + resourceTemplateName + " to Web App " + appName, null, errorDetails);
            } catch (Exception x) {
                LOGGER.error("Failed to tokenize template {} in group {}", resourceTemplateName, groupName, x);
                throw new ApplicationException("Template token replacement failed.", x);
            }
        }
        return template;
    }

    @Override
    public CommandOutput deployGroupAppTemplate(String groupName, String fileName, Application application, Jvm jvm) {
        return executeDeployGroupAppTemplate(groupName, fileName, application, jvm.getHostName());
    }

    @Override
    public CommandOutput deployGroupAppTemplate(String groupName, String fileName, Application application, String hostName) {
        return executeDeployGroupAppTemplate(groupName, fileName, application, hostName);
    }

    /**
     * This method executes all the commands for copying the template over to the destination for a group app config file
     *
     * @param groupName   name of the group in which the application can be found
     * @param fileName    name of the file that needs to deployed
     * @param application the application object for the application to deploy the config file too
     * @param hostName    name of the host which needs the application file
     * @return returns a command output object
     */
    protected CommandOutput executeDeployGroupAppTemplate(final String groupName, final String fileName, final Application application, final String hostName) {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName(fileName)
                .setGroupName(groupName)
                .setWebAppName(application.getName())
                .build();
        String lockKey = groupName + application.getName() + fileName + JwalaUtils.getHostAddress(hostName);
        try{
            binaryDistributionLockManager.writeLock(lockKey);
            return resourceService.generateAndDeployFile(resourceIdentifier, application.getName(), fileName, hostName);
        }finally {
            binaryDistributionLockManager.writeUnlock(lockKey);
        }
    }

    @Override
    public List<String> getHosts(final String groupName) {
        return groupPersistenceService.getHosts(groupName);
    }

    @Override
    public List<String> getAllHosts() {
        Set<String> allHosts = new TreeSet<>();
        for (Group group : groupPersistenceService.getGroups()) {
            allHosts.addAll(groupPersistenceService.getHosts(group.getName()));
        }
        return new ArrayList<>(allHosts);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Group generateAndDeployGroupJvmFile(final String groupName, final String fileName, final User user) {
        final Group group = groupPersistenceService.getGroup(groupName);
        final List<Jvm> jvms = jvmService.getJvmsByGroupName(group.getName());

        checkJvmStatesBeforeDeployFile(groupName, fileName, jvms);


        final Map<String, Future<Jvm>> futures = new HashMap<>(jvms.size());
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String template = groupPersistenceService.getGroupJvmResourceTemplate(groupName, fileName);
        final String metaData = groupPersistenceService.getGroupJvmResourceTemplateMetaData(groupName, fileName);
        jvms.stream().forEach(jvm -> {
            final Future<Jvm> future = (Future) executorService.submit(() -> {
                SecurityContextHolder.getContext().setAuthentication(auth);
                jvmService.updateResourceTemplate(jvm.getJvmName(), fileName, template);
                ResourceIdentifier resourceId = new ResourceIdentifier.Builder().setResourceName(fileName)
                        .setGroupName(groupName).setJvmName(jvm.getJvmName()).build();
                resourceService.updateResourceMetaData(resourceId, fileName, metaData);
                return jvmService.generateAndDeployFile(jvm.getJvmName(), fileName, user);
            });
            futures.put(jvm.getJvmName(), future);
        });

        checkJvmGenerateAndDeployFutureErrorStatus(futures);
        return group;
    }

    private void checkJvmStatesBeforeDeployFile(String groupName, String fileName, List<Jvm> jvms) {
        // Check if any JVMs are running before generating the file
        final List<Jvm> startedJvmNameList = new ArrayList<>();
        final List<String> startedAndNotHotDeployList = new ArrayList<>();
        jvms.stream().filter(jvm -> jvm.getState().isStartedState()).forEach(startedJvm -> startedJvmNameList.add(startedJvm));

        for (Jvm jvm : startedJvmNameList) {
            try {
                String metaDataStr = resourceService.getResourceContent(
                        new ResourceIdentifier.Builder()
                                .setResourceName(fileName)
                                .setJvmName(jvm.getJvmName())
                                .setGroupName(groupName)
                                .build()).getMetaData();
                ResourceTemplateMetaData metaData = resourceService.getTokenizedMetaData(fileName, jvm, metaDataStr);
                if (!metaData.isHotDeploy()) {
                    startedAndNotHotDeployList.add(jvm.getJvmName());
                    continue;
                }
                LOGGER.info("JVM {} is started but resource {} is configured for hot deploy. Continuing with deploy ...", jvm.getJvmName(), fileName);
            } catch (IOException e) {
                String errMsg = MessageFormat.format("Failed to tokenize meta data for resource {0} of JVM {1}", fileName, jvm.getJvmName());
                LOGGER.error(errMsg, e);
                throw new GroupServiceException(errMsg);
            }
        }

        if (!startedAndNotHotDeployList.isEmpty()) {
            final String errMsg = MessageFormat.format("Failed to deploy file {0} for group {1} since the following JVMs are running and the file is not configured for hot deploy: {2}",
                    fileName, groupName, startedAndNotHotDeployList);
            LOGGER.error(errMsg);
            throw new GroupServiceException(errMsg);
        }
    }

    /**
     * Check for future error statuses of all jvms
     *
     * @param futureMap a map of all jvm keys and their Future object values
     */
    private void checkJvmGenerateAndDeployFutureErrorStatus(final Map<String, Future<Jvm>> futureMap) {
        final Map<String, List<String>> errorMap = new HashMap<>(futureMap.size());
        final long timeout = Long.parseLong(ApplicationProperties.get("remote.jwala.execution.timeout.seconds", "600"));

        for (final String key : futureMap.keySet()) {
            try {
                futureMap.get(key).get(timeout, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error("Remote Command Failure for {}!", key, e);
                final Throwable cause = e.getCause();
                if (cause instanceof InternalErrorException) {
                    if (((InternalErrorException) cause).getErrorDetails() != null) {
                        errorMap.putAll(((InternalErrorException) cause).getErrorDetails());
                    } else {
                        errorMap.put(key, Collections.singletonList(cause.getMessage()));
                    }
                } else {
                    errorMap.put(key, Collections.singletonList(e.getMessage()));
                }
            }
        }

        if (!errorMap.isEmpty()) {
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Request failed for the following errors:",
                    null, errorMap);
        }

        LOGGER.info("Finished checking requests for error statuses.");
    }

}
