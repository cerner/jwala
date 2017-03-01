package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AppResourceHandlerTest {

    private AppResourceHandler appResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private JvmPersistenceService mockJvmPersistence;

    @Mock
    private ApplicationPersistenceService mockAppPersistence;

    private ResourceIdentifier resourceIdentifier;

    @Before
    public void setup(){
        resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("resource-name")
                .setGroupName("app-group")
                .setWebAppName("app-name")
                .setJvmName("app-jvm-name").build();
        mockResourceDao = mock(ResourceDao.class);
        mockJvmPersistence = mock(JvmPersistenceService.class);
        mockAppPersistence = mock (ApplicationPersistenceService.class);
        mockSuccessor = mock(ResourceHandler.class);
        appResourceHandler = new AppResourceHandler(mockResourceDao, mockJvmPersistence, mockAppPersistence, mockSuccessor);
    }

    @Test
    public void testUpdateMetaData() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";

        when(mockAppPersistence.updateResourceMetaData(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        appResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);
        verify(mockAppPersistence).updateResourceMetaData(eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData), eq(resourceIdentifier.jvmName), eq(resourceIdentifier.groupName));
    }

    @Test
    public void testUpdateMetaDataCallsSuccessor() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName).setGroupName("not-a-web-app").build();

        appResourceHandler.updateResourceMetaData(notMeResourceIdentifier, resourceName, updatedMetaData);
        verify(mockSuccessor).updateResourceMetaData(eq(notMeResourceIdentifier), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testGetSelectedValue() {
        Application mockApp = mock(Application.class);
        Jvm mockJvm = mock(Jvm.class);

        when(mockAppPersistence.getApplication(anyString())).thenReturn(mockApp);
        when(mockJvmPersistence.findJvmByExactName(anyString())).thenReturn(mockJvm);

        Application app = (Application) appResourceHandler.getSelectedValue(resourceIdentifier);
        verify(mockApp).setParentJvm(eq(mockJvm));
        assertEquals(mockApp, app);
    }

    @Test
    public void testGetSelectedValueCallsSuccessor() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("whats-app-amiright").setGroupName("not-a-web-app").build();

        Application app = (Application) appResourceHandler.getSelectedValue(notMeResourceIdentifier);
        verify(mockSuccessor).getSelectedValue(notMeResourceIdentifier);
    }

    @Test
    public void testGetResourceNames() {
        appResourceHandler.getResourceNames(resourceIdentifier);
        verify(mockAppPersistence).getResourceTemplateNames(anyString(), anyString());

        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("whats-app-amiright").setGroupName("not-a-web-app").build();
        appResourceHandler.getResourceNames(notMeResourceIdentifier);
        verify(mockSuccessor).getResourceNames(notMeResourceIdentifier);
    }
}
