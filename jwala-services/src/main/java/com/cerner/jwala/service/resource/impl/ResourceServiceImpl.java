package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.common.JwalaUtils;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.*;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.ExternalProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.*;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.binarydistribution.DistributionService;
import com.cerner.jwala.service.exception.ResourceServiceException;
import com.cerner.jwala.service.repository.RepositoryService;
import com.cerner.jwala.service.repository.RepositoryServiceException;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import opennlp.tools.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

public class ResourceServiceImpl implements ResourceService {


    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);
    private static final String WAR_FILE_EXTENSION = ".war";
    private static final String MEDIA_TYPE_TEXT = "text";

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private BinaryDistributionControlService distributionControlService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    private HistoryFacadeService historyFacadeService;

    private final ResourcePersistenceService resourcePersistenceService;

    private final GroupPersistenceService groupPersistenceService;

    private final ApplicationPersistenceService applicationPersistenceService;

    private final JvmPersistenceService jvmPersistenceService;

    private final WebServerPersistenceService webServerPersistenceService;

    private final ResourceDao resourceDao;

    private final ResourceHandler resourceHandler;

    private final ResourceContentGeneratorService resourceContentGeneratorService;

    private final BinaryDistributionService binaryDistributionService;

    private final Tika fileTypeDetector;

    private final RepositoryService repositoryService;

    public ResourceServiceImpl(final ResourcePersistenceService resourcePersistenceService,
                               final GroupPersistenceService groupPersistenceService,
                               final ApplicationPersistenceService applicationPersistenceService,
                               final JvmPersistenceService jvmPersistenceService,
                               final WebServerPersistenceService webServerPersistenceService,
                               final ResourceDao resourceDao,
                               final ResourceHandler resourceHandler,
                               final ResourceContentGeneratorService resourceContentGeneratorService,
                               final BinaryDistributionService binaryDistributionService,
                               final Tika fileTypeDetector,
                               final RepositoryService repositoryService) {
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.resourceDao = resourceDao;
        this.resourceHandler = resourceHandler;
        this.resourceContentGeneratorService = resourceContentGeneratorService;
        this.binaryDistributionService = binaryDistributionService;
        this.fileTypeDetector = fileTypeDetector;
        this.repositoryService = repositoryService;
    }

    @Override
    public String encryptUsingPlatformBean(String cleartext) {
        SpelExpressionParser expressionParser = new SpelExpressionParser();
        String encryptExpressionString = ApplicationProperties.get("encryptExpression");
        Expression encryptExpression = expressionParser.parseExpression(encryptExpressionString);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", cleartext);
        return encryptExpression.getValue(context, String.class);
    }

    @Override
    @Transactional
    public CreateResourceResponseWrapper createTemplate(final InputStream metaData,
                                                        final InputStream templateData,
                                                        String targetName,
                                                        final User user) {
        final ResourceTemplateMetaData resourceTemplateMetaData;
        final CreateResourceResponseWrapper responseWrapper;
        String templateContent;

        try {

            // This legacy method required the user to specify the content type in the JSON meta data but
            // since we now have mime type detection capability, it would be prudent to use it here also.
            // I don't like to make ResourceTemplateMetaData contentType property mutable just to accommodate
            // this legacy method's short comings therefore we do it by converting the JSON string to a map,
            // change the content type then convert it back again to String. In addition the ResourceTemplateMetaData
            // keeps a copy of the JSON String so the JSON String should match the property values.
            final ObjectMapper objectMapper = new ObjectMapper();
            final String jsonStr = IOUtils.toString(metaData, Charset.defaultCharset());
            final HashMap<String, Object> jsonMap = objectMapper.readValue(jsonStr, HashMap.class);
            // Read input stream into a byte array and use the byte array going forward so we don't need to deal
            // with resetting the input stream
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(templateData);
            jsonMap.put("contentType", getResourceMimeType(bufferedInputStream));

            resourceTemplateMetaData = getMetaData(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap));
            if (MEDIA_TYPE_TEXT.equalsIgnoreCase(resourceTemplateMetaData.getContentType().getType()) ||
                    MediaType.APPLICATION_XML.equals(resourceTemplateMetaData.getContentType())) {
                Scanner scanner = new Scanner(bufferedInputStream).useDelimiter("\\A");
                templateContent = scanner.hasNext() ? scanner.next() : "";
            } else {
                templateContent = uploadResource(resourceTemplateMetaData, bufferedInputStream);
            }

            if (StringUtil.isEmpty(targetName)) {
                if (null == resourceTemplateMetaData.getEntity()) {
                    String errMsg = MessageFormat.format("No entity data found in the meta data when attempting to upload {0}. Unable to continue with resource upload.", resourceTemplateMetaData.getDeployFileName());
                    LOGGER.error(errMsg);
                    throw new ResourceServiceException(errMsg);
                }
                targetName = resourceTemplateMetaData.getEntity().getTarget();
            }

            // Let's create the template!
            final EntityType entityType = EntityType.fromValue(resourceTemplateMetaData.getEntity().getType());
            switch (entityType) {
                case JVM:
                    responseWrapper = createJvmTemplate(resourceTemplateMetaData, templateContent, targetName);
                    break;
                case GROUPED_JVMS:
                    responseWrapper = createGroupedJvmsTemplate(resourceTemplateMetaData, templateContent);
                    break;
                case WEB_SERVER:
                    responseWrapper = createWebServerTemplate(resourceTemplateMetaData, templateContent, targetName, user);
                    break;
                case GROUPED_WEBSERVERS:
                    responseWrapper = createGroupedWebServersTemplate(resourceTemplateMetaData, templateContent, user);
                    break;
                case APP:
                    responseWrapper = createApplicationTemplate(resourceTemplateMetaData, templateContent, targetName, resourceTemplateMetaData.getEntity().getParentName());
                    break;
                case GROUPED_APPS:
                    responseWrapper = createGroupedApplicationsTemplate(resourceTemplateMetaData, templateContent, targetName);
                    break;
                default:
                    String errMsg = MessageFormat.format("Invalid entity type: {0}", resourceTemplateMetaData.getEntity().getType());
                    LOGGER.error(errMsg);
                    throw new ResourceServiceException(errMsg);
            }
        } catch (final IOException ioe) {
            LOGGER.error("Error creating template for target {}", targetName, ioe);
            throw new ResourceServiceException(ioe);
        }

        return responseWrapper;
    }

    @Override
    public ResourceGroup generateResourceGroup() {
        List<Group> groups = groupPersistenceService.getGroups();
        List<Group> groupsToBeAdded = new ArrayList<>(groups.size());
        for (Group group : groups) {
            List<Jvm> jvms = jvmPersistenceService.getJvmsAndWebAppsByGroupName(group.getName());
            List<WebServer> webServers = webServerPersistenceService.getWebServersByGroupName(group.getName());
            List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(group.getName());
            groupsToBeAdded.add(new Group(group.getId(),
                    group.getName(),
                    null != jvms ? new LinkedHashSet<>(jvms) : new LinkedHashSet<Jvm>(),
                    null != webServers ? new LinkedHashSet<>(webServers) : new LinkedHashSet<WebServer>(),
                    group.getHistory(),
                    null != applications ? new LinkedHashSet<>(applications) : new LinkedHashSet<Application>()));
        }
        return new ResourceGroup(groupsToBeAdded);
    }

    @Override
    public <T> String generateResourceFile(final String fileName, final String template, final ResourceGroup resourceGroup, T selectedValue, ResourceGeneratorType resourceGeneratorType) {
        return resourceContentGeneratorService.generateContent(fileName, template, resourceGroup, selectedValue, resourceGeneratorType);
    }

    @Override
    public void validateAllResourcesForGeneration(ResourceIdentifier resourceIdentifier) {
        List<String> exceptionList = new ArrayList<>();
        List<String> resourceNames = resourceHandler.getResourceNames(resourceIdentifier);
        Object entity = resourceHandler.getSelectedValue(resourceIdentifier);
        final ResourceGroup resourceGroup = generateResourceGroup();
        for (String resourceName : resourceNames) {
            ResourceIdentifier resourceIdentifierWithResource = new ResourceIdentifier.Builder()
                    .setResourceName(resourceName)
                    .setGroupName(resourceIdentifier.groupName)
                    .setJvmName(resourceIdentifier.jvmName)
                    .setWebAppName(resourceIdentifier.webAppName)
                    .setWebServerName(resourceIdentifier.webServerName)
                    .build();
            ResourceContent resourceContent = getResourceContent(resourceIdentifierWithResource);

            try {
                if (entity instanceof Application && getMetaData(resourceContent.getMetaData()).getEntity().getDeployToJvms()) {
                    LOGGER.info("Skipping application resource validation for {} because deployToJvms=true", resourceName);
                    continue;
                }
            } catch (IOException e) {
                throw new ApplicationException(MessageFormat.format("Unable to retrieve meta data for {0} during validation step.", resourceName), e);
            }

            try {
                generateResourceFile(resourceName, resourceContent.getMetaData(), resourceGroup, entity, ResourceGeneratorType.METADATA);
            } catch (ResourceFileGeneratorException e) {
                LOGGER.error("Failed to generate {} {} for {}", resourceName, ResourceGeneratorType.METADATA, entity, e);
                exceptionList.add(e.getMessage());
            }

            try {
                generateResourceFile(resourceName, resourceContent.getContent(), resourceGroup, entity, ResourceGeneratorType.TEMPLATE);
            } catch (ResourceFileGeneratorException e) {
                LOGGER.error("Failed to generate {} {} for {}", resourceName, ResourceGeneratorType.TEMPLATE, entity, e);
                exceptionList.add(e.getMessage());
            }
        }

        checkForResourceGenerationException(resourceIdentifier, exceptionList, entity);
    }

    @Override
    public void validateSingleResourceForGeneration(ResourceIdentifier resourceIdentifier) {
        List<String> exceptionList = new ArrayList<>();
        ConfigTemplate resource = resourceHandler.fetchResource(resourceIdentifier);
        Object entity = resourceHandler.getSelectedValue(resourceIdentifier);
        final ResourceGroup resourceGroup = generateResourceGroup();

        try {
            generateResourceFile(resourceIdentifier.resourceName, resource.getMetaData(), resourceGroup, entity, ResourceGeneratorType.METADATA);
        } catch (ResourceFileGeneratorException e) {
            LOGGER.error("Failed to generate {} {} for {}", resourceIdentifier.resourceName, ResourceGeneratorType.METADATA, entity, e);
            exceptionList.add(e.getMessage());
        }

        try {
            generateResourceFile(resourceIdentifier.resourceName, resource.getTemplateContent(), resourceGroup, entity, ResourceGeneratorType.TEMPLATE);
        } catch (ResourceFileGeneratorException e) {
            LOGGER.error("Failed to generate {} {} for {}", resourceIdentifier.resourceName, ResourceGeneratorType.TEMPLATE, entity, e);
            exceptionList.add(e.getMessage());
        }

        checkForResourceGenerationException(resourceIdentifier, exceptionList, entity);
    }

    private void checkForResourceGenerationException(ResourceIdentifier resourceIdentifier, List<String> exceptionList, Object entity) {
        if (!exceptionList.isEmpty()) {
            final String resourceName = resourceIdentifier.jvmName != null ? resourceIdentifier.jvmName : resourceIdentifier.webServerName != null ? resourceIdentifier.webServerName : resourceIdentifier.webAppName;
            Map<String, List<String>> resourceExceptionMap = new HashMap<>();
            resourceExceptionMap.put(resourceName, exceptionList);
            throw new InternalErrorException(FaultType.RESOURCE_GENERATION_FAILED, "Failed to validate the following resources.", null, resourceExceptionMap);
        } else {
            LOGGER.info("Resources passed validation for {}", entity);
        }
    }

    @Override
    public <T> ResourceTemplateMetaData getTokenizedMetaData(String fileName, T entity, String metaDataStr) throws IOException {
        String tokenizedMetaData = generateResourceFile(fileName, metaDataStr, generateResourceGroup(), entity, ResourceGeneratorType.METADATA);
        LOGGER.info("tokenized metadata is : {}", tokenizedMetaData);
        return getMetaData(tokenizedMetaData);
    }

    @Override
    public ResourceTemplateMetaData getMetaData(final String jsonMetaData) throws IOException {
        if (StringUtils.isNotEmpty(jsonMetaData)) {
            final ResourceTemplateMetaData metaData = new ObjectMapper().readValue(jsonMetaData.replace("\\", "\\\\"),
                    ResourceTemplateMetaData.class);
            metaData.setJsonData(jsonMetaData);
            return metaData;
        }
        return null;
    }

    @Override
    public List<String> getApplicationResourceNames(final String groupName, final String appName) {
        return resourcePersistenceService.getApplicationResourceNames(groupName, appName);
    }

    @Override
    public String getAppTemplate(final String groupName, final String appName, final String templateName) {
        return resourcePersistenceService.getAppTemplate(groupName, appName, templateName);
    }

    @Override
    public Map<String, String> checkFileExists(final String groupName, final String jvmName, final String webappName, final String webserverName, final String fileName) {
        boolean resultBoolean = false;
        if (StringUtils.isNotEmpty(groupName) && StringUtils.isNoneEmpty(fileName)) {
            if (jvmName != null && !jvmName.isEmpty()) {
                // Search for file in jvms
                LOGGER.debug("Searching for resource {} in group {} and jvm {}", fileName, groupName, jvmName);
                resultBoolean = groupPersistenceService.checkGroupJvmResourceFileName(groupName, fileName) ||
                        jvmPersistenceService.checkJvmResourceFileName(groupName, jvmName, fileName);
            } else if (webappName != null && !webappName.isEmpty()) {
                // Search for file in webapps
                LOGGER.debug("Searching for resource {} in group {} and webapp {}", fileName, groupName, webappName);
                resultBoolean = groupPersistenceService.checkGroupAppResourceFileName(groupName, fileName) ||
                        applicationPersistenceService.checkAppResourceFileName(groupName, webappName, fileName);
            } else if (webserverName != null && !webserverName.isEmpty()) {
                // Search for file in webservers
                LOGGER.debug("Searching for resource {} in group {} and webserver {}", fileName, groupName, webserverName);
                resultBoolean = groupPersistenceService.checkGroupWebServerResourceFileName(groupName, fileName) ||
                        webServerPersistenceService.checkWebServerResourceFileName(groupName, webserverName, fileName);
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("exists", Boolean.toString(resultBoolean));
        LOGGER.debug("result: {}", result.toString());
        return result;
    }

    @Override
    public boolean checkJvmFileExists(String groupName, String jvmName, String fileName) {
        LOGGER.debug("Searching for resource {} in group {} and jvm {}", fileName, groupName, jvmName);
        return groupPersistenceService.checkGroupJvmResourceFileName(groupName, fileName) ||
                jvmPersistenceService.checkJvmResourceFileName(groupName, jvmName, fileName);
    }

    @Override
    @Transactional
    public int deleteWebServerResource(final String templateName, final String webServerName) {
        return resourceDao.deleteWebServerResource(templateName, webServerName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelWebServerResource(final String templateName, final String groupName) {
        return resourceDao.deleteGroupLevelWebServerResource(templateName, groupName);
    }

    @Override
    @Transactional
    public int deleteJvmResource(final String templateName, final String jvmName) {
        return resourceDao.deleteJvmResource(templateName, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelJvmResource(final String templateName, final String groupName) {
        return resourceDao.deleteGroupLevelJvmResource(templateName, groupName);
    }

    @Override
    @Transactional
    public int deleteAppResource(final String templateName, final String appName, final String jvmName) {
        return resourceDao.deleteAppResource(templateName, appName, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelAppResource(final String appName, final String templateName) {
        final Application application = applicationPersistenceService.getApplication(appName);
        return resourceDao.deleteGroupLevelAppResource(application.getName(), application.getGroup().getName(), templateName);
    }


    /****** OLD CREATE RESOURCE PRIVATE METHODS TO BE REMOVED AFTER THE NEW CREATE RESOURCE HAS BEEN IMPLEMENTED ******/

    /**
     * Create the JVM template in the db and in the templates path for a specific JVM entity target.
     *
     * @param metaData        the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateContent the template content/data
     * @param jvmName         identifies the JVM to which the template is attached to
     */
    @Deprecated
    private CreateResourceResponseWrapper createJvmTemplate(final ResourceTemplateMetaData metaData,
                                                            final String templateContent,
                                                            final String jvmName) {
        final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
        final Jvm jvmWithParentGroup = new Jvm(jvm.getId(),
                jvm.getJvmName(),
                jvm.getHostName(),
                jvm.getGroups(),
                jvm.getHttpPort(),
                jvm.getHttpsPort(),
                jvm.getRedirectPort(),
                jvm.getShutdownPort(),
                jvm.getAjpPort(),
                jvm.getStatusPath(),
                jvm.getSystemProperties(),
                jvm.getState(),
                jvm.getErrorStatus(),
                jvm.getLastUpdatedDate(),
                jvm.getUserName(),
                jvm.getEncryptedPassword(),
                jvm.getJdkMedia(),
                jvm.getTomcatMedia(),
                jvm.getJavaHome(),
                jvm.getWebApps());

        final UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvmWithParentGroup, metaData.getTemplateName(),
                templateContent, metaData.getJsonData());
        uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
        return new CreateResourceResponseWrapper(jvmPersistenceService.uploadJvmConfigTemplate(uploadJvmTemplateRequest));
    }

    /**
     * Create the JVM template in the db and in the templates path for all the JVMs.
     *
     * @param metaData        the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateContent the template content/data
     */
    // TODO: When the resource file is locked, don't overwrite!
    @Deprecated
    private CreateResourceResponseWrapper createGroupedJvmsTemplate(final ResourceTemplateMetaData metaData,
                                                                    final String templateContent) throws IOException {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(metaData.getEntity().getGroup()).getJvms();
        ConfigTemplate createdJpaJvmConfigTemplate = null;
        final String deployFileName = metaData.getDeployFileName();

        for (final Jvm jvm : jvms) {
            UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvm, metaData.getTemplateName(),
                    templateContent, metaData.getJsonData());
            uploadJvmTemplateRequest.setConfFileName(deployFileName);

            // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
            // configuration template.
            createdJpaJvmConfigTemplate = jvmPersistenceService.uploadJvmConfigTemplate(uploadJvmTemplateRequest);
        }
        final List<UploadJvmTemplateRequest> uploadJvmTemplateRequestList = new ArrayList<>();
        UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(null, metaData.getTemplateName(),
                templateContent, metaData.getJsonData());
        uploadJvmTemplateRequest.setConfFileName(deployFileName);
        uploadJvmTemplateRequestList.add(uploadJvmTemplateRequest);
        groupPersistenceService.populateGroupJvmTemplates(metaData.getEntity().getGroup(), uploadJvmTemplateRequestList);
        return new CreateResourceResponseWrapper(createdJpaJvmConfigTemplate);
    }

    /**
     * Create the web server template in the db and in the templates path for a specific web server entity target.
     *
     * @param metaData        the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateContent the template content/data
     * @param webServerName   identifies the web server to which the template belongs to
     * @param user
     */
    @Deprecated
    private CreateResourceResponseWrapper createWebServerTemplate(final ResourceTemplateMetaData metaData,
                                                                  final String templateContent,
                                                                  final String webServerName,
                                                                  final User user) {
        final WebServer webServer = webServerPersistenceService.findWebServerByName(webServerName);
        final String deployFileName = metaData.getDeployFileName();
        final UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                metaData.getTemplateName(), metaData.getJsonData(), templateContent) {
            @Override
            public String getConfFileName() {
                return deployFileName;
            }
        };
        String generatedDeployPath = generateResourceFile(metaData.getDeployFileName(), metaData.getDeployPath(), generateResourceGroup(), webServer, ResourceGeneratorType.METADATA);
        return new CreateResourceResponseWrapper(webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebArchiveRequest, generatedDeployPath + "/" + deployFileName, user.getId()));
    }

    /**
     * Create the web server template in the db and in the templates path for all the web servers.
     *
     * @param metaData        the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateContent the template content/data
     * @param user
     */
    @Deprecated
    private CreateResourceResponseWrapper createGroupedWebServersTemplate(final ResourceTemplateMetaData metaData,
                                                                          final String templateContent,
                                                                          final User user) throws IOException {
        final Group group = groupPersistenceService.getGroupWithWebServers(metaData.getEntity().getGroup());
        final Set<WebServer> webServers = group.getWebServers();
        final Map<String, UploadWebServerTemplateRequest> uploadWebServerTemplateRequestMap = new HashMap<>();
        ConfigTemplate createdConfigTemplate = null;
        final String deployFileName = metaData.getDeployFileName();
        for (final WebServer webServer : webServers) {

            UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                    metaData.getTemplateName(), metaData.getJsonData(), templateContent) {
                @Override
                public String getConfFileName() {
                    return deployFileName;
                }
            };

            // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
            // configuration template.
            String generatedDeployPath = generateResourceFile(metaData.getDeployFileName(), metaData.getDeployPath(), generateResourceGroup(), webServer, ResourceGeneratorType.METADATA);
            createdConfigTemplate = webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebServerTemplateRequest, generatedDeployPath + "/" + deployFileName, user.getId());
        }

        UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(null,
                metaData.getTemplateName(), metaData.getJsonData(), templateContent) {
            @Override
            public String getConfFileName() {
                return deployFileName;
            }
        };
        uploadWebServerTemplateRequestMap.put(deployFileName, uploadWebServerTemplateRequest);
        groupPersistenceService.populateGroupWebServerTemplates(group.getName(), uploadWebServerTemplateRequestMap);
        return new CreateResourceResponseWrapper(createdConfigTemplate);
    }

    /**
     * Create the application template in the db and in the templates path for a specific application entity target.
     *
     * @param metaData        the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateContent the template content/data
     * @param targetAppName   the name of the application
     * @param parentName      the name of the JVM to associate with the application template
     */
    @Deprecated
    private CreateResourceResponseWrapper createApplicationTemplate(final ResourceTemplateMetaData metaData,
                                                                    final String templateContent,
                                                                    final String targetAppName,
                                                                    final String parentName) {
        final Application application = applicationPersistenceService.getApplication(targetAppName);
        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                metaData.getDeployFileName(), parentName, metaData.getJsonData(), templateContent);
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvmPersistenceService.findJvmByExactName(parentName).getId(), false);
        return new CreateResourceResponseWrapper(applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm));
    }

    /**
     * Create the application template in the db and in the templates path for all the application.
     *
     * @param metaData        the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateContent the template content/data
     * @param targetAppName   the application name
     */
    @Deprecated
    private CreateResourceResponseWrapper createGroupedApplicationsTemplate(final ResourceTemplateMetaData metaData,
                                                                            final String templateContent,
                                                                            final String targetAppName) throws IOException {
        final String groupName = metaData.getEntity().getGroup();
        Group group = groupPersistenceService.getGroup(groupName);
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(groupName);
        ConfigTemplate createdConfigTemplate = null;

        if (MediaType.APPLICATION_ZIP.equals(metaData.getContentType()) &&
                metaData.getTemplateName().toLowerCase(Locale.US).endsWith(WAR_FILE_EXTENSION)) {
            String tokenizedWarDeployPath = resourceContentGeneratorService.generateContent(
                    metaData.getDeployFileName(),
                    metaData.getDeployPath(),
                    null,
                    applicationPersistenceService.getApplication(targetAppName),
                    ResourceGeneratorType.METADATA);
            applicationPersistenceService.updateWarInfo(targetAppName, metaData.getDeployFileName(), templateContent, tokenizedWarDeployPath);
        }
        final String deployFileName = metaData.getDeployFileName();

        for (final Application application : applications) {
            if (metaData.getEntity().getDeployToJvms() && application.getName().equals(targetAppName)) {
                for (final Jvm jvm : group.getJvms()) {
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                            deployFileName, jvm.getJvmName(), metaData.getJsonData(), templateContent
                    );
                    JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
                    applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
                }
            }
        }

        createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(groupName, targetAppName, deployFileName,
                metaData.getJsonData(), templateContent);

        return new CreateResourceResponseWrapper(createdConfigTemplate);
    }

    @Override
    @Transactional
    public int deleteWebServerResources(List<String> templateNameList, String webServerName) {
        return resourceDao.deleteWebServerResources(templateNameList, webServerName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelWebServerResources(List<String> templateNameList, String groupName) {
        return resourceDao.deleteGroupLevelWebServerResources(templateNameList, groupName);
    }

    @Override
    @Transactional
    public int deleteJvmResources(List<String> templateNameList, String jvmName) {
        return resourceDao.deleteJvmResources(templateNameList, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelJvmResources(List<String> templateNameList, String groupName) {
        return resourceDao.deleteGroupLevelJvmResources(templateNameList, groupName);
    }

    @Override
    @Transactional
    public int deleteAppResources(List<String> templateNameList, String appName, String jvmName) {
        return resourceDao.deleteAppResources(templateNameList, appName, jvmName);
    }

    @Override
    @Transactional
    public int deleteExternalProperties() {
        final int deleteResult = resourceDao.deleteExternalProperties();
        ExternalProperties.reset();
        return deleteResult;
    }

    @Override
    @Transactional
    public int deleteGroupLevelAppResources(final String appName, final String groupName, final List<String> templateNameList) {
        final int deletedCount = resourceDao.deleteGroupLevelAppResources(appName, groupName, templateNameList);
        if (deletedCount > 0) {
            final List<Jvm> jvms = jvmPersistenceService.getJvmsByGroupName(groupName);
            for (Jvm jvm : jvms) {
                resourceDao.deleteAppResources(templateNameList, appName, jvm.getJvmName());
            }
            for (final String templateName : templateNameList) {
                if (templateName.toLowerCase(Locale.US).endsWith(".war")) {
                    final Application app = applicationPersistenceService.getApplication(appName);

                    // An app is only assigned to one group as of July 7, 2016 so we don't need the group to delete the
                    // war info of an app
                    applicationPersistenceService.deleteWarInfo(appName);

                    try {
                        repositoryService.delete(app.getWarPath());
                    } catch (final RepositoryServiceException e) {
                        LOGGER.error("Failed to delete the archive {}!", app.getWarPath(), e);
                    }

                }
            }
        }


        return deletedCount;
    }

    @Override
    @Transactional
    public ResourceContent getResourceContent(final ResourceIdentifier resourceIdentifier) {
        final ConfigTemplate configTemplate = resourceHandler.fetchResource(resourceIdentifier);
        if (configTemplate != null) {
            String jsonStrMetaData = configTemplate.getMetaData();
            return new ResourceContent(jsonStrMetaData, configTemplate.getTemplateContent());
        }
        return null;
    }

    @Override
    @Transactional
    public String updateResourceContent(ResourceIdentifier resourceIdentifier, String templateContent) {

        LOGGER.debug("Update template content for {} :: Updated content={}", resourceIdentifier, templateContent);

        resourceDao.updateResource(resourceIdentifier, EntityType.EXT_PROPERTIES, templateContent);

        // if the external properties resource was just saved then update properties
        checkResourceExternalProperties(resourceIdentifier, templateContent);

        final ConfigTemplate configTemplate = resourceHandler.fetchResource(resourceIdentifier);
        return configTemplate.getTemplateContent();
    }

    @Override
    @Transactional
    public String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData) {
        return resourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, metaData);
    }

    private void checkResourceExternalProperties(ResourceIdentifier resourceIdentifier, String templateContent) {
        if (StringUtils.isNotEmpty(resourceIdentifier.resourceName) &&
                StringUtils.isEmpty(resourceIdentifier.webAppName) &&
                StringUtils.isEmpty(resourceIdentifier.jvmName) &&
                StringUtils.isEmpty(resourceIdentifier.groupName) &&
                StringUtils.isEmpty(resourceIdentifier.webServerName)) {
            ExternalProperties.loadFromInputStream(new ByteArrayInputStream(templateContent.getBytes()));
        }

    }

    @Override
    public String previewResourceContent(ResourceIdentifier resourceIdentifier, String content) {
        return generateResourceFile(resourceIdentifier.resourceName, content, generateResourceGroup(), resourceHandler.getSelectedValue(resourceIdentifier), ResourceGeneratorType.PREVIEW);
    }

    private CommandOutput secureCopyFile(final String hostName, final String sourcePath, final String destPath, ResourceTemplateMetaData metaData, Object selectedValue) throws CommandFailureException {
        final boolean fileExists = distributionService.remoteFileCheck(hostName, destPath);
        if (fileExists && metaData.isOverwrite()) {
            LOGGER.info("Found the file {}, backing up", destPath);
            final CommandOutput commandOutput;
            if (metaData.isHotDeploy()) {
                commandOutput = distributionControlService.backupFileWithCopy(hostName, destPath);
            } else {
                commandOutput = distributionControlService.backupFileWithMove(hostName, destPath);
            }
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                String message = MessageFormat.format("Failed to backup source file {0} at destination {1} on host {2}", sourcePath, destPath, hostName);
                LOGGER.error(message);
                throw new ResourceServiceException(message);
            }
        } else if (fileExists) {
            String message = MessageFormat.format("Skipping scp of file: {0} already exists and overwrite is set to false.", destPath);
            LOGGER.info(message);
            historyFacadeService.write(hostName, getGroupsFromSelectedResource(selectedValue), message, EventType.SYSTEM_INFO, getUserFromSecurityContext());
            return new CommandOutput(new ExecReturnCode(0), message, "");
        }
        return distributionControlService.secureCopyFile(hostName, sourcePath, destPath);
    }

    private String getUserFromSecurityContext() {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return "";
        }

        final Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return "";
        }

        return authentication.getName();
    }

    private Collection<Group> getGroupsFromSelectedResource(Object selectedValue) {
        if (selectedValue instanceof Jvm) {
            return ((Jvm) selectedValue).getGroups();
        }

        if (selectedValue instanceof WebServer) {
            return ((WebServer) selectedValue).getGroups();
        }

        if (selectedValue instanceof Application) {
            return Collections.singleton(((Application) selectedValue).getGroup());
        }

        return new HashSet<>();
    }

    private void createConfigFile(String path, String configFileName, String templateContent) throws IOException {
        File configFile = new File(path + configFileName);
        if (configFileName.endsWith(".bat")) {
            templateContent = templateContent.replaceAll("\n", "\r\n");
        }
        FileUtils.writeStringToFile(configFile, templateContent);
    }

    @Override
    public String getExternalPropertiesAsString() {
        Properties externalProperties = getExternalProperties();

        // use a TreeMap to put the properties in alphabetical order
        final TreeMap sortedProperties = null == externalProperties ? null : new TreeMap<>(externalProperties);

        String retVal = "No External Properties configured";
        if (null != sortedProperties && !sortedProperties.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Object key : sortedProperties.keySet()) {
                sb.append(key);
                sb.append("=");
                sb.append(sortedProperties.get(key));
                sb.append("\n");
            }
            retVal = sb.toString();
        }

        return retVal;
    }

    @Override
    public File getExternalPropertiesAsFile() throws IOException {
        final List<String> extPropertiesNamesList = getResourceNames(new ResourceIdentifier.Builder().build());
        if (extPropertiesNamesList.isEmpty()) {
            LOGGER.error("No external properties file has been uploaded. Cannot provide a download at this time.");
            throw new InternalErrorException(FaultType.TEMPLATE_NOT_FOUND, "No external properties file has been uploaded. Cannot provide a download at this time.");
        }

        final String extPropertiesResourceName = extPropertiesNamesList.get(0);
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder().setResourceName(extPropertiesResourceName);
        ResourceIdentifier resourceIdentifier = idBuilder.build();
        final ResourceContent resourceContent = getResourceContent(resourceIdentifier);
        final String content = resourceContent.getContent();
        final ResourceGroup resourceGroup = generateResourceGroup();
        String fileContent = this.generateResourceFile(extPropertiesResourceName, content, resourceGroup, null, ResourceGeneratorType.TEMPLATE);
        String jvmResourcesNameDir = ApplicationProperties.get(PropertyKeys.PATHS_GENERATED_RESOURCE_DIR) + "/external-properties-download";

        createConfigFile(jvmResourcesNameDir + "/", extPropertiesResourceName, fileContent);

        return new File(jvmResourcesNameDir + "/" + extPropertiesResourceName);
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier resourceIdentifier) {
        // TODO derive the EntityType based on the resource identifier
        final List<String> resourceNames = resourceDao.getResourceNames(resourceIdentifier, EntityType.EXT_PROPERTIES);
        return resourceNames;
    }

    @Override
    public Properties getExternalProperties() {
        return ExternalProperties.getProperties();
    }

    @Override
    @Transactional
    public CreateResourceResponseWrapper createResource(final ResourceIdentifier resourceIdentifier,
                                                        final ResourceTemplateMetaData metaData,
                                                        final InputStream templateData) {

        String templateContent;
        if (MEDIA_TYPE_TEXT.equalsIgnoreCase(metaData.getContentType().getType()) || metaData.getContentType().equals(MediaType.APPLICATION_XML)) {
            Scanner scanner = new Scanner(templateData).useDelimiter("\\A");
            templateContent = scanner.hasNext() ? scanner.next() : "";
        } else {
            templateContent = uploadResource(metaData, templateData);
        }

        LOGGER.debug("Creating resource content for {} :: Content={}", resourceIdentifier, templateContent);

        try {
            return resourceHandler.createResource(resourceIdentifier, metaData, templateContent);
        } catch (final ResourceServiceException rhe) {
            throw new ResourceServiceException(rhe);
        }
    }

    @Override
    public String uploadResource(final ResourceTemplateMetaData resourceTemplateMetaData, final InputStream resourceDataIn) {
        return repositoryService.upload(resourceTemplateMetaData.getDeployFileName(), resourceDataIn);
    }

    @Override
    public CommandOutput generateAndDeployFile(final ResourceIdentifier resourceIdentifier, final String entity, final String fileName, final String hostName) {
        CommandOutput commandOutput = null;
        final String badStreamMessage = "Bad Stream: ";
        String metaDataStr;
        ResourceTemplateMetaData resourceTemplateMetaData;
        String resourceDestPath = null;
        String hostIPAddress = JwalaUtils.getHostAddress(hostName);
        try {
            validateSingleResourceForGeneration(resourceIdentifier);
            ConfigTemplate configTemplate = resourceHandler.fetchResource(resourceIdentifier);
            metaDataStr = configTemplate.getMetaData();
            final Object selectedValue = resourceHandler.getSelectedValue(resourceIdentifier);
            resourceTemplateMetaData = getTokenizedMetaData(fileName, selectedValue, metaDataStr);
            String resourceSourceCopy;
            final String deployFileName = resourceTemplateMetaData.getDeployFileName();
            final String deployPath = resourceTemplateMetaData.getDeployPath();
            if (deployPath != null && deployFileName != null) {
                resourceDestPath = deployPath + "/" + deployFileName;
                binaryDistributionLockManager.writeLock(hostIPAddress + ":" + resourceDestPath);
                if (MEDIA_TYPE_TEXT.equalsIgnoreCase(resourceTemplateMetaData.getContentType().getType()) ||
                        MediaType.APPLICATION_XML.equals(resourceTemplateMetaData.getContentType())) {
                    String fileContent = generateConfigFile(selectedValue, fileName);
                    String resourcesNameDir = ApplicationProperties.get(PropertyKeys.PATHS_GENERATED_RESOURCE_DIR) + "/" + entity;
                    resourceSourceCopy = resourcesNameDir + "/" + deployFileName;
                    createConfigFile(resourcesNameDir + "/", deployFileName, fileContent);
                } else {
                    resourceSourceCopy = generateTemplateForNotText(selectedValue, fileName);
                }
                //Create resource dir
                commandOutput = distributionControlService.createDirectory(hostName, deployPath);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    String errorMessage = MessageFormat.format("Failed to create directory {0} while deploying {1} to host {2}", deployPath, fileName, hostName);
                    LOGGER.error(errorMessage);
                    throw new ResourceServiceException(errorMessage);
                }
                commandOutput = secureCopyFile(hostName, resourceSourceCopy, resourceDestPath, resourceTemplateMetaData, selectedValue);
                if (resourceTemplateMetaData.isUnpack()) {
                    doUnpack(hostName, deployPath + "/" + resourceTemplateMetaData.getDeployFileName());
                }
            }
        } catch (IOException e) {
            String message = "Failed to write file " + fileName + ". " + e.toString();
            LOGGER.error(badStreamMessage + message, e);
            throw new InternalErrorException(FaultType.BAD_STREAM, message, e);
        } catch (CommandFailureException ce) {
            String message = "Failed to copy file " + fileName + ". " + ce.getMessage();
            LOGGER.error(badStreamMessage + message, ce);
            throw new InternalErrorException(FaultType.BAD_STREAM, message, ce);
        } finally {
            if (resourceDestPath != null) {
                binaryDistributionLockManager.writeUnlock(hostIPAddress + ":" + resourceDestPath);
            }
        }
        return commandOutput;
    }

    private void doUnpack(final String hostName, final String destPath) {
        try {
            binaryDistributionService.distributeUnzip(hostName);
            final String zipDestinationOption = FilenameUtils.removeExtension(destPath);
            LOGGER.debug("checking if unpacked destination exists: {}", zipDestinationOption);
            binaryDistributionService.remoteFileCheck(hostName, zipDestinationOption);
            binaryDistributionService.backupFile(hostName, zipDestinationOption);
            binaryDistributionService.remoteUnzipBinary(hostName, destPath, zipDestinationOption, "");
        } catch (CommandFailureException e) {
            LOGGER.error("Failed to execute remote command when unpack to {} ", destPath, e);
            throw new ApplicationException("Failed to execute remote command when unpack to  " + destPath, e);
        }
    }

    private <T> String generateTemplateForNotText(final T entity, final String fileName) {
        String template = "";
        if (entity instanceof Jvm) {
            template = jvmPersistenceService.getResourceTemplate(((Jvm) entity).getJvmName(), fileName);
        } else if (entity instanceof Application) {
            Application application = setApplicationWarDeployPath((Application) entity);
            if (application.getParentJvm() != null) {
                template = applicationPersistenceService.getResourceTemplate(
                        application.getName(),
                        fileName,
                        application.getParentJvm().getJvmName(),
                        application.getGroup().getName());
            } else {
                template = groupPersistenceService.getGroupAppResourceTemplate(
                        application.getGroup().getName(),
                        application.getName(), fileName);
            }
        } else if (entity instanceof WebServer) {
            template = webServerPersistenceService.getResourceTemplate(((WebServer) entity).getName(), fileName);
        }
        return template;
    }

    private <T> String generateConfigFile(final T entity, final String fileName) {
        String resourceFileString = "";
        final String failMessage = "Failed to find the template in the database or on the file system";
        if (entity instanceof Jvm) {
            final String templateContent = jvmPersistenceService.getJvmTemplate(fileName, ((Jvm) entity).getId());
            if (!templateContent.isEmpty()) {
                resourceFileString = generateResourceFile(fileName, templateContent, generateResourceGroup(), entity, ResourceGeneratorType.TEMPLATE);
            } else {
                throw new BadRequestException(FaultType.JVM_TEMPLATE_NOT_FOUND, failMessage);
            }
        } else if (entity instanceof Application) {
            String templateContentApplication;
            Application application = setApplicationWarDeployPath((Application) entity);
            if (application.getParentJvm() != null) {
                templateContentApplication = applicationPersistenceService.getResourceTemplate(
                        application.getName(),
                        fileName,
                        application.getParentJvm().getJvmName(),
                        application.getGroup().getName());
            } else {
                templateContentApplication = groupPersistenceService.getGroupAppResourceTemplate(
                        application.getGroup().getName(),
                        application.getName(), fileName);
            }
            if (!templateContentApplication.isEmpty()) {
                resourceFileString = generateResourceFile(fileName, templateContentApplication, generateResourceGroup(), entity, ResourceGeneratorType.TEMPLATE);
            } else {
                throw new BadRequestException(FaultType.APP_TEMPLATE_NOT_FOUND, failMessage);
            }
        } else if (entity instanceof WebServer) {
            final String templateContentWebServer = webServerPersistenceService.getResourceTemplate(((WebServer) entity).getName(), fileName);
            if (!templateContentWebServer.isEmpty()) {
                resourceFileString = generateResourceFile(fileName, templateContentWebServer, generateResourceGroup(), entity, ResourceGeneratorType.TEMPLATE);
            } else {
                throw new BadRequestException(FaultType.WEB_SERVER_CONF_TEMPLATE_NOT_FOUND, failMessage);
            }
        }
        return resourceFileString;
    }

    /**
     * Get the deploy path of the application's war from the meta data, and update the application's warDeployPath attribute
     * @param application the application to update the war deploy path
     * @return the application with the latest war deploy path
     */
    private Application setApplicationWarDeployPath(Application application) {
        final String warName = application.getWarName();
        final String name = application.getName();

        if (StringUtils.isEmpty(warName)) {
            LOGGER.info("No war found for application {}, skipping setting of war deploy path.", name);
            return application;
        }

        ResourceContent warResourceContent = getApplicationWarResourceContent(application, warName, name);
        return updateApplicationWarInfo(application, warName, name, warResourceContent);
    }

    private Application updateApplicationWarInfo(Application application, String warName, String name, ResourceContent warResourceContent) {
        final ResourceTemplateMetaData tokenizedMetaData;
        try {
            final String metaData = warResourceContent.getMetaData();
            tokenizedMetaData = getTokenizedMetaData(warName, application, metaData);
            final String deployPath = tokenizedMetaData.getDeployPath();
            LOGGER.info("Setting application {} war deploy path to: {}", name, deployPath);

            application = applicationPersistenceService.updateWarInfo(name, warName, application.getWarPath(), deployPath);
        } catch (IOException e) {
            String errMsg = MessageFormat.format("Failed to tokenize the meta data for resource {0} in application {1}", warName, name);
            throw new ResourceServiceException(errMsg, e);
        }
        return application;
    }

    private ResourceContent getApplicationWarResourceContent(Application application, String warName, String name) {
        ResourceIdentifier appWarIdentifier = new ResourceIdentifier.Builder()
                .setResourceName(warName)
                .setWebAppName(name)
                .setGroupName(application.getGroup().getName())
                .build();
        return getResourceContent(appWarIdentifier);
    }


    @Override
    public String getResourceMimeType(final BufferedInputStream fileContents) {
        try {
            return fileTypeDetector.detect(fileContents);
        } catch (final IOException e) {
            throw new ResourceServiceException("Failed to read mime type from stream!", e);
        }
    }

}
