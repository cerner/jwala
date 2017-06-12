package com.cerner.jwala.service.resource;

import com.cerner.jwala.common.JwalaUtils;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.History;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.*;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
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
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;
import com.cerner.jwala.service.resource.impl.ResourceContentGeneratorServiceImpl;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.resource.impl.ResourceServiceImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.*;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ResourceService}.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ResourceServiceImplTest.Config.class})
@PrepareForTest(JwalaUtils.class )
public class ResourceServiceImplTest {

    @Autowired
    private ResourceService resourceService;

    @Before
    public void setup() throws Exception{
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        PowerMockito.mockStatic(JwalaUtils.class);
        PowerMockito.when(JwalaUtils.getHostAddress("testServer")).thenReturn(Inet4Address.getLocalHost().getHostAddress());
        PowerMockito.when(JwalaUtils.getHostAddress("testServer2")).thenReturn(Inet4Address.getLocalHost().getHostAddress());

        // It is good practice to start with a clean sheet of paper before each test that is why resourceService is
        // initialized here. This makes sure that unrelated tests don't affect each other.
        MockitoAnnotations.initMocks(this);
        reset(Config.mockHistoryFacadeService);
        reset(Config.mockJvmPersistenceService);
        reset(Config.mockWebServerPersistenceService);
        reset(Config.mockGroupPesistenceService);
        reset(Config.mockAppPersistenceService);

        when(Config.mockJvmPersistenceService.findJvmByExactName(eq("someJvm"))).thenReturn(mock(Jvm.class));
        when(Config.mockRepositoryService.upload(anyString(), any(InputStream.class))).thenReturn("thePath");
    }

    @Test
    public void testEncryption() {
        final String encryptedHello = "aGVsbG8=";
        final String clearTextHello = "hello";
        assertEquals(encryptedHello, resourceService.encryptUsingPlatformBean(clearTextHello));
    }

    @Test
    public void testCreate() {
        assertNotNull("");
    }

    @Test
    public void testUpdateAttributes() {
        assertNotNull("");
    }

    @Test
    public void testUpdateFriendlyName() {
        assertNotNull("");
    }

    @Test
    public void testRead() {
        assertNotNull("");
    }

    @Test
    public void getType() {
        assertNotNull("");
    }

