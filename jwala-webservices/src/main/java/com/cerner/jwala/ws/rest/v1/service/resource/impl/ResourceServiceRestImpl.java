package com.cerner.jwala.ws.rest.v1.service.resource.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.resource.Entity;
import com.cerner.jwala.common.domain.model.resource.ResourceContent;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.properties.ExternalProperties;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateMetaDataUpdateException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.resource.CreateResourceParam;
import com.cerner.jwala.ws.rest.v1.service.resource.ResourceHierarchyParam;
import com.cerner.jwala.ws.rest.v1.service.resource.ResourceServiceRest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * {@link ResourceServiceRest} implementation.
 * <p/>
 * Created by Eric Pinder on 3/16/2015.
 */
public class ResourceServiceRestImpl implements ResourceServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServiceRestImpl.class);
    private static final int CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS = 2;
    private static final String JSON_FILE_EXTENSION = ".json";
    public static final String UNEXPECTED_CONTENT_TYPE_ERROR_MSG =
            "File being uploaded is invalid! The expected file type as indicated in the meta data is text based and should have a TPL extension.";
    public static final String TPL_FILE_EXTENSION = ".tpl";
    public static final String EXT_PROPERTIES_RESOURCE_NAME = "ext.properties";
    public static final String EXT_PROPERTIES_RESOURCE_META_DATA = "{\"contentType\":\"text/plain\", \"templateName\":\"external.properties\", \"deployPath\":\"\", \"deployFileName\":\"" + EXT_PROPERTIES_RESOURCE_NAME + "\"}";
    public static final int CREATE_RESOURCE_ATTACHMENT_SIZE = 3;

    private final ResourceService resourceService;

    public ResourceServiceRestImpl(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    public Response createTemplate(final List<Attachment> attachments, final String targetName, final AuthenticatedUser user) {
        LOGGER.info("create template for target {} by user {}", targetName, user.getUser().getId());
        // TODO check for a max file size
        List<Attachment> filteredAttachments = new ArrayList<>();
        for (Attachment attachment : attachments) {
            if (null != attachment.getDataHandler() && null != attachment.getDataHandler().getName()) {
                filteredAttachments.add(attachment);
            }
        }
        if (filteredAttachments.size() == CREATE_TEMPLATE_EXPECTED_NUM_OF_ATTACHMENTS) {
            InputStream metadataInputStream = null;
            InputStream templateInputStream = null;
            for (Attachment attachment : filteredAttachments) {
                final DataHandler handler = attachment.getDataHandler();
                try {
                    LOGGER.debug("filename is {}", handler.getName());
                    if (handler.getName().toLowerCase(Locale.US).endsWith(JSON_FILE_EXTENSION)) {
                        metadataInputStream = attachment.getDataHandler().getInputStream();
                    } else {
                        templateInputStream = attachment.getDataHandler().getInputStream();
                    }
                } catch (final IOException ioe) {
                    LOGGER.error("Create template failed!", ioe);
                    return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                            new FaultCodeException(FaultType.IO_EXCEPTION, ioe.getMessage()));
                }
            }
            return ResponseBuilder.created(resourceService.createTemplate(metadataInputStream, templateInputStream, targetName, user.getUser()));
        } else {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_NUMBER_OF_ATTACHMENTS,
                    "Invalid number of attachments! 2 attachments is expected by the service."));
        }
    }

    @Override
    public Response getResourceAttrData() {
        LOGGER.debug("Get resource attribute data");
        return ResponseBuilder.ok(resourceService.generateResourceGroup());
    }

    @Override
    public Response getResourceTopology() {
        LOGGER.debug("Get resource topology");
        return ResponseBuilder.ok(resourceService.generateResourceGroup());
    }

    @Override
    public Response getApplicationResourceNames(final String groupName, final String appName) {
        LOGGER.debug("Get application resource name for group {} and application {}", groupName, appName);
        return ResponseBuilder.ok(resourceService.getApplicationResourceNames(groupName, appName));
    }

    @Override
    public Response getAppTemplate(final String groupName, final String appName, final String templateName) {
        LOGGER.debug("Get application template for group {}, application {}, and template {}", groupName, appName, templateName);
        return ResponseBuilder.ok(resourceService.getAppTemplate(groupName, appName, templateName));
    }

    @Override
    public Response checkFileExists(final String groupName, final String jvmName, final String webappName, final String webserverName, final String fileName) {
        LOGGER.debug("Check file exists for group {}, JVM {}, application {}, web server {}, file {}", groupName, jvmName, webappName, webserverName, fileName);
        return ResponseBuilder.ok(resourceService.checkFileExists(groupName, jvmName, webappName, webserverName, fileName));
    }

    @Override
    public Response createResource(final String deployFilename, final CreateResourceParam createResourceParam,
                                   final List<Attachment> attachments) {
        final CreateResourceResponseWrapper createResourceResponseWrapper;

        // TODO pass down single param from UI to designate external properties
        final boolean isExternalProperty = createResourceParam.getGroup() == null && createResourceParam.getJvm() == null && createResourceParam.getWebApp() == null && createResourceParam.getWebServer() == null;

        if (attachments == null || !isExternalProperty && attachments.size() != CREATE_RESOURCE_ATTACHMENT_SIZE) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    FaultType.INVALID_NUMBER_OF_ATTACHMENTS,
                    "Invalid number of attachments! " + CREATE_RESOURCE_ATTACHMENT_SIZE + " attachments is expected by the service."));
        }

        try {
            final Map<String, Object> metaDataMap = new HashMap<>();
            BufferedInputStream bufferedInputStream = null;
            String templateName = null;
            for (final Attachment attachment : attachments) {
                if (attachment.getHeader("Content-Type") == null) {
                    metaDataMap.put(attachment.getDataHandler().getName(),
                            IOUtils.toString(attachment.getDataHandler().getInputStream(), Charset.defaultCharset()));
                } else {
                    templateName = attachment.getDataHandler().getName();
                    bufferedInputStream = new BufferedInputStream(attachment.getDataHandler().getInputStream());
                }
            }

            metaDataMap.put("deployFileName", deployFilename);
            metaDataMap.put("templateName", templateName);
            metaDataMap.put("contentType", resourceService.getResourceMimeType(bufferedInputStream));

            // Note: In the create resource UI "assign to JVMs" makes more sense than "deploy to JVMs" e.g.
            //       one create's a resource that will be assigned to JVMs.
            //       We have to put it in its meta data counter part which is deployToJvms.
            //       IMHO meta data's deployToJvms should be renamed to assignToJvms but it can't be changed just yet
            //       not until an impact analysis has been made.
            // TODO: Discuss with the team about renaming meta data's deployToJvms to assignToJvms
            final Entity entity = new Entity(null, null, null, null, Boolean.parseBoolean((String) metaDataMap.get("assignToJvms")));
            metaDataMap.remove("assignToJvms");
            metaDataMap.put("entity", entity);

            final ResourceTemplateMetaData resourceTemplateMetaData =
                    resourceService.getMetaData(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(metaDataMap));

            final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(templateName)
                    .setGroupName(createResourceParam.getGroup())
                    .setWebServerName(createResourceParam.getWebServer())
                    .setJvmName(createResourceParam.getJvm())
                    .setWebAppName(createResourceParam.getWebApp()).build();

            createResourceResponseWrapper = resourceService.createResource(resourceIdentifier, resourceTemplateMetaData,
                    bufferedInputStream);
        } catch (final IOException e) {
            LOGGER.error("Failed to create resource {}!", deployFilename, e);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(FaultType.IO_EXCEPTION, e.getMessage()));
        }

        return ResponseBuilder.ok(createResourceResponseWrapper);
    }

    @Context
    private MessageContext context;

    // for unit testing
    public void setMessageContext(MessageContext messageContext) {
        this.context = messageContext;
    }

    @Override
    public Response uploadExternalProperties(AuthenticatedUser user) {
        LOGGER.info("Upload external properties by user {}", user.getUser().getId());

        // iframe uploads from IE do not understand application/json
        // as a response and will prompt for download. Fix: return
        // text/html
        if (!context.getHttpHeaders().getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE)) {
            context.getHttpServletResponse().setContentType(MediaType.TEXT_HTML);
        }

        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier resourceIdentifier = idBuilder.setResourceName(EXT_PROPERTIES_RESOURCE_NAME).build();
        final ResourceTemplateMetaData metaData;
        InputStream data = null;
        CreateResourceResponseWrapper responseWrapper = null;

        try {
            metaData = resourceService.getMetaData(EXT_PROPERTIES_RESOURCE_META_DATA);
            ServletFileUpload sfu = new ServletFileUpload();
            FileItemIterator iter = sfu.getItemIterator(context.getHttpServletRequest());
            FileItemStream fileItemStream = iter.next();
            data = fileItemStream.openStream();

            responseWrapper = resourceService.createResource(resourceIdentifier, metaData, data);
        } catch (IOException ioe) {
            LOGGER.error("IOException thrown in uploadExternalProperties", ioe);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(FaultType.SERVICE_EXCEPTION, ioe.getMessage()));
        } catch (FileUploadException fue) {
            LOGGER.error("FileUploadException thrown in uploadExternalProperties", fue);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(FaultType.SERVICE_EXCEPTION, fue.getMessage()));
        } finally {
            assert data != null;
            try {
                data.close();
            } catch (IOException e) {
                LOGGER.warn("IOException attempting to close external properties upload stream", e);
            }

        }
        return ResponseBuilder.ok(responseWrapper);
    }

    @Override
