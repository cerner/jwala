package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/29/2016.
 */
public class GroupLevelWebServerResourceHandlerTest {

    private GroupLevelWebServerResourceHandler groupLevelWebServerResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private WebServerPersistenceService mockWebServerPersistence;

    @Mock
    private GroupPersistenceService mockGroupPersistence;

    @Mock
    private ResourceContentGeneratorService mockResourceContentGenerator;

    private ResourceIdentifier resourceIdentifier;

    @Before
    public void setup() {
        resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("resource-name")
                .setGroupName("webserver-group")
                .setWebServerName("*").build();
        mockResourceDao = mock(ResourceDao.class);
        mockWebServerPersistence = mock(WebServerPersistenceService.class);
        mockGroupPersistence = mock(GroupPersistenceService.class);
        mockSuccessor = mock(ResourceHandler.class);
        mockResourceContentGenerator = mock(ResourceContentGeneratorService.class);
        groupLevelWebServerResourceHandler = new GroupLevelWebServerResourceHandler(mockResourceDao, mockGroupPersistence, mockWebServerPersistence, mockResourceContentGenerator, mockSuccessor);
    }

    @Test
    public void testUpdateMetaData() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        Group mockGroup = mock(Group.class);
        WebServer mockWebServer = mock(WebServer.class);
        Set<WebServer> groupSet = new HashSet<>();
        groupSet.add(mockWebServer);

        when(mockGroup.getWebServers()).thenReturn(groupSet);
        when(mockWebServer.getName()).thenReturn("webserver-name");
        when(mockGroupPersistence.getGroupWithWebServers(anyString())).thenReturn(mockGroup);
        when(mockGroupPersistence.updateGroupWebServerResourceMetaData(anyString(), anyString(), anyString())).thenReturn(updatedMetaData);

        groupLevelWebServerResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);

        verify(mockGroupPersistence).updateGroupWebServerResourceMetaData(eq(resourceIdentifier.groupName), eq(resourceName), eq(updatedMetaData));
        verify(mockWebServerPersistence).updateResourceMetaData(eq(mockWebServer.getName()), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testUpdateMetaDataCallsSuccessor() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName).setGroupName("not-a-webserver").build();

        groupLevelWebServerResourceHandler.updateResourceMetaData(notMeResourceIdentifier, resourceName, updatedMetaData);
        verify(mockSuccessor).updateResourceMetaData(eq(notMeResourceIdentifier), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testGetSelectedValue() {
        // group level entities don't return a selected value
        Object selectedValue = groupLevelWebServerResourceHandler.getSelectedValue(resourceIdentifier);
        assertNull(selectedValue);
    }

    @Test
    public void testGetSelectedValueCallsSuccessor() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("webserver-resource").setGroupName("not-a-webserver").build();

        groupLevelWebServerResourceHandler.getSelectedValue(notMeResourceIdentifier);
        verify(mockSuccessor).getSelectedValue(notMeResourceIdentifier);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testGetResourceNames() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("what-group-level-web-server").setGroupName("not-a-web-server").build();
        groupLevelWebServerResourceHandler.getResourceNames(notMeResourceIdentifier);
        verify(mockSuccessor).getResourceNames(notMeResourceIdentifier);

        groupLevelWebServerResourceHandler.getResourceNames(resourceIdentifier);
    }

}