    @Test
    public void testCreateJvmTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-jvm-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/server.xml.tpl");
        Group mockGroup = mock(Group.class);
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getAjpPort()).thenReturn(9103);
        when(mockJvm.getErrorStatus()).thenReturn(null);
        when(mockJvm.getGroups()).thenReturn(new HashSet<Group>());
        when(mockJvm.getHostName()).thenReturn("localhost");
        when(mockJvm.getHttpPort()).thenReturn(9100);
        when(mockJvm.getHttpsPort()).thenReturn(9101);
        when(mockJvm.getJvmName()).thenReturn("some jvm");
        when(mockJvm.getRedirectPort()).thenReturn(9102);
        when(mockJvm.getShutdownPort()).thenReturn(-1);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(mockJvm.getStateLabel()).thenReturn(null);
        when(mockJvm.getStatusPath()).thenReturn(null);
        when(mockJvm.getStatusUri()).thenReturn(null);
        when(mockJvm.getSystemProperties()).thenReturn(null);
        when(Config.mockGroupPesistenceService.getGroup(anyString())).thenReturn(mockGroup);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some jvm", mockUser);
        verify(Config.mockJvmPersistenceService).findJvmByExactName("some jvm");
        verify(Config.mockJvmPersistenceService).uploadJvmConfigTemplate(any(UploadJvmConfigTemplateRequest.class));
    }

    @Test
    public void testCreateGroupedJvmsTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-grouped-jvms-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/server.xml.tpl");

        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mock(Jvm.class));
        jvmSet.add(mock(Jvm.class));
        final Group mockGroup = mock(Group.class);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(Config.mockGroupPesistenceService.getGroup(eq("HEALTH CHECK 4.0"))).thenReturn(mockGroup);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-app-name", mockUser);
        verify(Config.mockJvmPersistenceService, new Times(2)).uploadJvmConfigTemplate(any(UploadJvmConfigTemplateRequest.class));
        verify(Config.mockGroupPesistenceService).populateGroupJvmTemplates(eq("HEALTH CHECK 4.0"), any(List.class));
    }

    @Test
    public void testCreateWebServerTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-ws-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/httpd.conf.tpl");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some webserver", mockUser);
        verify(Config.mockWebServerPersistenceService).findWebServerByName("some webserver");
        verify(Config.mockWebServerPersistenceService).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class), eq("/conf/httpd.conf"), eq("user-id"));
    }

    @Test
    public void testCreateGroupedWebServersTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-grouped-ws-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/httpd.conf.tpl");

        final Set<WebServer> webServerSet = new HashSet<>();
        webServerSet.add(mock(WebServer.class));
        webServerSet.add(mock(WebServer.class));
        final Group mockGroup = mock(Group.class);
        when(mockGroup.getWebServers()).thenReturn(webServerSet);
        when(mockGroup.getName()).thenReturn("HEALTH CHECK 4.0");
        when(Config.mockGroupPesistenceService.getGroupWithWebServers(eq("HEALTH CHECK 4.0"))).thenReturn(mockGroup);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-app-name", mockUser);
        verify(Config.mockWebServerPersistenceService, new Times(2)).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class), eq("/conf/httpd.conf"), eq("user-id"));
        verify(Config.mockGroupPesistenceService).populateGroupWebServerTemplates(eq("HEALTH CHECK 4.0"), anyMap());
    }

    @Test
    public void testCreateAppTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-app-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/app.xml.tpl");
        Jvm mockJvm = mock(Jvm.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some application", mockUser);
        verify(Config.mockJvmPersistenceService).findJvmByExactName("some jvm name");
        verify(Config.mockAppPersistenceService).getApplication("some application");
        verify(Config.mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test (expected = ResourceServiceException.class)
    public void testCreateAppTemplateWithNullEntity() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-app-template-test-null-entity-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/app.xml.tpl");
        Jvm mockJvm = mock(Jvm.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some application", mockUser);
        verify(Config.mockJvmPersistenceService).findJvmByExactName("some jvm name");
        verify(Config.mockAppPersistenceService).getApplication("some application");
        verify(Config.mockAppPersistenceService, never()).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test (expected = ResourceServiceException.class)
    public void testCreateAppTemplateWithEmptyEntity() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-app-template-test-empty-entity-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/app.xml.tpl");
        Jvm mockJvm = mock(Jvm.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "", mockUser);
        verify(Config.mockJvmPersistenceService, never()).findJvmByExactName("some jvm name");
        verify(Config.mockAppPersistenceService, never()).getApplication("");
        verify(Config.mockAppPersistenceService, never()).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testCreateAppTemplateBinary() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-app-template-test-metadata-binary.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/app.xml.tpl");
        Jvm mockJvm = mock(Jvm.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(Config.mockJvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(Config.mockJvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        when(Config.mockRepositoryService.upload(anyString(), any(InputStream.class))).thenReturn("./anyPath");
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "some application", mockUser);
        verify(Config.mockJvmPersistenceService).findJvmByExactName("some jvm name");
        verify(Config.mockAppPersistenceService).getApplication("some application");
        verify(Config.mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testCreateGroupedAppsTemplate() {
        final InputStream metaDataIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/create-grouped-apps-template-test-metadata.json");
        final InputStream templateIn = this.getClass().getClassLoader()
                .getResourceAsStream("resource-service-test-files/httpd.conf.tpl");

        final List<Application> appList = new ArrayList<>();
        final Application mockApp = mock(Application.class);
        final Application mockApp2 = mock(Application.class);
        when(mockApp.getName()).thenReturn("test-app-name");
        when(mockApp2.getName()).thenReturn("test-app-name2");
        appList.add(mockApp);
        appList.add(mockApp2);
        final Group mockGroup = mock(Group.class);
        Set<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvmSet.add(mockJvm);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-name");
        when(Config.mockAppPersistenceService.findApplicationsBelongingTo(eq("HEALTH CHECK 4.0"))).thenReturn(appList);
        when(Config.mockGroupPesistenceService.getGroup(eq("HEALTH CHECK 4.0"))).thenReturn(mockGroup);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn("user-id");
        resourceService.createTemplate(metaDataIn, templateIn, "test-app-name", mockUser);
        verify(Config.mockAppPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
        verify(Config.mockGroupPesistenceService).populateGroupAppTemplate(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGenerateResourceFile() {
        File httpdTemplate = new File("../jwala-common/src/test/resources/HttpdConfTemplate.tpl");
        try {
            List<Group> groups = new ArrayList<>();
            List<Jvm> jvms = new ArrayList<>();
            List<WebServer> webServers = new ArrayList<>();
            List<Application> applications = new ArrayList<>();
            Group group = new Group(new Identifier<Group>(1111L),
                    "groupName",
                    new HashSet<>(jvms),
                    new HashSet<>(webServers),
                    new HashSet<History>(),
                    new HashSet<>(applications));
            groups.add(group);
            applications.add(new Application(new Identifier<Application>(111L), "hello-world-1", "d:/jwala/app/archive", "/hello-world-1", group, true, true, false, "testWar.war"));
            applications.add(new Application(new Identifier<Application>(222L), "hello-world-2", "d:/jwala/app/archive", "/hello-world-2", group, true, true, false, "testWar.war"));
            applications.add(new Application(new Identifier<Application>(333L), "hello-world-3", "d:/jwala/app/archive", "/hello-world-3", group, true, true, false, "testWar.war"));
            WebServer webServer = new WebServer(new Identifier<WebServer>(1L), groups, "Apache2.4", "localhost", 80, 443,
                    new com.cerner.jwala.common.domain.model.path.Path("/statusPath"), WebServerReachableState.WS_UNREACHABLE, null);
            webServers.add(webServer);
            jvms.add(new Jvm(new Identifier<Jvm>(11L), "tc1", "someHostGenerateMe", new HashSet<>(groups), 11010, 11011, 11012, -1, 11013,
                    new com.cerner.jwala.common.domain.model.path.Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null, null, null, null));
            jvms.add(new Jvm(new Identifier<Jvm>(22L), "tc2", "someHostGenerateMe", new HashSet<>(groups), 11020, 11021, 11022, -1, 11023,
                    new com.cerner.jwala.common.domain.model.path.Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "", null, null, null, null, null, null, null));

            when(Config.mockGroupPesistenceService.getGroups()).thenReturn(groups);
            when(Config.mockAppPersistenceService.findApplicationsBelongingTo(anyString())).thenReturn(applications);
            when(Config.mockJvmPersistenceService.getJvmsAndWebAppsByGroupName(anyString())).thenReturn(jvms);
            when(Config.mockWebServerPersistenceService.getWebServersByGroupName(anyString())).thenReturn(webServers);

            System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                    this.getClass().getClassLoader().getResource("vars.properties").getPath().replace("vars.properties", ""));

            final ResourceGroup resourceGroup = resourceService.generateResourceGroup();
            String output = resourceService.generateResourceFile(ResourceGroovyMethods.getText(httpdTemplate), ResourceGroovyMethods.getText(httpdTemplate), resourceGroup, webServer, ResourceGeneratorType.TEMPLATE);

            String expected = ResourceGroovyMethods.getText(new File("../jwala-common/src/test/resources/HttpdConfTemplate-EXPECTED.conf"));
            expected = expected.replaceAll("\\r", "").replaceAll("\\n", "");
            output = output.replaceAll("\\r", "").replaceAll("\\n", "");
            String diff = StringUtils.difference(output, expected);
            assertEquals(expected, output);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCheckFileExists() throws IOException {
        final String testGroup = "testGroup";
        final String testFile = "testFile";
        final String testJvm = "testJvm";
        final String testApp = "testApp";
        final String testWebServer = "testWebServer";
        Map<String, String> expectedMap = new HashMap<>();
        Map<String, String> result = resourceService.checkFileExists(null, null, null, null, null);
        expectedMap.put("fileName", null);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(null, null, null, null, "");
        expectedMap.put("fileName", new String());
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(testGroup, null, null, null, null);
        expectedMap.put("fileName", null);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(null, null, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);
        result = resourceService.checkFileExists(testGroup, null, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(Config.mockGroupPesistenceService.checkGroupJvmResourceFileName(testGroup, testFile)).thenReturn(false);
        when(Config.mockJvmPersistenceService.checkJvmResourceFileName(testGroup, testJvm, testFile)).thenReturn(true);
        result = resourceService.checkFileExists(testGroup, testJvm, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "true");
        assertEquals(expectedMap, result);

        when(Config.mockGroupPesistenceService.checkGroupJvmResourceFileName(testGroup, testFile)).thenReturn(false);
        when(Config.mockJvmPersistenceService.checkJvmResourceFileName(testGroup, testJvm, testFile)).thenReturn(false);
        result = resourceService.checkFileExists(testGroup, testJvm, null, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(Config.mockGroupPesistenceService.checkGroupAppResourceFileName(testGroup, testFile)).thenReturn(false);
        when(Config.mockAppPersistenceService.checkAppResourceFileName(testGroup, testApp, testFile)).thenReturn(false);
        result = resourceService.checkFileExists(testGroup, null, testApp, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(Config.mockGroupPesistenceService.checkGroupAppResourceFileName(testGroup, testFile)).thenReturn(false);
        when(Config.mockAppPersistenceService.checkAppResourceFileName(testGroup, testApp, testFile)).thenReturn(true);
        result = resourceService.checkFileExists(testGroup, null, testApp, null, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "true");
        assertEquals(expectedMap, result);

        when(Config.mockGroupPesistenceService.checkGroupWebServerResourceFileName(testGroup, testFile)).thenReturn(false);
        when(Config.mockWebServerPersistenceService.checkWebServerResourceFileName(testGroup, testWebServer, testFile)).thenReturn(false);
        result = resourceService.checkFileExists(testGroup, null, null, testWebServer, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "false");
        assertEquals(expectedMap, result);

        when(Config.mockGroupPesistenceService.checkGroupWebServerResourceFileName(testGroup, testFile)).thenReturn(false);
        when(Config.mockWebServerPersistenceService.checkWebServerResourceFileName(testGroup, testWebServer, testFile)).thenReturn(true);
        result = resourceService.checkFileExists(testGroup, null, null, testWebServer, testFile);
        expectedMap.put("fileName", testFile);
        expectedMap.put("exists", "true");
        assertEquals(expectedMap, result);
    }

    @Test
    public void testGetExternalProperties() {
        Properties result = resourceService.getExternalProperties();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetResourcesContent() {
        ResourceIdentifier identifier = mock(ResourceIdentifier.class);
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(mockConfigTemplate.getMetaData()).thenReturn("{}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("key=value");
        when(Config.mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);

        ResourceContent result = resourceService.getResourceContent(identifier);
        assertEquals("{}", result.getMetaData());
        assertEquals("key=value", result.getContent());
    }

    @Test
    public void testGetResourceContentWhenNull() {
        ResourceIdentifier identifier = mock(ResourceIdentifier.class);
        when(Config.mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(null);

        ResourceContent result = resourceService.getResourceContent(identifier);
        assertNull(result);
    }

    @Test
    public void testUpdateResourceContent() {
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier identifier = idBuilder.setResourceName("external.properties").build();
        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(mockConfigTemplate.getTemplateContent()).thenReturn("newkey=newvalue");
        when(Config.mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);

        String result = resourceService.updateResourceContent(identifier, "newkey=newvalue");
        assertEquals("newkey=newvalue", result);
        verify(Config.mockResourceDao).updateResource(eq(identifier), eq(EntityType.EXT_PROPERTIES), eq("newkey=newvalue"));
    }

    @Test
    public void testPreviewResourceContent() {
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier resourceId = idBuilder.setResourceName("external.properties").build();
        String result = resourceService.previewResourceContent(resourceId, "key=value");
        assertEquals("key=value", result);
    }

    @Test
    public void testGetExternalPropertiesFile() {
        List<String> resultList = new ArrayList<>();
        resultList.add("external.properties");
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        ResourceIdentifier resourceId = idBuilder.setResourceName("external.properties").build();
        when(Config.mockResourceDao.getResourceNames(any(ResourceIdentifier.class), any(EntityType.class))).thenReturn(resultList);

        List<String> result = resourceService.getResourceNames(resourceId);
        verify(Config.mockResourceDao).getResourceNames(eq(resourceId), eq(EntityType.EXT_PROPERTIES));
        assertEquals("external.properties", result.get(0));
    }

    @Test
    public void testUploadResource() {
        when(Config.mockRepositoryService.upload(anyString(), any(InputStream.class))).thenReturn("thePath");
        assertEquals("thePath", resourceService.uploadResource(mock(ResourceTemplateMetaData.class), new ByteArrayInputStream("data".getBytes())));
    }

    @Test
    public void testGetExternalPropertiesAsFile() throws IOException {

        // test for an existing external properties file
        when(Config.mockResourceDao.getResourceNames(any(ResourceIdentifier.class), any(EntityType.class))).thenReturn(new ArrayList<String>());
        boolean exceptionThrown = false;
        try {
            resourceService.getExternalPropertiesAsFile();
        } catch (InternalErrorException iee) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        // now test the success case
        final ArrayList<String> extPropertiesResourceNames = new ArrayList<>();
        extPropertiesResourceNames.add("external.properties");
        final ConfigTemplate extPropertiesConfigTemplate = new ConfigTemplate();
        extPropertiesConfigTemplate.setMetaData("{\"deployPath\":\"c:/fake/path\", \"deployFileName\":\"external.properties\"}");
        extPropertiesConfigTemplate.setTemplateContent("key=value");

        when(Config.mockResourceDao.getResourceNames(any(ResourceIdentifier.class), any(EntityType.class))).thenReturn(extPropertiesResourceNames);
        when(Config.mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(extPropertiesConfigTemplate);
        when(Config.mockGroupPesistenceService.getGroups()).thenReturn(new ArrayList<Group>());

        File result = resourceService.getExternalPropertiesAsFile();
        assertTrue(result.length() > 0);
        assertTrue(result.delete());
    }

    @Test
    public void testGetExternalPropertiesAsString() {
        String result = resourceService.getExternalPropertiesAsString();
        assertEquals("newkey=newvalue\n", result);
    }

    @Test
    public void testDeleteGroupLevelAppResources() throws IOException {
        List<String> templateList = new ArrayList<>();
        templateList.add("test.war");
        List<Jvm> jvms = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Application mockApp = mock(Application.class);
        when(Config.mockResourceDao.deleteGroupLevelAppResources(anyString(), anyString(), anyList())).thenReturn(1);
        when(Config.mockJvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(jvms);
        when(Config.mockResourceDao.deleteAppResources(anyList(), anyString(), anyString())).thenReturn(1);
        when(Config.mockAppPersistenceService.getApplication(anyString())).thenReturn(mockApp);
        when(Config.mockAppPersistenceService.deleteWarInfo(anyString())).thenReturn(mockApp);
        assertEquals(1, resourceService.deleteGroupLevelAppResources("test-app", "test-group", templateList));
    }

    @Test
    public void testDeleteGroupLevelAppResourcesFail() throws IOException {
        List<String> templateList = new ArrayList<>();
        templateList.add("test.war");
        List<Jvm> jvms = new ArrayList<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Application mockApp = mock(Application.class);
        when(Config.mockResourceDao.deleteGroupLevelAppResources(anyString(), anyString(), anyList())).thenReturn(1);
        when(Config.mockJvmPersistenceService.getJvmsByGroupName(anyString())).thenReturn(jvms);
        when(Config.mockResourceDao.deleteAppResources(anyList(), anyString(), anyString())).thenReturn(1);
        when(Config.mockAppPersistenceService.getApplication(anyString())).thenReturn(mockApp);
        when(Config.mockAppPersistenceService.deleteWarInfo(anyString())).thenReturn(mockApp);
        doThrow(RepositoryServiceException.class).when(Config.mockRepositoryService).delete(anyString());
        assertEquals(1, resourceService.deleteGroupLevelAppResources("test-app", "test-group", templateList));
    }

    @Test
    public void testCreateResource() {
        final ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        final ResourceTemplateMetaData resourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        when(resourceTemplateMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        final InputStream inputStream = mock(InputStream.class);
        CreateResourceResponseWrapper createResourceResponseWrapper = mock(CreateResourceResponseWrapper.class);
        when(Config.mockResourceHandler.createResource(eq(resourceIdentifier), eq(resourceTemplateMetaData), anyString())).thenReturn(createResourceResponseWrapper);
        assertEquals(createResourceResponseWrapper, resourceService.createResource(resourceIdentifier, resourceTemplateMetaData, inputStream));
    }

    @Test(expected = ResourceServiceException.class)
    public void testCreateResourceFail() {
        final ResourceIdentifier resourceIdentifier = mock(ResourceIdentifier.class);
        final ResourceTemplateMetaData resourceTemplateMetaData = mock(ResourceTemplateMetaData.class);
        when(resourceTemplateMetaData.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        final InputStream inputStream = mock(InputStream.class);
        when(Config.mockResourceHandler.createResource(eq(resourceIdentifier), eq(resourceTemplateMetaData), anyString())).thenThrow(ResourceServiceException.class);
        resourceService.createResource(resourceIdentifier, resourceTemplateMetaData, inputStream);
    }

    @Test
    public void testDeleteWebServerResource() {
        when(Config.mockResourceDao.deleteWebServerResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteWebServerResource("testFilename", "testWebServer"));
    }

    @Test
    public void testDeleteGroupLevelWebServerResource() {
        when(Config.mockResourceDao.deleteGroupLevelWebServerResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteGroupLevelWebServerResource("testFilename", "testGroupName"));
    }

    @Test
    public void testDeleteJvmResource() {
        when(Config.mockResourceDao.deleteJvmResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteJvmResource("testFilename", "testJvm"));
    }

    @Test
    public void testDeleteGroupLevelJvmResource() {
        when(Config.mockResourceDao.deleteGroupLevelJvmResource(anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteGroupLevelJvmResource("testFilename", "testGroupName"));
    }

    @Test
    public void testDeleteAppResource() {
        when(Config.mockResourceDao.deleteAppResource(anyString(), anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteAppResource("testFilename", "testApp", "testJvm"));
    }

    @Test
    public void testDeleteGroupLevelAppResource() {
        Application application = mock(Application.class);
        when(Config.mockAppPersistenceService.getApplication(anyString())).thenReturn(application);
        when(application.getName()).thenReturn("testApp");
        Group group = mock(Group.class);
        when(application.getGroup()).thenReturn(group);
        when(group.getName()).thenReturn("testGroup");
        when(Config.mockResourceDao.deleteGroupLevelAppResource(anyString(), anyString(), anyString())).thenReturn(1);
        assertEquals(1, resourceService.deleteGroupLevelAppResource("testAppName", "testFilename"));
    }

    @Test
    public void testGetApplicationResourceNames() {
        List<String> strings = new ArrayList<>();
        final String groupName = "testGroupName";
        final String appName = "testAppName";
        when(Config.mockResourcePersistenceService.getApplicationResourceNames(eq(groupName), eq(appName))).thenReturn(strings);
        assertEquals(strings, resourceService.getApplicationResourceNames(groupName, appName));
    }

    @Test
    public void testGetAppTemplate() {
        final String result = "test";
        final String groupName = "testGroupName";
        final String appName = "testAppName";
        final String templateName = "testTemplate";
        when(Config.mockResourcePersistenceService.getAppTemplate(eq(groupName), eq(appName), eq(templateName))).thenReturn(result);
        assertEquals(result, resourceService.getAppTemplate(groupName, appName, templateName));
    }

    @Test
    public void testUpdateResourceMetaData() {
        ResourceIdentifier mockResourceIdentifier = mock(ResourceIdentifier.class);
        when(Config.mockResourceHandler.updateResourceMetaData(any(ResourceIdentifier.class), anyString(), anyString())).thenReturn("{}");
        final String resourceName = "test-resource-name";
        final String metaData = "{\"key\":\"value\"}";
        resourceService.updateResourceMetaData(mockResourceIdentifier, resourceName, metaData);
        verify(Config.mockResourceHandler).updateResourceMetaData(eq(mockResourceIdentifier), eq(resourceName), eq(metaData));
    }

    @Test
    public void testGetFormattedMetaData() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("./src/test/resources/vars.properties")));

        final String rawMetaData = (String) properties.get("test.path.backslash.escaped"); // --> {"deployPath":"\\\\server\\d$"}
        ResourceTemplateMetaData result = resourceService.getTokenizedMetaData("test-file.txt", null, rawMetaData);
        assertEquals("\\\\server\\d$", result.getDeployPath());
    }

    @Test
    public void testGetMetaData() throws IOException {
        ResourceTemplateMetaData result = resourceService.getMetaData("{\"deployPath\":\"\\\\server\\d$\"}");
        assertEquals("\\\\server\\d$", result.getDeployPath());
    }

    @Test
    public void testMetaDataBackslash() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File("./src/test/resources/vars.properties")));
        final String testPathBackslash = (String) properties.get("test.path.backslash"); // --> {"deployPath":"\\server\d$"}

        // Throws exception if backslashes are not escaped
        boolean exceptionThrown = false;
        Exception exception = null;
        try {
            new ObjectMapper().readValue(testPathBackslash, ResourceTemplateMetaData.class);
        } catch (IOException e) {
            exceptionThrown = true;
            exception = e;
        }
        assertTrue("Back slashes not escaped", exceptionThrown);
        assertNotNull(exception);
        System.out.println(testPathBackslash + " " + exception.getMessage());
        assertTrue(exception.getMessage().startsWith("Unrecognized character escape"));

        // Escaped backslashes in properties file still fails
        final String testPathBackslashEscaped = (String) properties.get("test.path.backslash.escaped"); // --> {"deployPath":"\\\\server\\d$"}
        exceptionThrown = false;
        exception = null;
        try {
            new ObjectMapper().readValue(testPathBackslashEscaped, ResourceTemplateMetaData.class);
        } catch (IOException e) {
            exceptionThrown = true;
            exception = e;
        }
        assertTrue("Back slashes escaped", exceptionThrown);
        assertNotNull(exception);
        System.out.println(testPathBackslashEscaped + " " + exception.getMessage());
        assertTrue(exception.getMessage().startsWith("Unrecognized character escape"));

        // More backslashes works
        final String testPathBackslashThree = (String) properties.get("test.path.backslash.escaped.escaped"); // --> {"deployPath":"\\\\\\\\server\\\\d$"}
        exceptionThrown = false;
        exception = null;
        ResourceTemplateMetaData mappingResult = null;
        try {
            mappingResult = new ObjectMapper().readValue(testPathBackslashThree, ResourceTemplateMetaData.class);
        } catch (IOException e) {
            exceptionThrown = true;
            exception = e;
        }
        assertFalse("Back slashes three", exceptionThrown);
        assertNull(exception);
        System.out.println(testPathBackslashThree + " :: " + mappingResult.getDeployPath());

        // Ok, what we need to do is escape the backslashes before reading the string by the object mapper
        final String escapedEscapedTestPath = testPathBackslashEscaped.replace("\\", "\\\\"); //{"deployPath":"\\\\server\\d$"}
        exceptionThrown = false;
        exception = null;
        mappingResult = null;
        try {
            mappingResult = new ObjectMapper().readValue(escapedEscapedTestPath, ResourceTemplateMetaData.class);
        } catch (IOException e) {
            exceptionThrown = true;
            exception = e;
        }
        assertFalse("Back slashes escaped, and then escaped manually", exceptionThrown);
        assertNull(exception);
        System.out.println("BEFORE:" + escapedEscapedTestPath + " :: AFTER:" + mappingResult.getDeployPath());

    }

    @Test
    public void testResourceValidation() {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("*")
                .build();
        ResourceIdentifier resourceIdentifierServerXml = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("test-server.xml")
                .build();
        ResourceIdentifier resourceIdentifierSetenvBat = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("test-setenv.bat")
                .build();
        ResourceIdentifier resourceIdentifierCatalinaProperties = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("test-catalina.properties")
                .build();

        List<String> resourcesNames = Arrays.asList("test-server.xml", "test-setenv.bat", "test-catalina.properties");

        ConfigTemplate configTemplateCatalinaProps = new ConfigTemplate();
        configTemplateCatalinaProps.setTemplateContent("fake catalina.properties content");
        configTemplateCatalinaProps.setMetaData("{}");
        ConfigTemplate configTemplateServerXml = new ConfigTemplate();
        configTemplateServerXml.setTemplateContent("fake server.xml content");
        configTemplateServerXml.setMetaData("{}");
        ConfigTemplate configTemplateSetenvBat = new ConfigTemplate();
        configTemplateSetenvBat.setTemplateContent("fake setenv.bat content");
        configTemplateSetenvBat.setMetaData("{}");

        Jvm mockJvm = mock(Jvm.class);

        when(Config.mockResourceHandler.getResourceNames(eq(resourceIdentifier))).thenReturn(resourcesNames);
        when(Config.mockResourceHandler.getSelectedValue(eq(resourceIdentifier))).thenReturn(mockJvm);
        when(Config.mockResourceHandler.fetchResource(eq(resourceIdentifierCatalinaProperties))).thenReturn(configTemplateCatalinaProps);
        when(Config.mockResourceHandler.fetchResource(eq(resourceIdentifierServerXml))).thenReturn(configTemplateServerXml);
        when(Config.mockResourceHandler.fetchResource(eq(resourceIdentifierSetenvBat))).thenReturn(configTemplateSetenvBat);

        resourceService.validateAllResourcesForGeneration(resourceIdentifier);
        verify(Config.mockHistoryFacadeService, never()).write(anyString(), anyList(), anyString(), any(EventType.class), anyString());
    }

    @Test
    public void testResourceValidationFails() {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("*")
                .build();
        ResourceIdentifier resourceIdentifierServerXml = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("test-server.xml")
                .build();
        ResourceIdentifier resourceIdentifierSetenvBat = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("test-setenv.bat")
                .build();
        ResourceIdentifier resourceIdentifierCatalinaProperties = new ResourceIdentifier.Builder()
                .setGroupName("test-group-name")
                .setJvmName("test-jvm-name")
                .setResourceName("test-catalina.properties")
                .build();

        List<String> resourcesNames = Arrays.asList("test-server.xml", "test-setenv.bat", "test-catalina.properties");

        ConfigTemplate configTemplateCatalinaProps = new ConfigTemplate();
        configTemplateCatalinaProps.setTemplateContent("fake catalina.properties content\n${property.does.not.exist}\nfake properties");
        configTemplateCatalinaProps.setMetaData("{${property.does.not.exist}}");
        ConfigTemplate configTemplateServerXml = new ConfigTemplate();
        configTemplateServerXml.setTemplateContent("fake server.xml content\n${property.does.not.exist}\nfake properties");
        configTemplateServerXml.setMetaData("{${property.does.not.exist}}");
        ConfigTemplate configTemplateSetenvBat = new ConfigTemplate();
        configTemplateSetenvBat.setTemplateContent("fake setenv.bat content\n${property.does.not.exist}\nfake properties");
        configTemplateSetenvBat.setMetaData("{${property.does.not.exist}}");

        Jvm mockJvm = mock(Jvm.class);
        Authentication mockAuthentication = mock(Authentication.class);

        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);

        when(mockAuthentication.getName()).thenReturn("test-user-name");
        when(Config.mockResourceHandler.getResourceNames(eq(resourceIdentifier))).thenReturn(resourcesNames);
        when(Config.mockResourceHandler.getSelectedValue(eq(resourceIdentifier))).thenReturn(mockJvm);
        when(Config.mockResourceHandler.fetchResource(eq(resourceIdentifierCatalinaProperties))).thenReturn(configTemplateCatalinaProps);
        when(Config.mockResourceHandler.fetchResource(eq(resourceIdentifierServerXml))).thenReturn(configTemplateServerXml);
        when(Config.mockResourceHandler.fetchResource(eq(resourceIdentifierSetenvBat))).thenReturn(configTemplateSetenvBat);

        InternalErrorException iee = null;
        try {
            resourceService.validateAllResourcesForGeneration(resourceIdentifier);
        } catch (InternalErrorException e) {
            iee = e;
        }
        assertNotNull(iee);
        final Map<String, List<String>> errorDetails = iee.getErrorDetails();
        verify(Config.mockHistoryFacadeService, times(6)).write(anyString(), anyList(), anyString(), any(EventType.class), anyString());
        assertEquals(1, errorDetails.size());
        final List<String> exceptionList = errorDetails.get("test-jvm-name");
        assertEquals(6, exceptionList.size());
        assertEquals("METADATA: Failed to bind data and properties to : test-server.xml for Jvm: null. Cause(s) of the failure is/are: Template execution error at line 1:\n", exceptionList.get(0));
        assertTrue(exceptionList.get(1).contains("--> 2: ${property.does.not.exist}"));
    }

    @Test
    public void testSingleResourceValidation() {

        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("test-resource.xml")
                .setJvmName("test-jvm")
                .build();

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-resource-validation");

        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployFileName\":\"\test-deploy-name.xml\", \"deployPath\":\"./fake/test/path\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("template${nope}template");

        when(Config.mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(Config.mockResourceHandler.getSelectedValue(any(ResourceIdentifier.class))).thenReturn(mockJvm);

        resourceService.validateSingleResourceForGeneration(resourceIdentifier);

        verify(Config.mockHistoryFacadeService, never()).write(anyString(), anyList(), anyString(), any(EventType.class), anyString());
    }

    @Test
    public void testSingleResourceValidationFails() {

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("test-user-resource-validation");
        SecurityContextHolder.getContext().setAuthentication(mockAuth);

        ResourceIdentifier resourceIdentifier = new ResourceIdentifier.Builder()
                .setResourceName("test-resource.xml")
                .setJvmName("test-jvm-resource-validation")
                .build();

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm-resource-validation");

        ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(mockConfigTemplate.getMetaData()).thenReturn("{\"deployFileName\":\"${fail.fail}-test-deploy-name.xml\", \"deployPath\":\"./fake/test/path\"}");
        when(mockConfigTemplate.getTemplateContent()).thenReturn("template${nope.fail}template");

        when(Config.mockResourceHandler.fetchResource(any(ResourceIdentifier.class))).thenReturn(mockConfigTemplate);
        when(Config.mockResourceHandler.getSelectedValue(any(ResourceIdentifier.class))).thenReturn(mockJvm);

        InternalErrorException caughtException = null;
        try {
            resourceService.validateSingleResourceForGeneration(resourceIdentifier);
        } catch (InternalErrorException iee) {
            caughtException = iee;
        }

        assertNotNull(caughtException);
        assertEquals(2, caughtException.getErrorDetails().get("test-jvm-resource-validation").size());
    }

    @Test
    public void testGetResourceMimeTypes() throws IOException {
        // jpg
        assertEquals("image/jpeg", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/jpg"))));
        assertEquals("image/jpeg", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/jpg.jpg"))));
        assertEquals("image/jpeg", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/jpg.txt"))));

        // xml
        assertEquals("application/xml", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/xml-tpl"))));
        assertEquals("application/xml", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/xml-tpl.tpl"))));
        assertEquals("application/xml", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/xml-tpl.war"))));
        assertEquals("application/xml", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/xml-tpl.zip"))));

        // war
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/war"))));
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/war.txt"))));
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/war.war"))));
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/war.zip"))));

        // zip
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/zip"))));
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/zip.txt"))));
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/zip.zip"))));

        // properties file/text
        assertEquals("text/plain", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/properties-file-tpl"))));
        assertEquals("text/plain", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/properties-file-tpl.tpl"))));
        assertEquals("text/plain", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/properties-file-tpl.war"))));

        // jar
        assertEquals("application/zip", resourceService.getResourceMimeType(new BufferedInputStream(
                this.getClass().getResourceAsStream("/get-resource-mime-type-test-files/jar.jar"))));
    }

    @Test
    public void testGetResourceMimeType() {
        final String type = resourceService.getResourceMimeType(new BufferedInputStream(this.getClass().getClassLoader()
                .getResourceAsStream("tika-test-file.xml")));
        assertEquals("application/xml", type);
    }

    @Test(expected = ResourceServiceException.class)
    public void testGetResourceMimeTypeErr() {
        final String type = resourceService.getResourceMimeType(new BufferedInputStream(new IoExIns()));
        assertEquals("application/xml", type);
    }

    @Test
    public void testGenerateAndDeployFile() throws IOException {
        final ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        final ResourceIdentifier resourceIdentifier = builder.setGroupName("group1").setResourceName("server.xml")
                .setJvmName("jvm1").build();
        final ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(Config.mockResourceHandler.fetchResource(resourceIdentifier)).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn(IOUtils.toString(this.getClass().getClassLoader()
                .getResourceAsStream("sample-metadata.json"), StandardCharsets.UTF_8));
        when(mockConfigTemplate.getTemplateContent()).thenReturn("<server/>");
        when(Config.mockResourceHandler.getSelectedValue(resourceIdentifier)).thenReturn(mock(Jvm.class));
        final List<Group> groupList = new ArrayList<>();
        groupList.add(mock(Group.class));
        when(Config.mockGroupPesistenceService.getGroups()).thenReturn(groupList);
        when(Config.mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server/>");

        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Created directory", ""));
        when(Config.mockDistributionService.remoteFileCheck(anyString(), anyString())).thenReturn(true);
        when(Config.mockBinaryDistributionControlService.backupFileWithMove(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Backup succeeded", ""));
        final CommandOutput scpResult = new CommandOutput(new ExecReturnCode(0), "SCP succeeded", "");
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(scpResult);

        CommandOutput result = resourceService.generateAndDeployFile(resourceIdentifier, "jvm1", "server.xml", "localhost");
        assertEquals(scpResult, result);
    }

    @Test (expected = ResourceServiceException.class)
    public void testGenerateAndDeployFileFailsCreateDir() throws IOException {
        final ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        final ResourceIdentifier resourceIdentifier = builder.setGroupName("group1").setResourceName("server.xml")
                .setJvmName("jvm1").build();
        final ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(Config.mockResourceHandler.fetchResource(resourceIdentifier)).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn(IOUtils.toString(this.getClass().getClassLoader()
                .getResourceAsStream("sample-metadata.json"), StandardCharsets.UTF_8));
        when(mockConfigTemplate.getTemplateContent()).thenReturn("<server/>");
        when(Config.mockResourceHandler.getSelectedValue(resourceIdentifier)).thenReturn(mock(Jvm.class));
        final List<Group> groupList = new ArrayList<>();
        groupList.add(mock(Group.class));
        when(Config.mockGroupPesistenceService.getGroups()).thenReturn(groupList);
        when(Config.mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server/>");

        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "FAILED TO  CREATE THE DIRECTORY"));

        resourceService.generateAndDeployFile(resourceIdentifier, "jvm1", "server.xml", "localhost");
    }

    @Test
    public void testGenerateAndDeployFileOverwriteFalse() throws IOException {
        final ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        final ResourceIdentifier resourceIdentifier = builder.setGroupName("group1").setResourceName("server.xml")
                .setJvmName("jvm1").build();
        final ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(Config.mockResourceHandler.fetchResource(resourceIdentifier)).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn(IOUtils.toString(this.getClass().getClassLoader()
                .getResourceAsStream("sample-metadata-overwrite-false.json"), StandardCharsets.UTF_8));
        when(mockConfigTemplate.getTemplateContent()).thenReturn("<server/>");
        when(Config.mockResourceHandler.getSelectedValue(resourceIdentifier)).thenReturn(mock(Jvm.class));
        final List<Group> groupList = new ArrayList<>();
        groupList.add(mock(Group.class));
        when(Config.mockGroupPesistenceService.getGroups()).thenReturn(groupList);
        when(Config.mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server/>");

        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Created directory", ""));
        when(Config.mockDistributionService.remoteFileCheck(anyString(), anyString())).thenReturn(true);

        CommandOutput result = resourceService.generateAndDeployFile(resourceIdentifier, "jvm1", "server.xml", "localhost");
        CommandOutput skipOverwriteResult = new CommandOutput(new ExecReturnCode(0), "Skipping scp of file: c://server.xml already exists and overwrite is set to false.", "");
        assertEquals(skipOverwriteResult, result);
    }

    @Test (expected = ResourceServiceException.class)
    public void testGenerateAndDeployFileFailsBackup() throws IOException {
        final ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        final ResourceIdentifier resourceIdentifier = builder.setGroupName("group1").setResourceName("server.xml")
                .setJvmName("jvm1").build();
        final ConfigTemplate mockConfigTemplate = mock(ConfigTemplate.class);
        when(Config.mockResourceHandler.fetchResource(resourceIdentifier)).thenReturn(mockConfigTemplate);
        when(mockConfigTemplate.getMetaData()).thenReturn(IOUtils.toString(this.getClass().getClassLoader()
                .getResourceAsStream("sample-metadata.json"), StandardCharsets.UTF_8));
        when(mockConfigTemplate.getTemplateContent()).thenReturn("<server/>");
        when(Config.mockResourceHandler.getSelectedValue(resourceIdentifier)).thenReturn(mock(Jvm.class));
        final List<Group> groupList = new ArrayList<>();
        groupList.add(mock(Group.class));
        when(Config.mockGroupPesistenceService.getGroups()).thenReturn(groupList);
        when(Config.mockJvmPersistenceService.getJvmTemplate(anyString(), any(Identifier.class))).thenReturn("<server/>");

        when(Config.mockBinaryDistributionControlService.createDirectory(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Created directory", ""));
        when(Config.mockDistributionService.remoteFileCheck(anyString(), anyString())).thenReturn(true);
        when(Config.mockBinaryDistributionControlService.backupFileWithMove(anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "BACK UP FAILED"));
        final CommandOutput scpResult = new CommandOutput(new ExecReturnCode(0), "SCP succeeded", "");
        when(Config.mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(scpResult);

        resourceService.generateAndDeployFile(resourceIdentifier, "jvm1", "server.xml", "localhost");
    }

    static class IoExIns extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("IO exception!");
        }

    }

    @Configuration
    static class Config {
        private static final DistributionService mockDistributionService = mock(DistributionService.class);
        private static final BinaryDistributionControlService mockBinaryDistributionControlService = mock(BinaryDistributionControlService.class);
        private static final BinaryDistributionLockManager mockBinaryDistributionLockManager= mock(BinaryDistributionLockManager.class);
        private static final ResourcePersistenceService mockResourcePersistenceService = mock(ResourcePersistenceService.class);
        private static final GroupPersistenceService mockGroupPesistenceService = mock(GroupPersistenceService.class);
        private static final ApplicationPersistenceService mockAppPersistenceService = mock(ApplicationPersistenceService.class);
        private static final JvmPersistenceService mockJvmPersistenceService = mock(JvmPersistenceService.class);
        private static final WebServerPersistenceService mockWebServerPersistenceService = mock(WebServerPersistenceService.class);
        private static final ResourceDao mockResourceDao = mock(ResourceDao.class);
        private static final ResourceHandler mockResourceHandler = mock(ResourceHandler.class);
        private static final RepositoryService mockRepositoryService = mock(RepositoryService.class);
        private static final HistoryFacadeService mockHistoryFacadeService = mock(HistoryFacadeService.class);

        @Bean
        public DistributionService getMockDistributionService() {
            return mockDistributionService;
        }

        @Bean
        public BinaryDistributionControlService getMockBinaryDistributionControlService() {
            return mockBinaryDistributionControlService;
        }

        @Bean
        public ResourceService getResourceService() {
            ResourceContentGeneratorService resourceContentGeneratorService = new ResourceContentGeneratorServiceImpl(mockGroupPesistenceService,
                    mockWebServerPersistenceService, mockJvmPersistenceService, mockAppPersistenceService, mockHistoryFacadeService);
            Tika tika = new Tika();
            BinaryDistributionService mockBinaryDistributionService = mock(BinaryDistributionService.class);

            return new ResourceServiceImpl(mockResourcePersistenceService, mockGroupPesistenceService,
                    mockAppPersistenceService, mockJvmPersistenceService, mockWebServerPersistenceService,
                    mockResourceDao, mockResourceHandler,
                    resourceContentGeneratorService, mockBinaryDistributionService, tika, mockRepositoryService);
        }

        @Bean
        public HistoryFacadeService getMockHistoryFacadeService(){
            return mockHistoryFacadeService;
        }

        @Bean
        public BinaryDistributionLockManager getMockBinaryDistributionLockManager(){
            return mockBinaryDistributionLockManager;
        }
    }
}
