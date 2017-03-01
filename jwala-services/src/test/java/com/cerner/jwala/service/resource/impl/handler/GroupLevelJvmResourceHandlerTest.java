package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 9/29/2016.
 */
public class GroupLevelJvmResourceHandlerTest {

    private GroupLevelJvmResourceHandler groupLevelJvmResourceHandler;

    @Mock
    private ResourceHandler mockSuccessor;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private JvmPersistenceService mockJvmPersistence;

    @Mock
    private GroupPersistenceService mockGroupPersistence;

    private ResourceIdentifier resourceIdentifier;

    @Before
    public void setup() {
        resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("resource-name")
                .setGroupName("jvm-group")
                .setJvmName("*").build();
        mockResourceDao = mock(ResourceDao.class);
        mockJvmPersistence = mock(JvmPersistenceService.class);
        mockGroupPersistence = mock(GroupPersistenceService.class);
        mockSuccessor = mock(ResourceHandler.class);
        groupLevelJvmResourceHandler = new GroupLevelJvmResourceHandler(mockResourceDao, mockGroupPersistence, mockJvmPersistence, mockSuccessor);
    }

    @Test
    public void testUpdateMetaData() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        Set<Jvm> groupSet = new HashSet<>();
        groupSet.add(mockJvm);

        when(mockGroup.getJvms()).thenReturn(groupSet);
        when(mockJvm.getJvmName()).thenReturn("jvm-name");
        when(mockGroupPersistence.getGroup(anyString())).thenReturn(mockGroup);
        when(mockGroupPersistence.updateGroupJvmResourceMetaData(anyString(), anyString(), anyString())).thenReturn(updatedMetaData);

        groupLevelJvmResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);

        verify(mockGroupPersistence).updateGroupJvmResourceMetaData(eq(resourceIdentifier.groupName), eq(resourceName), eq(updatedMetaData));
        verify(mockJvmPersistence).updateResourceMetaData(eq(mockJvm.getJvmName()), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testUpdateMetaDataCallsSuccessor() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName).setGroupName("not-a-jvm").build();

        groupLevelJvmResourceHandler.updateResourceMetaData(notMeResourceIdentifier, resourceName, updatedMetaData);
        verify(mockSuccessor).updateResourceMetaData(eq(notMeResourceIdentifier), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testGetSelectedValue() {
        // group level entities don't return a selected value
        Object selectedValue = groupLevelJvmResourceHandler.getSelectedValue(resourceIdentifier);
        assertNull(selectedValue);
    }

    @Test
    public void testGetSelectedValueCallsSuccessor() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("jvm-resource").setGroupName("not-a-jvm").build();

        groupLevelJvmResourceHandler.getSelectedValue(notMeResourceIdentifier);
        verify(mockSuccessor).getSelectedValue(notMeResourceIdentifier);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testGetResourceNames() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("what-group-level-jvm").setGroupName("not-a-jvm").build();
        groupLevelJvmResourceHandler.getResourceNames(notMeResourceIdentifier);
        verify(mockSuccessor).getResourceNames(notMeResourceIdentifier);

        groupLevelJvmResourceHandler.getResourceNames(resourceIdentifier);
    }

}
