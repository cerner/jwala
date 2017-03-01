package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaResourceConfigTemplate;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.repository.RepositoryService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.service.resource.impl.ResourceServiceImpl;
import org.apache.tika.Tika;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

public class ExternalPropertiesResourceHandlerTest {

    private ExternalPropertiesResourceHandler externalPropertiesResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private RepositoryService mockRepositoryService;

    private ResourceService resourceService = new ResourceServiceImpl(null, null, null, null, null, null, null,
                                                                      null, null, new Tika(), mockRepositoryService);
    @Before
    public void setup(){
        mockResourceDao = mock(ResourceDao.class);
        mockSuccessor = mock(ResourceHandler.class);
        externalPropertiesResourceHandler = new ExternalPropertiesResourceHandler(mockResourceDao, mockSuccessor);
    }

    @Test
    public void testCanHandle() {
        ResourceIdentifier.Builder resourceIdentifier = new ResourceIdentifier.Builder();
        resourceIdentifier.setGroupName(null);
        resourceIdentifier.setWebAppName(null);
        resourceIdentifier.setJvmName(null);
        resourceIdentifier.setWebServerName(null);
        resourceIdentifier.setResourceName("external.properties");

        assertTrue(externalPropertiesResourceHandler.canHandle(resourceIdentifier.build()));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testDeleteExternalProperties() {
        ResourceIdentifier.Builder resourceIdentifier = new ResourceIdentifier.Builder();
        externalPropertiesResourceHandler.deleteResource(resourceIdentifier.build());
    }

    @Test
    public void testFetchResource() {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName(null);
        resourceIdentifierBuilder.setWebAppName(null);
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        JpaResourceConfigTemplate mockResourceConfigTemplate = mock(JpaResourceConfigTemplate.class);
        when(mockResourceDao.getExternalPropertiesResource(eq("external.properties"))).thenReturn(mockResourceConfigTemplate);

        externalPropertiesResourceHandler.fetchResource(resourceIdentifierBuilder.build());

        verify(mockResourceDao).getExternalPropertiesResource(eq("external.properties"));
    }

    @Test
    public void testFetchResourcePassesOnToSuccessor() {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName("test-group-name");
        resourceIdentifierBuilder.setWebAppName("test-app-name");
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        ConfigTemplate mockResourceConfigTemplate = mock(JpaResourceConfigTemplate.class);
        when(mockSuccessor.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockResourceConfigTemplate);

        final ResourceIdentifier resourceId = resourceIdentifierBuilder.build();
        externalPropertiesResourceHandler.fetchResource(resourceId);

        verify(mockSuccessor).fetchResource(eq(resourceId));
    }

    @Test
    public void testCreateResource() throws IOException {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName(null);
        resourceIdentifierBuilder.setWebAppName(null);
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        ResourceIdentifier identifier = resourceIdentifierBuilder.build();

        final ResourceTemplateMetaData metaData =
                resourceService.getMetaData("{\"deployFileName\": \"external.properties\"}");

        String templateContent = "key=value";
        JpaResourceConfigTemplate mockJpaResourceConfigTemplate = mock(JpaResourceConfigTemplate.class);

        when(mockResourceDao.createResource(anyLong(), anyLong(), anyLong(), eq(EntityType.EXT_PROPERTIES), eq("external.properties"), anyString(), anyString())).thenReturn(mockJpaResourceConfigTemplate);
        when(mockResourceDao.getExternalPropertiesResource(anyString())).thenReturn(mockJpaResourceConfigTemplate);
        when(mockJpaResourceConfigTemplate.getTemplateContent()).thenReturn("key=value");

        CreateResourceResponseWrapper result = externalPropertiesResourceHandler.createResource(identifier, metaData, templateContent);
        assertNotNull(result);
        verify(mockResourceDao).createResource((Long)isNull(), (Long)isNull(), (Long)isNull(), eq(EntityType.EXT_PROPERTIES), eq("external.properties"), eq(templateContent), anyString());
    }

    @Test
    public void testCreateResourceDeletesExistingResource() throws IOException {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName(null);
        resourceIdentifierBuilder.setWebAppName(null);
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        ResourceIdentifier identifier = resourceIdentifierBuilder.build();
        final ResourceTemplateMetaData metaData =
                resourceService.getMetaData("{\"deployFileName\": \"external.properties\"}");
        String templateContent = "key=value";
        JpaResourceConfigTemplate mockJpaResourceConfigTemplate = mock(JpaResourceConfigTemplate.class);
        List<String> resourceNames = new ArrayList<>();
        resourceNames.add("external.properties");

        when(mockResourceDao.getResourceNames(any(ResourceIdentifier.class), eq(EntityType.EXT_PROPERTIES))).thenReturn(resourceNames);
        when(mockResourceDao.deleteExternalProperties()).thenReturn(1);
        when(mockResourceDao.createResource(anyLong(), anyLong(), anyLong(), eq(EntityType.EXT_PROPERTIES), eq("external.properties"), anyString(), anyString())).thenReturn(mockJpaResourceConfigTemplate);
        when(mockResourceDao.getExternalPropertiesResource(anyString())).thenReturn(mockJpaResourceConfigTemplate);
        when(mockJpaResourceConfigTemplate.getTemplateContent()).thenReturn("key=value");

        CreateResourceResponseWrapper result = externalPropertiesResourceHandler.createResource(identifier, metaData, templateContent);
        assertNotNull(result);
        verify(mockResourceDao).createResource((Long)isNull(), (Long)isNull(), (Long)isNull(), eq(EntityType.EXT_PROPERTIES), eq("external.properties"), eq(templateContent), anyString());
    }

    @Test
    public void testCreateResourceCallsSuccessor() throws IOException {
        ResourceIdentifier.Builder resourceIdentifierBuilder = new ResourceIdentifier.Builder();
        resourceIdentifierBuilder.setGroupName("test-group-name");
        resourceIdentifierBuilder.setWebAppName("test-app-name");
        resourceIdentifierBuilder.setJvmName(null);
        resourceIdentifierBuilder.setWebServerName(null);
        resourceIdentifierBuilder.setResourceName("external.properties");

        final ResourceTemplateMetaData metaData =
                resourceService.getMetaData("{\"deployFileName\": \"external.properties\"}");

        CreateResourceResponseWrapper mockCreateResourceResponseWrapper = mock(CreateResourceResponseWrapper.class);

        when(mockSuccessor.createResource(any(ResourceIdentifier.class), any(ResourceTemplateMetaData.class), anyString())).thenReturn(mockCreateResourceResponseWrapper);
        CreateResourceResponseWrapper result = externalPropertiesResourceHandler.createResource(resourceIdentifierBuilder.build(), metaData, "key=value");
        assertNotNull(result);
        verify(mockSuccessor).createResource(any(ResourceIdentifier.class), any(ResourceTemplateMetaData.class), anyString());
    }
}
