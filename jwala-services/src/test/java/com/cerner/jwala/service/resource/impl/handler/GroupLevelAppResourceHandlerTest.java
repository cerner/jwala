package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created on 9/29/2016.
 */
public class GroupLevelAppResourceHandlerTest {

    private GroupLevelAppResourceHandler groupAppResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private JvmPersistenceService mockJvmPersistence;

    @Mock
    private ApplicationPersistenceService mockAppPersistence;

    @Mock
    private GroupPersistenceService mockGroupPersistence;

    private ResourceIdentifier resourceIdentifier;

    @Before
    public void setup() {
        resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("resource-name")
                .setGroupName("app-group")
                .setWebAppName("app-name").build();
        mockResourceDao = mock(ResourceDao.class);
        mockJvmPersistence = mock(JvmPersistenceService.class);
        mockAppPersistence = mock(ApplicationPersistenceService.class);
        mockGroupPersistence = mock(GroupPersistenceService.class);
        mockSuccessor = mock(ResourceHandler.class);
        groupAppResourceHandler = new GroupLevelAppResourceHandler(mockResourceDao, mockGroupPersistence, mockJvmPersistence, mockAppPersistence, mockSuccessor);
    }

    @Test
    public void testUpdateMetaData() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("mockJvm");
        when(mockGroup.getJvms()).thenReturn(jvmSet);

        when(mockGroupPersistence.updateGroupAppResourceMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        when(mockGroupPersistence.getGroup(anyString())).thenReturn(mockGroup);
        when(mockAppPersistence.getResourceTemplateNames(anyString(), anyString())).thenReturn(Collections.singletonList(resourceName));
        groupAppResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);
        verify(mockGroupPersistence).updateGroupAppResourceMetaData(eq(resourceIdentifier.groupName), eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData));
        verify(mockAppPersistence).updateResourceMetaData(eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData), eq("mockJvm"), eq(resourceIdentifier.groupName));
    }

    @Test
    public void testUpdateMetaDataCallsSuccessor() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName).setGroupName("not-a-web-app").build();

        groupAppResourceHandler.updateResourceMetaData(notMeResourceIdentifier, resourceName, updatedMetaData);
        verify(mockSuccessor).updateResourceMetaData(eq(notMeResourceIdentifier), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testGetSelectedValue() {
        // group level entities don't return a selected value
        Object selectedValue = groupAppResourceHandler.getSelectedValue(resourceIdentifier);
        assertNull(selectedValue);
    }

    @Test
    public void testGetSelectedValueCallsSuccessor() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("whats-app-amiright").setGroupName("not-a-web-app").build();

        groupAppResourceHandler.getSelectedValue(notMeResourceIdentifier);
        verify(mockSuccessor).getSelectedValue(notMeResourceIdentifier);
    }

    @Test
    public void testGetResourceNames() {
        groupAppResourceHandler.getResourceNames(resourceIdentifier);
        verify(mockResourceDao).getGroupLevelAppResourceNames(eq(resourceIdentifier.groupName), eq(resourceIdentifier.webAppName));

        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("whats-app-amiright").setGroupName("not-a-web-app").build();
        groupAppResourceHandler.getResourceNames(notMeResourceIdentifier);
        verify(mockSuccessor).getResourceNames(notMeResourceIdentifier);

    }

}