// TODO: Re validation, maybe we can use CXF bean validation ?
    public Response deleteResource(final String templateName, final ResourceHierarchyParam resourceHierarchyParam,
                                   final AuthenticatedUser aUser) {
        LOGGER.info("Delete resource {} by user {} with details {}", templateName, aUser.getUser().getId(), resourceHierarchyParam);
        int deletedRecCount = 0;

        // NOTE: We do the parameter checking logic here since the service layer does not know anything about ResourceHierarchyParam.
        if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level Web App
            deletedRecCount = resourceService.deleteGroupLevelAppResource(resourceHierarchyParam.getWebApp(), templateName);

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web App
            deletedRecCount = resourceService.deleteAppResource(templateName, resourceHierarchyParam.getWebApp(), resourceHierarchyParam.getJvm());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {
            // Group Level Web Servers
            if (resourceHierarchyParam.getWebServer().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelWebServerResource(templateName, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web Server
            deletedRecCount = resourceService.deleteWebServerResource(templateName, resourceHierarchyParam.getWebServer());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level JVMs
            if (resourceHierarchyParam.getJvm().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelJvmResource(templateName, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // JVM
            deletedRecCount = resourceService.deleteJvmResource(templateName, resourceHierarchyParam.getJvm());

        } else {

            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(FaultType.INVALID_REST_SERVICE_PARAMETER,
                            "Parameters passed to the rest service is/are invalid!"));

        }
        return ResponseBuilder.ok(deletedRecCount);
    }

    @Override
    public Response deleteResources(final String[] templateNameArray, ResourceHierarchyParam
            resourceHierarchyParam, AuthenticatedUser user) {
        LOGGER.info("Delete resources {} by user {} with details {}", templateNameArray, user.getUser().getId(), resourceHierarchyParam);
        int deletedRecCount = 0;

        final List<String> templateNameList = Arrays.asList(templateNameArray);

        // NOTE: We do the parameter checking logic here since the service layer does not know anything about ResourceHierarchyParam.
        if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level Web App
            deletedRecCount = resourceService.deleteGroupLevelAppResources(resourceHierarchyParam.getWebApp(), resourceHierarchyParam.getGroup(), templateNameList);

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isNotEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web App
            deletedRecCount = resourceService.deleteAppResources(templateNameList, resourceHierarchyParam.getWebApp(), resourceHierarchyParam.getJvm());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {
            // Group Level Web Servers
            if (resourceHierarchyParam.getWebServer().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelWebServerResources(templateNameList, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isNotEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Web Server
            deletedRecCount = resourceService.deleteWebServerResources(templateNameList, resourceHierarchyParam.getWebServer());

        } else if (ParamValidator.getNewInstance().isNotEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // Group Level JVMs
            if (resourceHierarchyParam.getJvm().equalsIgnoreCase("*")) {
                deletedRecCount = resourceService.deleteGroupLevelJvmResources(templateNameList, resourceHierarchyParam.getGroup());
            }

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isNotEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {

            // JVM
            deletedRecCount = resourceService.deleteJvmResources(templateNameList, resourceHierarchyParam.getJvm());

        } else if (ParamValidator.getNewInstance().isEmpty(resourceHierarchyParam.getGroup())
                .isEmpty(resourceHierarchyParam.getWebServer())
                .isEmpty(resourceHierarchyParam.getJvm())
                .isEmpty(resourceHierarchyParam.getWebApp()).isValid()) {
            // External Properties
            deletedRecCount = resourceService.deleteExternalProperties();

        } else {

            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                    new FaultCodeException(FaultType.INVALID_REST_SERVICE_PARAMETER,
                            "Parameters passed to the rest service is/are invalid!"));

        }
        return ResponseBuilder.ok(deletedRecCount);
    }

    @Override
    public Response getResourceContent(final String resourceName, final ResourceHierarchyParam param) {
        LOGGER.debug("Get the resource content for {} with hierarchy {}", resourceName, param);
        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName)
                .setGroupName(param.getGroup())
                .setWebServerName(param.getWebServer())
                .setJvmName(param.getJvm())
                .setWebAppName(param.getWebApp()).build();
        final ResourceContent resourceContent = resourceService.getResourceContent(resourceIdentifier);
        if (resourceContent == null) {
            return Response.noContent().build();
        }
        return ResponseBuilder.ok(resourceContent);
    }

    @Override
    public Response updateResourceContent(String resourceName, ResourceHierarchyParam
            resourceHierarchyParam, String templateContent) {
        LOGGER.info("Update the resource {} with hierarchy {}", resourceName, resourceHierarchyParam);
        LOGGER.debug("Updated content: {}", templateContent);

        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName)
                .setGroupName(resourceHierarchyParam.getGroup())
                .setWebServerName(resourceHierarchyParam.getWebServer())
                .setJvmName(resourceHierarchyParam.getJvm())
                .setWebAppName(resourceHierarchyParam.getWebApp()).build();

        return ResponseBuilder.ok(resourceService.updateResourceContent(resourceIdentifier, templateContent));
    }

    @Override
    public Response updateResourceMetaData(String resourceName, ResourceHierarchyParam resourceHierarchyParam, String metaData) {
        LOGGER.info("Update the meta data for resource {} with hierarchy {}", resourceName, resourceHierarchyParam);
        LOGGER.debug("Updated content: {}", metaData);

        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName)
                .setGroupName(resourceHierarchyParam.getGroup())
                .setWebServerName(resourceHierarchyParam.getWebServer())
                .setJvmName(resourceHierarchyParam.getJvm())
                .setWebAppName(resourceHierarchyParam.getWebApp()).build();
        try {
            return ResponseBuilder.ok(resourceService.updateResourceMetaData(resourceIdentifier, resourceName, metaData));
        } catch (ResourceTemplateMetaDataUpdateException ue) {
            LOGGER.error("Failed to update the resource", ue);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(FaultType.RESOURCE_META_DATA_UPDATE_FAILED, ue.getMessage()));
        }
    }

    @Override
    public Response previewResourceContent(final String resourceName, final ResourceHierarchyParam resourceHierarchyParam, String content) {
        LOGGER.debug("Preview the template for {}", resourceHierarchyParam);
        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName(resourceName)
                .setGroupName(resourceHierarchyParam.getGroup())
                .setWebServerName(resourceHierarchyParam.getWebServer())
                .setJvmName(resourceHierarchyParam.getJvm())
                .setWebAppName(resourceHierarchyParam.getWebApp()).build();
        return ResponseBuilder.ok(resourceService.previewResourceContent(resourceIdentifier, content));
    }

    @Override
    public Response getResourcesFileNames(ResourceHierarchyParam resourceHierarchyParam) {
        LOGGER.debug("Get the external properties file name");
        final ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setGroupName(resourceHierarchyParam.getGroup())
                .setWebServerName(resourceHierarchyParam.getWebServer())
                .setJvmName(resourceHierarchyParam.getJvm())
                .setWebAppName(resourceHierarchyParam.getWebApp()).build();
        final List<String> propertiesFile = resourceService.getResourceNames(resourceIdentifier);
        return ResponseBuilder.ok(propertiesFile);
    }

    @Override
    public Response getExternalProperties() {
        LOGGER.debug("Get the external properties");
        // use a TreeMap to put the properties in alphabetical order
        final Properties externalProperties = resourceService.getExternalProperties();
        return ResponseBuilder.ok(null == externalProperties ? null : new TreeMap<>(externalProperties));
    }

    @Override
    public Response getExternalPropertiesView() {
        LOGGER.debug("Get the external properties view");
        final String externalProperties = resourceService.getExternalPropertiesAsString();
        return ResponseImpl.ok(externalProperties).build();
    }

    @Override
    public Response getExternalPropertiesDownload() {
        Response response = null;
        try {
            final File propertiesAsFile = resourceService.getExternalPropertiesAsFile();
            Response.ResponseBuilder responseBuilder = Response.ok(propertiesAsFile);
            responseBuilder.header("Content-Disposition", "attachment; filename=" + propertiesAsFile.getName());
            response = responseBuilder.build();
        } catch (IOException e) {
            LOGGER.error("Error attempting to download the external properties file", e);
            response = ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(FaultType.BAD_STREAM, "Unable to provide the external properties file as a download."));
        }
        return response;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // load the external properties
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();

        final ResourceIdentifier resourceId = idBuilder.build();
        final List<String> resourceNames = resourceService.getResourceNames(resourceId);
        if (!resourceNames.isEmpty()) {
            idBuilder.setResourceName(resourceNames.get(0));
            ResourceContent resourceContent = resourceService.getResourceContent(idBuilder.build());
            if (resourceContent != null) {
                final String externalProperties = resourceContent.getContent();
                if (!externalProperties.isEmpty()) {
                    LOGGER.info("Load the external properties from the database on ResourceServiceRest initialization");
                    ExternalProperties.loadFromInputStream(new ByteArrayInputStream(externalProperties.getBytes()));
                }
            }
        }
    }
}