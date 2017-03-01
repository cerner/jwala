package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/29/2016.
 */
public class WebServerResourceHandlerTest {

    private WebServerResourceHandler webServerResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private WebServerPersistenceService mockWebServerPersistence;

    @Mock
    private ResourceContentGeneratorService mockResourceContentGenerator;

    private ResourceIdentifier resourceIdentifier;

    @Before
    public void setup(){
        resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("resource-name")
                .setGroupName("webserver-group")
                .setWebServerName("webserver-name").build();
        mockResourceDao = mock(ResourceDao.class);
        mockWebServerPersistence = mock(WebServerPersistenceService.class);
        mockSuccessor = mock(ResourceHandler.class);
        mockResourceContentGenerator = mock(ResourceContentGeneratorService.class);
        webServerResourceHandler = new WebServerResourceHandler(mockResourceDao, mockWebServerPersistence, mockResourceContentGenerator, mockSuccessor);
    }

    @Test
    public void testUpdateMetaData() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";

        when(mockWebServerPersistence.updateResourceMetaData(anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        webServerResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);
        verify(mockWebServerPersistence).updateResourceMetaData(eq(resourceIdentifier.webServerName), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testUpdateMetaDataCallsSuccessor() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName).setGroupName("not-a-webserver").build();

        webServerResourceHandler.updateResourceMetaData(notMeResourceIdentifier, resourceName, updatedMetaData);
        verify(mockSuccessor).updateResourceMetaData(eq(notMeResourceIdentifier), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testGetSelectedValue() {
        WebServer mockWebServer = mock(WebServer.class);

        when(mockWebServerPersistence.findWebServerByName(anyString())).thenReturn(mockWebServer);

        WebServer webServer = (WebServer) webServerResourceHandler.getSelectedValue(resourceIdentifier);
        assertEquals(mockWebServer, webServer);
    }

    @Test
    public void testGetSelectedValueCallsSuccessor() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("webserver-resource").setGroupName("not-a-webserver").build();

        webServerResourceHandler.getSelectedValue(notMeResourceIdentifier);
        verify(mockSuccessor).getSelectedValue(notMeResourceIdentifier);
    }

    @Test
    public void testGetResourceNames() {
        webServerResourceHandler.getResourceNames(resourceIdentifier);
        verify(mockWebServerPersistence).getResourceTemplateNames(anyString());

        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("what-webserver").setGroupName("not-a-web-server").build();
        webServerResourceHandler.getResourceNames(notMeResourceIdentifier);
        verify(mockSuccessor).getResourceNames(notMeResourceIdentifier);
    }
}
