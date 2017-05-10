package com.cerner.jwala.persistence.service.webserver;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.*;
import static junit.framework.TestCase.assertNotNull;

@Transactional
public abstract class AbstractWebServerPersistenceServiceTest {

    @Autowired
    private WebServerPersistenceService webServerPersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Test
    public void testCreateWebServer() {
        Set<Group> groupCollection = new HashSet<>();
        CreateGroupRequest createGroupRequest = new CreateGroupRequest("test-group");
        Group groupResult = groupPersistenceService.createGroup(createGroupRequest);
        groupCollection.add(groupResult);

        WebServer webServer = new WebServer(null, groupCollection, "test-ws", "test-host", 80, 443, new Path("./apache_pb.png"),
                WebServerReachableState.WS_NEW, null);
        WebServer result = webServerPersistenceService.createWebServer(webServer, "test-user");

        assertNotNull(result);
        assertEquals(1, result.getGroups().size());

        WebServer getItAgain = webServerPersistenceService.getWebServer(result.getId());
        assertEquals(result, getItAgain);
    }

    @Test
    public void testUpdateWebServer() {
        testCreateWebServer();
        WebServer oldWebServer = webServerPersistenceService.findWebServerByName("test-ws");
        WebServer updatedWebServer = new WebServer(oldWebServer.getId(), oldWebServer.getGroups(), "test-ws-updated",
                oldWebServer.getHost(), oldWebServer.getPort(), oldWebServer.getHttpsPort(), oldWebServer.getStatusPath(),
                oldWebServer.getState(), oldWebServer.getApacheHttpdMedia());
        WebServer result = webServerPersistenceService.updateWebServer(updatedWebServer, "test-user");
        assertEquals(updatedWebServer, result);
        assertEquals(1, webServerPersistenceService.getWebServers().size());

        Collection<Identifier<Group>> groupIds = result.getGroupIds();
        assertEquals(1, groupIds.size());
        List<WebServer> webServersByGroup = webServerPersistenceService.findWebServersBelongingTo(groupIds.iterator().next());
        assertEquals(1, webServersByGroup.size());
        assertEquals(result, webServersByGroup.iterator().next());

        webServerPersistenceService.removeWebServer(result.getId());
        assertEquals(0, webServerPersistenceService.getWebServers().size());
    }

    @Test
    public void testFindApplications() {
        testCreateWebServer();
        List<Application> result = webServerPersistenceService.findApplications("test-ws");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindJvms() {
        testCreateWebServer();
        List<Jvm> resultJvms = webServerPersistenceService.findJvms("test-ws");
        assertEquals(0, resultJvms.size());
    }

    @Test
    public void testUploadHttpdConfTemplate() {
        testCreateWebServer();
        WebServer webServer = webServerPersistenceService.findWebServerByName("test-ws");
        final String metaData = "{\"deployPath\":\"./absoluteDeployPath\", \"deployFileName\":\"httpd.conf\"}";
        final String fileName = "httpd.conf";
        final String templateContent = "httpd.conf template";
        UploadWebServerTemplateRequest uploadTemplateRequest = new UploadWebServerTemplateRequest(
                webServer,
                fileName,
                metaData,
                templateContent) {

            @Override
            public String getConfFileName() {
                return fileName;
            }

        };
        JpaWebServerConfigTemplate result = webServerPersistenceService.uploadWebServerConfigTemplate(uploadTemplateRequest, "./absoluteDeployPath/httpd.conf", "test-user");
        assertEquals(metaData, result.getMetaData());
        assertEquals(templateContent, result.getTemplateContent());
        assertEquals(fileName, result.getTemplateName());

        List<String> resultTemplates = webServerPersistenceService.getResourceTemplateNames("test-ws");
        assertEquals(1, resultTemplates.size());
        assertEquals(fileName, resultTemplates.get(0));

        String resultContent = webServerPersistenceService.getResourceTemplate("test-ws", fileName);
        assertEquals(templateContent, resultContent);

        final String updatedTemplateContent = "new httpd.conf template content";
        webServerPersistenceService.updateResourceTemplate("test-ws", fileName, updatedTemplateContent);
        resultContent = webServerPersistenceService.getResourceTemplate("test-ws", fileName);
        assertEquals(updatedTemplateContent, resultContent);

        String resultMetaData = webServerPersistenceService.getResourceTemplateMetaData("test-ws", "httpd.conf");
        assertEquals(metaData, resultMetaData);
    }

    @Test
    public void testUpdateState() {
        testCreateWebServer();
        WebServer webServer = webServerPersistenceService.findWebServerByName("test-ws");
        int result = webServerPersistenceService.updateState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE);
        assertEquals(1, result);
    }

    @Test
    public void testUpdateStateWithErrorStatus() {
        testCreateWebServer();
        WebServer webServer = webServerPersistenceService.findWebServerByName("test-ws");
        final String testErrorStatus = "TEST ERROR";
        int result = webServerPersistenceService.updateState(webServer.getId(), WebServerReachableState.WS_UNREACHABLE, testErrorStatus);
        assertEquals(1, result);
    }

    @Test
    public void testGetWebServerStartedCount() {
        testCreateWebServer();
        Long result = webServerPersistenceService.getWebServerStartedCount("test-group");
        assertEquals(0L, result.longValue());
    }

    @Test
    public void testGetWebServerCount() {
        testCreateWebServer();
        Long result = webServerPersistenceService.getWebServerCount("test-group");
        assertEquals(1L, result.longValue());
    }

    @Test
    public void testGetWebServersStoppedCount() {
        testCreateWebServer();
        Long result = webServerPersistenceService.getWebServerStoppedCount("test-group");
        assertEquals(0L, result.longValue());
    }

    @Test
    public void testGetWebServersByGroupName() {
        testCreateWebServer();
        WebServer webServer = webServerPersistenceService.findWebServerByName("test-ws");
        List<WebServer> result = webServerPersistenceService.getWebServersByGroupName("test-group");
        assertEquals(webServer.getId(), result.get(0).getId());
    }

    @Test
    public void testCheckResourceName() {
        testCreateWebServer();
        WebServer webServer = webServerPersistenceService.findWebServerByName("test-ws");
        boolean result = webServerPersistenceService.checkWebServerResourceFileName("test-group", "test-ws", "httpd.conf");
        assertFalse(result);

        final UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(
                webServer,
                "httpd.conf",
                "{}",
                "meh") {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };

        webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebServerTemplateRequest, "./httpd.conf", "test-user");
        result = webServerPersistenceService.checkWebServerResourceFileName("test-group", "test-ws", "httpd.conf");
        assertTrue(result);
    }
}
