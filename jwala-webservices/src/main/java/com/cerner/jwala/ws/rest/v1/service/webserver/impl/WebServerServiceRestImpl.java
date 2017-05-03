package com.cerner.jwala.ws.rest.v1.service.webserver.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.WebServerState;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.CommandOutputReturnCode;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.exception.WebServerServiceException;
import com.cerner.jwala.service.webserver.impl.WebServerServiceImpl;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityExistsException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class WebServerServiceRestImpl implements WebServerServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceRestImpl.class);
    private static final String COMMANDS_SCRIPTS_PATH = ApplicationProperties.get("commands.scripts-path");
    private static final String HTTPD_CONF = "httpd.conf";
    private static final Long DEFAULT_WAIT_TIMEOUT = 30000L;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    private MessagingService simpleMessagingService;

    private final WebServerService webServerService;
    private final WebServerControlService webServerControlService;
    private final WebServerCommandService webServerCommandService;
    private final ResourceService resourceService;
    private final GroupService groupService;
    private final HistoryFacadeService historyFacadeService;
    private final BinaryDistributionService binaryDistributionService;


    public WebServerServiceRestImpl(final WebServerService theWebServerService,
                                    final WebServerControlService theWebServerControlService,
                                    final WebServerCommandService theWebServerCommandService,
                                    final ResourceService theResourceService, GroupService groupService,
                                    final BinaryDistributionService binaryDistributionService,
                                    final HistoryFacadeService historyFacadeService
                                    ) {
        webServerService = theWebServerService;
        webServerControlService = theWebServerControlService;
        webServerCommandService = theWebServerCommandService;
        resourceService = theResourceService;
        this.groupService = groupService;
        this.binaryDistributionService = binaryDistributionService;
        this.historyFacadeService = historyFacadeService;
    }

    @Override
    public Response getWebServers(final Identifier<Group> aGroupId) {
        LOGGER.debug("Get web servers for group {}", aGroupId);
        final List<WebServer> webServers;
        if (aGroupId == null) {
            webServers = webServerService.getWebServers();
            return ResponseBuilder.ok(webServers);
        }
        webServers = webServerService.findWebServers(aGroupId);
        return ResponseBuilder.ok(webServers);
    }

    @Override
    public Response getWebServer(final Identifier<WebServer> aWsId) {
        LOGGER.debug("Get WS requested: {}", aWsId);
        return ResponseBuilder.ok(webServerService.getWebServer(aWsId));
    }

    @Override
    public Response createWebServer(final JsonCreateWebServer aWebServerToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Create WS requested: {} by user {}", aWebServerToCreate, aUser.getUser().getId());
        try {
            final WebServer webServer = webServerService.createWebServer(aWebServerToCreate.toCreateWebServerRequest(), aUser.getUser());
            // Populate the web server templates from the group templates
            Collection<Group> groups = webServer.getGroups();
            if (null != groups && !groups.isEmpty()) {
                Group group = groups.iterator().next();
                final String groupName = group.getName();
                for (final String templateName : groupService.getGroupWebServersResourceTemplateNames(groupName)) {
                    String templateContent = groupService.getGroupWebServerResourceTemplate(groupName, templateName, false, new ResourceGroup());
                    final String metaDataStr = groupService.getGroupWebServerResourceTemplateMetaData(groupName, templateName);
                    webServerService.uploadWebServerConfig(webServer, templateName, templateContent, metaDataStr, groupName, aUser.getUser());

                }
                if (groups.size() > 1) {
                    LOGGER.warn("Multiple groups were associated with the Web Server, but the Web Server was created using the templates from group " + groupName);
                }
            }
            return ResponseBuilder.created(webServer);

        } catch (EntityExistsException eee) {
            LOGGER.error("Web server \"{}\" already exists", aWebServerToCreate, eee);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_WEBSERVER_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response updateWebServer(final JsonUpdateWebServer aWebServerToCreate, final AuthenticatedUser aUser) {
        LOGGER.info("Update WS requested: {} by user {}", aWebServerToCreate, aUser.getUser().getId());
        try {
            return ResponseBuilder.ok(webServerService.updateWebServer(aWebServerToCreate.toUpdateWebServerRequest(),
                    aUser.getUser()));
        } catch (EntityExistsException eee) {
            LOGGER.error("Web server with name \"{}\" already exists", aWebServerToCreate.toUpdateWebServerRequest().getNewName(), eee);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.DUPLICATE_WEBSERVER_NAME, eee.getMessage(), eee));
        }
    }

    @Override
    public Response deleteWebServer(final Identifier<WebServer> id, boolean hardDelete, final AuthenticatedUser authUser) {
        webServerService.deleteWebServer(id, hardDelete, authUser.getUser());
        return Response.noContent().build();
    }

    @Override
    public Response controlWebServer(final Identifier<WebServer> aWebServerId,
                                     final JsonControlWebServer aWebServerToControl, final AuthenticatedUser aUser,
                                     final Boolean wait, Long waitTimeout) {
        LOGGER.debug("Control Web Server requested: {} {} by user {}", aWebServerId, aWebServerToControl, aUser.getUser().getId());
        final ControlWebServerRequest controlWebServerRequest = new ControlWebServerRequest(aWebServerId, aWebServerToControl.toControlOperation());
        final CommandOutput commandOutput = webServerControlService.controlWebServer(
                controlWebServerRequest,
                aUser.getUser());
        if (Boolean.TRUE.equals(wait)) {
            waitTimeout = waitTimeout == null ? DEFAULT_WAIT_TIMEOUT : waitTimeout * 1000; // wait timeout is in seconds converting it to ms
        }
        if (Boolean.TRUE.equals(wait) && commandOutput.getReturnCode().wasSuccessful() && webServerControlService.waitForState(controlWebServerRequest, waitTimeout)) {
            return ResponseBuilder.ok(commandOutput.getStandardOutput());
        } else if (!Boolean.TRUE.equals(wait) && commandOutput.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(commandOutput.getStandardOutput());
        } else {
            final String standardError = commandOutput.getStandardError();
            final String standardOut = commandOutput.getStandardOutput();
            String errMessage = null != standardError && !standardError.isEmpty() ? standardError : standardOut;
            LOGGER.error("Control Operation Unsuccessful: {}", errMessage);
            throw new InternalErrorException(FaultType.CONTROL_OPERATION_UNSUCCESSFUL, CommandOutputReturnCode.fromReturnCode(commandOutput.getReturnCode().getReturnCode()).getDesc());
        }
    }

    @Override
    public Response generateConfig(final String webServerName) {
        LOGGER.info("Generate the httpd.conf for {}", webServerName);
        return Response.ok(webServerService.getResourceTemplate(webServerName, "httpd.conf", true,
                resourceService.generateResourceGroup())).build();
    }

    @Override
    public Response generateAndDeployConfig(final String aWebServerName, final String resourceFileName, final AuthenticatedUser user) {
        LOGGER.info("Generate and deploy config {} for web server {} by user {}", resourceFileName, aWebServerName, user.getUser().getId());
        return ResponseBuilder.ok(webServerService.generateAndDeployFile(aWebServerName, resourceFileName, user.getUser()));
    }

    @Override
    public Response generateAndDeployWebServer(final String aWebServerName, final AuthenticatedUser aUser) {
        LOGGER.info("Generate and deploy web server {} by user {}", aWebServerName, aUser.getUser().getId());
        boolean didSucceed = false;

        // only one at a time per web server
        binaryDistributionLockManager.writeLock(aWebServerName);

        WebServer webServer = webServerService.getWebServer(aWebServerName);

        try {

            historyFacadeService.write(aWebServerName, webServer.getGroups(), "Starting to generate remote web server " + aWebServerName, EventType.USER_ACTION_INFO, aUser.getUser().getId());

            verifyStopped(aWebServerName, webServer);

            validateResources(webServer);

            binaryDistributionService.distributeUnzip(webServer.getHost());

            binaryDistributionService.distributeMedia(webServer.getName(), webServer.getHost(), webServer.getGroups()
                    .toArray(new Group[webServer.getGroups().size()]), webServer.getApacheHttpdMedia());

            // check for httpd.conf template
            validateHttpdConf(webServer);

            // create the remote scripts directory
            createScriptsDirectory(webServer);

            // copy the start and stop scripts
            deployStartStopScripts(webServer, aUser.getUser().getId());

            // delete the service
            deleteWebServerWindowsService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.DELETE_SERVICE), aWebServerName);

            // create the configuration file(s)
            final List<String> templateNames = webServerService.getResourceTemplateNames(aWebServerName);
            for (final String templateName : templateNames) {
                generateAndDeployConfig(aWebServerName, templateName, aUser);
            }

            // re-install the service
            installWebServerService(aUser, new ControlWebServerRequest(webServer.getId(), WebServerControlOperation.INSTALL_SERVICE), webServer);

            webServerService.updateState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);

            simpleMessagingService.send(new WebServerState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE, DateTime.now()));

            didSucceed = true;
        } catch (CommandFailureException e) {
            LOGGER.error("Failed for {}", aWebServerName, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.REMOTE_COMMAND_FAILURE, e.getMessage(), e));
        } finally {
            binaryDistributionLockManager.writeUnlock(aWebServerName);
            final String historyMessage = didSucceed ? "Remote generation of web server " + aWebServerName + " succeeded" :
                    "Remote generation of web server " + aWebServerName + " failed";
            historyFacadeService.write(aWebServerName, new ArrayList<>(webServer.getGroups()), historyMessage, EventType.USER_ACTION_INFO, aUser.getUser().getId());
        }

        return ResponseBuilder.ok(webServerService.getWebServer(aWebServerName));
    }

    private void validateResources(WebServer webServer) {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setWebServerName(webServer.getName())
                .setResourceName("*")
                .build();
        resourceService.validateAllResourcesForGeneration(resourceIdentifier);
    }

    private void verifyStopped(String aWebServerName, WebServer webServer) {
        if (webServerService.isStarted(webServer)) {
            final String errorMessage = "The target Web Server " + aWebServerName + " must be stopped before attempting to update the resource file";
            LOGGER.error(errorMessage);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, errorMessage);
        }
    }

    protected void validateHttpdConf(WebServer webServer) {
        final String aWebServerName = webServer.getName();
        boolean foundHttpdConf = false;
        final List<String> templateNames = webServerService.getResourceTemplateNames(aWebServerName);
        for (final String template : templateNames) {
            if (template.equals(HTTPD_CONF)) {
                foundHttpdConf = true;
                break;
            }
        }

        if (!foundHttpdConf) {
            throw new WebServerServiceException("No template was found for the " + HTTPD_CONF + ". Please upload a template for the httpd.conf and try again.");
        }

    }

    protected void createScriptsDirectory(WebServer webServer) throws CommandFailureException {
        final String remoteScriptsDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
        webServerControlService.createDirectory(webServer, remoteScriptsDir);
    }

    protected void deployStartStopScripts(WebServer webServer, String userId) throws CommandFailureException {
        final String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" +webServer.getName();

        final String webServerName = webServer.getName();
        final String serviceStartScriptName = AemControl.Properties.START_SCRIPT_NAME.getValue();
        final String serviceStartScriptPath = COMMANDS_SCRIPTS_PATH + "/" + serviceStartScriptName;
        final String remoteDestStartScriptPath = remoteScriptDir + "/" + serviceStartScriptName;

        webServerControlService.createDirectory(webServer, remoteScriptDir);
        webServerControlService.secureCopyFile(webServerName, serviceStartScriptPath, remoteDestStartScriptPath, userId);

        final String serviceStopScriptName = AemControl.Properties.STOP_SCRIPT_NAME.getValue();
        final String serviceStopScriptPath = COMMANDS_SCRIPTS_PATH + "/" + serviceStopScriptName;
        final String remoteDestStopScriptPath = remoteScriptDir + "/" + serviceStopScriptName;

        webServerControlService.secureCopyFile(webServerName, serviceStopScriptPath, remoteDestStopScriptPath, userId);

        final String installServiceWsScriptName = AemControl.Properties.INSTALL_WS_SERVICE_SCRIPT_NAME.getValue();
        final String sourceInstallServiceWsServicePath = COMMANDS_SCRIPTS_PATH + "/" + installServiceWsScriptName;
        webServerControlService.secureCopyFile(webServerName, sourceInstallServiceWsServicePath, remoteScriptDir + "/" + installServiceWsScriptName, userId);

        //Delete script
        final String serviceDeleteScriptName = AemControl.Properties.DELETE_SERVICE_SCRIPT_NAME.getValue();
        final String serviceDeleteScriptPath = COMMANDS_SCRIPTS_PATH + "/" + serviceDeleteScriptName;
        final String remoteDestDeleteScriptPath = remoteScriptDir + "/" + serviceDeleteScriptName;

        webServerControlService.secureCopyFile(webServerName, serviceDeleteScriptPath, remoteDestDeleteScriptPath, userId);

        final String installLinuxServiceWsScriptName = "linux/httpd-ws-service.sh";
        final String sourceLinuxInstallServiceWsServicePath = COMMANDS_SCRIPTS_PATH + "/" + installLinuxServiceWsScriptName;
        webServerControlService.createDirectory(webServer, remoteScriptDir+"/linux");
        webServerControlService.secureCopyFile(webServerName, sourceLinuxInstallServiceWsServicePath, remoteScriptDir + "/" + installLinuxServiceWsScriptName, userId);
        //make scripts executable
        webServerControlService.changeFileMode(webServer, "a+x", remoteScriptDir, "*.sh");
        webServerControlService.changeFileMode(webServer, "a+x", remoteScriptDir, "linux/*");

    }

    protected void installWebServerService(final AuthenticatedUser user, final ControlWebServerRequest installServiceRequest, final WebServer webServer) throws CommandFailureException {

        final String remoteScriptDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR) + "/" + webServer.getName();

        // create the install_serviceWS.bat file
        String installServiceScriptText = webServerService.generateInstallServiceScript(webServer);
        final String jwalaGeneratedResourcesDir = ApplicationProperties.get(PropertyKeys.PATHS_GENERATED_RESOURCE_DIR);


        final String name = webServer.getName();
        final File installServiceScript = createTempWebServerResourceFile(name, jwalaGeneratedResourcesDir, WebServerServiceImpl.INSTALL_SERVICE_SCRIPT_NAME, installServiceScriptText);

        // copy the install_serviceWS.bat file
        final String installServiceScriptPath = installServiceScript.getAbsolutePath().replaceAll("\\\\", "/");
        String remoteInstallServiceScriptPath = remoteScriptDir + "/" + WebServerServiceImpl.INSTALL_SERVICE_SCRIPT_NAME;
        webServerControlService.createDirectory(webServer, remoteScriptDir);
        webServerControlService.secureCopyFile(name, installServiceScriptPath, remoteInstallServiceScriptPath, user.getUser().getId());
        webServerControlService.changeFileMode(webServer, "a+x", remoteScriptDir, "*.sh");
        webServerControlService.changeFileMode(webServer, "a+x", remoteScriptDir, "*.bat");
        webServerControlService.changeFileMode(webServer, "a+x", remoteScriptDir, "linux/*");

        // call the install service script file
        CommandOutput installServiceResult = webServerControlService.controlWebServer(installServiceRequest, user.getUser());
        if (installServiceResult.getReturnCode().wasSuccessful()) {
            LOGGER.info("Successfully invoked service {}", name);
        } else {
            final String standardError = installServiceResult.getStandardError();
            LOGGER.error("Failed to create windows service for {} :: Ret Code = {} Std Err = {}", name, installServiceResult.getReturnCode(),
                    StringUtils.isNotEmpty(standardError) ? standardError : installServiceResult.getStandardOutput());
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Failed to create windows service for " + name + ". " + installServiceResult.standardErrorOrStandardOut());
        }
    }

    protected void deleteWebServerWindowsService(AuthenticatedUser user, ControlWebServerRequest controlWebServerRequest, String webServerName) {
        WebServer webServer = webServerService.getWebServer(webServerName);
        if (!webServer.getState().equals(WebServerReachableState.WS_NEW)) {
            CommandOutput commandOutput = webServerControlService.controlWebServer(controlWebServerRequest, user.getUser());
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Delete of service {} was successful", webServerName);
            } else if (ExecReturnCode.JWALA_EXIT_CODE_SERVICE_DOES_NOT_EXIST == commandOutput.getReturnCode().getReturnCode()) {
                LOGGER.info("No such service found for {} during delete. Continuing with request.", webServerName);
            } else {
                String standardError =
                        commandOutput.getStandardError().isEmpty() ?
                                commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Deleting windows service {} failed :: ERROR: {}", webServerName, standardError);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, standardError);
            }
        }
    }

    protected File createTempWebServerResourceFile(String aWebServerName, String httpdDataDir, String fileName, String generatedTemplate) {
        String fileNamePrefix = FilenameUtils.getBaseName(fileName);
        String fileNameSuffix = FilenameUtils.getExtension(fileName);
        PrintWriter out = null;
        final File httpdConfFile =
                new File(httpdDataDir + System.getProperty("file.separator") + aWebServerName + "_" + fileNamePrefix + "."
                        + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Date.from(Instant.now()))+ "." + fileNameSuffix.replace("\\", "/"));
        final String httpdConfAbsolutePath = httpdConfFile.getAbsolutePath().replace("\\", "/");
        try {
            out = new PrintWriter(httpdConfAbsolutePath);
            out.println(generatedTemplate);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to create temporary file {}", httpdConfAbsolutePath);
            throw new InternalErrorException(FaultType.INVALID_PATH, e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return httpdConfFile;
    }

    @Override
    public Response getHttpdConfig(Identifier<WebServer> aWebServerId) {
        LOGGER.debug("Get " + HTTPD_CONF + " for {}", aWebServerId);
        try {
            return Response.ok(webServerCommandService.getHttpdConf(aWebServerId)).build();
        } catch (CommandFailureException cmdFailEx) {
            LOGGER.warn("Request Failure Occurred", cmdFailEx);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.REMOTE_COMMAND_FAILURE, cmdFailEx.getMessage()));
        }
    }

    @Override
    public Response getResourceNames(final String wsName) {
        LOGGER.debug("Get resource names for {}", wsName);
        return ResponseBuilder.ok(webServerService.getResourceTemplateNames(wsName));
    }

    @Override
    public Response getResourceTemplate(final String wsName, final String resourceTemplateName,
                                        final boolean tokensReplaced) {
        LOGGER.debug("Get resource template {} for web server {} : tokens replaced={}", resourceTemplateName, wsName, tokensReplaced);
        return ResponseBuilder.ok(webServerService.getResourceTemplate(wsName, resourceTemplateName, tokensReplaced, resourceService.generateResourceGroup()));
    }

    @Override
    public Response updateResourceTemplate(final String wsName, final String resourceTemplateName, final String content) {
        LOGGER.info("Update the resource template {} for web server {}", resourceTemplateName, wsName);
        LOGGER.debug(content);
        final String someContent = webServerService.updateResourceTemplate(wsName, resourceTemplateName, content);
        if (someContent != null) {
            return ResponseBuilder.ok(someContent);
        } else {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.PERSISTENCE_ERROR, "Failed to update the template " + resourceTemplateName + " for " + wsName + ". See the log for more details."));
        }

    }

    @Override
    public Response previewResourceTemplate(final String webServerName, final String fileName, final String groupName, String template) {
        LOGGER.debug("Preview resource template {} for web server {} in group {}", template, webServerName, groupName);
        try {
            return ResponseBuilder.ok(webServerService.previewResourceTemplate(fileName, webServerName, groupName, template));
        } catch (RuntimeException rte) {
            LOGGER.debug("Error previewing template.", rte);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_TEMPLATE, rte.getMessage()));
        }
    }
}