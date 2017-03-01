package com.cerner.jwala.ws.rest.v1.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cerner.jwala.common.domain.model.id.Identifier.id;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceRestImplTest {

    @Mock
    private MessageContext mockMc;
    @Mock
    private HttpServletRequest mockHsr;
    @Mock
    private HttpHeaders mockHh;

    @Mock
    Identifier<Application> anAppToGet;

    @Mock
    private Application mockApplication;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private Group mockGroup;

    @Mock
    private Jvm mockJvm;

    /*NoMock*/ private ApplicationService service;
    @Mock
    private AuthenticatedUser authenticatedUser;
    @InjectMocks
    @Spy
    private ApplicationServiceRestImpl applicationServiceRest = new ApplicationServiceRestImpl(service = Mockito.mock(ApplicationService.class),
            mock(ResourceService.class), mockGroupService);

    private ApplicationServiceRest cut;

    Group group1 = new Group(Identifier.id(0L, Group.class), "");
    Application application = new Application(Identifier.id(1L, Application.class), "", "", "", group1, true, true, false, "testWar.war");
    Application applicationWithWar = new Application(Identifier.id(1L, Application.class), "", "D:\\APACHE\\TOMCAT\\WEBAPPS\\jwala-webapp-1.0-SNAPSHOT-b6349ade-d8f2-4a2f-bdc5-d92d644a1a67-.war", "", group1, true, true, false, "testWar.war");
    Application newlyCreatedApp = new Application(Identifier.id(2L, Application.class), "", "", "", group1, true, true, false, "testWar.war");

    List<Application> applications = new ArrayList<>(1);
    List<Application> applications2 = new ArrayList<>(2);
    List<Application> emptyList = new ArrayList<>(0);

    @Before
    public void setUp() {
        cut = applicationServiceRest;
        applications.add(application);

        applications2.add(application);
        applications2.add(newlyCreatedApp);

        List<MediaType> mtOk = new ArrayList<>();
        mtOk.add(MediaType.APPLICATION_JSON_TYPE);
        when(mockHh.getAcceptableMediaTypes()).thenReturn(mtOk);
        when(mockMc.getHttpHeaders()).thenReturn(mockHh);
        when(mockMc.getHttpServletRequest()).thenReturn(mockHsr);
        when(authenticatedUser.getUser()).thenReturn(new User("unusedUser"));
    }

    @Test
    public void testJsonSettersGetters() {
        JsonUpdateApplication testJua = new JsonUpdateApplication(2L, "name", "/ctx", 1L, true, true, false);
        JsonCreateApplication testJca = new JsonCreateApplication(2L, "name", "/ctx", true, true, false);
        assertEquals(testJca, testJca.clone());
        assertEquals(testJua, testJua.clone());
        assertEquals(testJca.hashCode(), testJca.clone().hashCode());
        assertEquals(testJua.hashCode(), testJua.clone().hashCode());
    }

    private class MyIS extends ServletInputStream {

        private InputStream backingStream;

        public MyIS(InputStream backingStream) {
            this.backingStream = backingStream;
        }

        @Override
        public int read() throws IOException {
            return backingStream.read();
        }

    }

    @Test
    public void testGetApplications() {
        when(service.getApplications()).thenReturn(applications);

        Response resp = cut.getApplications(null);
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(applications, result);
    }

    @SuppressWarnings("unchecked")
    private List<Application> getApplicationsFromResponse(Response resp) {
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertTrue(entity instanceof List<?>);

        return (List<Application>) entity;
    }

    private Application getApplicationFromResponse(Response resp) {
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertNotNull(entity);
        assertTrue(entity instanceof Application);

        return (Application) entity;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetApplicationById() {
        when(service.getApplication(any(Identifier.class))).thenReturn(application);
        Response resp = cut.getApplication(id(1L, Application.class));

        Application result = getApplicationFromResponse(resp);

        assertEquals(application, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByGroupIdNone() {
        when(service.findApplications(any(Identifier.class))).thenReturn(emptyList);
        Response resp = cut.getApplications(id(2L, Group.class));

        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(emptyList, result);
    }

    @Test
    public void testFindApplicationsByJvmId() {
        when(service.findApplicationsByJvmId(Matchers.eq(id(2L, Jvm.class)))).thenReturn(applications);
        Response resp = cut.findApplicationsByJvmId(id(2L, Jvm.class));
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(applications, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByNullJvmId() {
        when(service.findApplicationsByJvmId(any(Identifier.class))).thenReturn(emptyList);
        Response resp = cut.findApplicationsByJvmId(null);
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(emptyList, result);
    }

    /**
     * Testing: {@link com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest#createApplication(JsonCreateApplication, AuthenticatedUser)}
     */
    @Test
    public void testCreate() {
        when(service.createApplication(any(CreateApplicationRequest.class), any(User.class))).thenReturn(newlyCreatedApp);

        JsonCreateApplication jsonCreateAppRequest = new JsonCreateApplication();

        Response resp = cut.createApplication(jsonCreateAppRequest, authenticatedUser);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(this.newlyCreatedApp, entity);
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    }

    /**
     * Testing: {@link com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest#updateApplication(JsonUpdateApplication, AuthenticatedUser)}
     */
    @Test
    public void testUpdate() {
        when(service.updateApplication(any(UpdateApplicationRequest.class), any(User.class))).thenReturn(newlyCreatedApp);
        ArrayList<UpdateApplicationRequest> multiUpdate = new ArrayList<>();
        multiUpdate.add(new UpdateApplicationRequest(Identifier.id(0L, Application.class), Identifier.id(0L, Group.class), "", "", true, true, false));
        JsonUpdateApplication jsonUpdateAppRequest = new JsonUpdateApplication();
        Response resp = cut.updateApplication(jsonUpdateAppRequest, authenticatedUser);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(this.newlyCreatedApp, entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemove() {
        Response resp = cut.removeApplication(application.getId(), authenticatedUser);
        Mockito.verify(service, Mockito.times(1)).removeApplication(any(Identifier.class), any(User.class));
        assertNull(resp.getEntity());
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testGetResourceNames() {
        when(service.getResourceTemplateNames(anyString(), anyString())).thenReturn(new ArrayList());
        Response response = cut.getResourceNames(application.getName(), "any");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        final String updateContent = "<server>updatedContent</server>";
        when(service.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(updateContent);
        Response response = cut.updateResourceTemplate(application.getName(), "ServerXMLTemplate.tpl", updateContent, "jvmName", "groupName");
        assertNotNull(response.getEntity());

        when(service.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("jvmName", "server"));
        response = cut.updateResourceTemplate(application.getName(), "ServerXMLTemplate.tpl", updateContent, "jvmName", "groupName");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGetApplicationByName() {
        when(service.getApplication(anyString())).thenReturn(mockApplication);
        Response response = cut.getApplicationByName("application");
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeployConfw() {
        Response response = cut.deployConf("appName", authenticatedUser, "hostName");
        assertEquals(response.getStatus(), 200);
    }
    @Ignore
    @Test
    public void testCheckIfFileExists() {
        Response response = cut.checkIfFileExists("filePath", authenticatedUser, "hostName");
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeployWebArchive() {
        Set<Jvm> set = new HashSet<>();
        set.add(mockJvm);
        when(service.getApplication(anAppToGet)).thenReturn(mockApplication);
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getJvms()).thenReturn(set);
        Response response = cut.deployWebArchive(anAppToGet, authenticatedUser);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeployConf() {
        CommandOutput mockExecData = mock(CommandOutput.class);
        when(service.deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class))).thenReturn(mockExecData);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        Response response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        response = cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        assertNotNull(response.getEntity());

        when(service.deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class))).thenThrow(new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Target JVM must be stopped"));
        boolean exceptionThrown = false;
        try {
            cut.deployConf(application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl", authenticatedUser);
        } catch (InternalErrorException ie) {
            exceptionThrown = true;
            assertEquals("Target JVM must be stopped", ie.getMessage());
        }
        assertTrue(exceptionThrown);

    }

    @Test
    public void testPreviewResourceTemplate() {
        when(service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenReturn("preview content");
        Response response = cut.previewResourceTemplate("myFile", application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());

        when(service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenThrow(new RuntimeException("Test fail preview"));
        response = cut.previewResourceTemplate("myFile", application.getName(), group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());
    }

    /**
     * Instead of mocking the ServletInputStream, let's extend it instead.
     *
     * @see "http://stackoverflow.com/questions/20995874/how-to-mock-a-javax-servlet-servletinputstream"
     */
    static class DelegatingServletInputStream extends ServletInputStream {

        private InputStream inputStream;

        public DelegatingServletInputStream() {
            inputStream = new ByteArrayInputStream("------WebKitFormBoundaryXRxegBGqTe4gApI2\r\nContent-Disposition: form-data; name=\"hct.properties\"; filename=\"hotel-booking.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\n------WebKitFormBoundaryXRxegBGqTe4gApI2--".getBytes(Charset.defaultCharset()));
        }

        /**
         * Return the underlying source stream (never <code>null</code>).
         */
        public final InputStream getSourceStream() {
            return inputStream;
        }


        public int read() throws IOException {
            return inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            inputStream.close();
        }

    }
}
