package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
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

/**
 * Created on 9/29/2016.
 */
public class JvmResourceHandlerTest {

    private JvmResourceHandler jvmResourceHandler;

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
    public void setup(){
        resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("resource-name")
                .setGroupName("jvm-group")
                .setJvmName("jvm-name").build();
        mockResourceDao = mock(ResourceDao.class);
        mockJvmPersistence = mock(JvmPersistenceService.class);
        mockSuccessor = mock(ResourceHandler.class);
        mockGroupPersistence = mock(GroupPersistenceService.class);
        jvmResourceHandler = new JvmResourceHandler(mockResourceDao, mockGroupPersistence, mockJvmPersistence, mockSuccessor);
    }

    @Test
    public void testUpdateMetaData() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";

        when(mockJvmPersistence.updateResourceMetaData(anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        jvmResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);
        verify(mockJvmPersistence).updateResourceMetaData(eq(resourceIdentifier.jvmName), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testUpdateMetaDataCallsSuccessor() {
        final String updatedMetaData = "{\"updated\":\"meta-data\"}";
        final String resourceName = "update-my-meta-data.txt";
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName(resourceName).setGroupName("not-a-jvm").build();

        jvmResourceHandler.updateResourceMetaData(notMeResourceIdentifier, resourceName, updatedMetaData);
        verify(mockSuccessor).updateResourceMetaData(eq(notMeResourceIdentifier), eq(resourceName), eq(updatedMetaData));
    }

    @Test
    public void testGetSelectedValue() {
        Jvm mockJvm = mock(Jvm.class);

        when(mockJvmPersistence.findJvmByExactName(anyString())).thenReturn(mockJvm);

        Jvm jvm = (Jvm) jvmResourceHandler.getSelectedValue(resourceIdentifier);
        assertEquals(mockJvm, jvm);
    }

    @Test
    public void testGetSelectedValueCallsSuccessor() {
        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("jvm-resource").setGroupName("not-a-jvm").build();

        jvmResourceHandler.getSelectedValue(notMeResourceIdentifier);
        verify(mockSuccessor).getSelectedValue(notMeResourceIdentifier);
    }

    @Test
    public void testGetResourceNames() {
        jvmResourceHandler.getResourceNames(resourceIdentifier);
        verify(mockJvmPersistence).getResourceTemplateNames(anyString());

        ResourceIdentifier notMeResourceIdentifier = new ResourceIdentifier.Builder().setResourceName("what-jvm").setGroupName("not-a-jvm").build();
        jvmResourceHandler.getResourceNames(notMeResourceIdentifier);
        verify(mockSuccessor).getResourceNames(notMeResourceIdentifier);
    }
}
