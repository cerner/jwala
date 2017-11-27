package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaGroupAppConfigTemplate;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.exception.GroupLevelAppResourceHandlerException;
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
        final String updatedMetaData = "{\"templateName\":\"test-template-name\", \"contentType\":\"application/zip\", \"deployFileName\":\"test-app.war\", \"deployPath\":\"/fake/deploy/path\", \"entity\":{}, \"unpack\":\"true\", \"overwrite\":\"true\"}";
        final String resourceName = "update-my-meta-data.txt";

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("mockJvm");
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockGroup.getName()).thenReturn("test-group-name");

        when(mockGroupPersistence.updateGroupAppResourceMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        when(mockGroupPersistence.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("{\"entity\":{\"deployToJvms\":true}}");
        when(mockGroupPersistence.getGroup(anyString())).thenReturn(mockGroup);
        when(mockAppPersistence.getResourceTemplateNames(anyString(), anyString())).thenReturn(Collections.singletonList(resourceName));

        Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("test-app-name");
        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1111L));
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockApplication.getWebAppContext()).thenReturn("/test-app-context");
        when(mockApplication.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockApplication.isSecure()).thenReturn(true);
        when(mockAppPersistence.getApplication(anyString())).thenReturn(mockApplication);

        groupAppResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);

        verify(mockGroupPersistence).updateGroupAppResourceMetaData(eq(resourceIdentifier.groupName), eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData));
        verify(mockAppPersistence).updateResourceMetaData(eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData), eq("mockJvm"), eq(resourceIdentifier.groupName));
        verify(mockAppPersistence).updateApplication(any(UpdateApplicationRequest.class));
    }

    @Test
    public void testUpdateMetaDataSetDeployToJvmsTrue() {
        reset(mockResourceDao, mockAppPersistence);
        final String updatedMetaData = "{\"templateName\":\"test-template-name\", \"contentType\":\"application/zip\", \"deployFileName\":\"test-app.war\", \"deployPath\":\"/fake/deploy/path\", \"entity\":{}, \"unpack\":\"true\", \"overwrite\":\"true\"}";
        final String resourceName = "update-my-meta-data.txt";

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("mockJvm");
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockGroup.getName()).thenReturn("test-group-name");

        when(mockGroupPersistence.updateGroupAppResourceMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        when(mockGroupPersistence.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("{\"entity\":{\"deployToJvms\":false}}");
        when(mockGroupPersistence.getGroup(anyString())).thenReturn(mockGroup);
        when(mockAppPersistence.getResourceTemplateNames(anyString(), anyString())).thenReturn(Collections.singletonList(resourceName));

        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(mockJvmPersistence.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);

        JpaGroupAppConfigTemplate mockGroupAppConfigTemplate = mock(JpaGroupAppConfigTemplate.class);
        when(mockGroupAppConfigTemplate.getTemplateContent()).thenReturn("some template content");
        when(mockResourceDao.getGroupLevelAppResource(anyString(), anyString(), anyString())).thenReturn(mockGroupAppConfigTemplate);

        Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("app-name");
        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1111L));
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockApplication.getWebAppContext()).thenReturn("/test-app-context");
        when(mockApplication.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockApplication.isSecure()).thenReturn(true);
        when(mockAppPersistence.getApplication(anyString())).thenReturn(mockApplication);
        when(mockAppPersistence.findApplicationsBelongingTo(anyString())).thenReturn(Collections.singletonList(mockApplication));

        groupAppResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);

        verify(mockGroupPersistence).updateGroupAppResourceMetaData(eq(resourceIdentifier.groupName), eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData));
        verify(mockAppPersistence).updateResourceMetaData(eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData), eq("mockJvm"), eq(resourceIdentifier.groupName));
        verify(mockAppPersistence).updateApplication(any(UpdateApplicationRequest.class));
        verify(mockResourceDao, never()).deleteAppResource(anyString(), anyString(), anyString());
        verify(mockAppPersistence).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testUpdateMetaDataSetDeployToJvmsFalse() {
        reset(mockResourceDao, mockAppPersistence);
        final String updatedMetaData = "{\"templateName\":\"test-template-name\", \"contentType\":\"application/zip\", \"deployFileName\":\"test-app.war\", \"deployPath\":\"/fake/deploy/path\", \"entity\":{\"deployToJvms\":false}, \"unpack\":\"true\", \"overwrite\":\"true\"}";
        final String resourceName = "update-my-meta-data.txt";

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("mockJvm");
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockGroup.getName()).thenReturn("test-group-name");

        when(mockGroupPersistence.updateGroupAppResourceMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        when(mockGroupPersistence.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("{\"entity\":{}}");
        when(mockGroupPersistence.getGroup(anyString())).thenReturn(mockGroup);
        when(mockAppPersistence.getResourceTemplateNames(anyString(), anyString())).thenReturn(Collections.singletonList(resourceName));

        Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("app-name");
        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1111L));
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockApplication.getWebAppContext()).thenReturn("/test-app-context");
        when(mockApplication.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockApplication.isSecure()).thenReturn(true);
        when(mockAppPersistence.getApplication(anyString())).thenReturn(mockApplication);

        groupAppResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);

        verify(mockGroupPersistence).updateGroupAppResourceMetaData(eq(resourceIdentifier.groupName), eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData));
        verify(mockAppPersistence).updateResourceMetaData(eq(resourceIdentifier.webAppName), eq(resourceName), eq(updatedMetaData), eq("mockJvm"), eq(resourceIdentifier.groupName));
        verify(mockAppPersistence).updateApplication(any(UpdateApplicationRequest.class));
        verify(mockResourceDao, times(1)).deleteAppResource(eq(resourceName), eq(resourceIdentifier.webAppName), anyString());
        verify(mockAppPersistence, never()).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test (expected = GroupLevelAppResourceHandlerException.class)
    public void testUpdateMetaDataFailsToParseMetaData() {
        reset(mockResourceDao, mockAppPersistence);
        final String updatedMetaData = "{\"templateName\":\"test-template-name\", \"contentType\":\"application/zip\", \"deployFileName\":\"test-app.war\", \"deployPath\":\"/fake/deploy/path\", \"entity\":{\"deployToJvms\":false}, \"unpack\":\"true\", \"overwrite\":\"true\"}";
        final String resourceName = "update-my-meta-data.txt";

        Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("mockJvm");
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockGroup.getName()).thenReturn("test-group-name");

        when(mockGroupPersistence.updateGroupAppResourceMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(updatedMetaData);
        when(mockGroupPersistence.getGroupAppResourceTemplateMetaData(anyString(), anyString(), anyString())).thenReturn("{\"entity\":{},,,}");
        when(mockGroupPersistence.getGroup(anyString())).thenReturn(mockGroup);
        when(mockAppPersistence.getResourceTemplateNames(anyString(), anyString())).thenReturn(Collections.singletonList(resourceName));

        Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("app-name");
        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1111L));
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockApplication.getWebAppContext()).thenReturn("/test-app-context");
        when(mockApplication.isLoadBalanceAcrossServers()).thenReturn(true);
        when(mockApplication.isSecure()).thenReturn(true);
        when(mockAppPersistence.getApplication(anyString())).thenReturn(mockApplication);

        groupAppResourceHandler.updateResourceMetaData(resourceIdentifier, resourceName, updatedMetaData);
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
