package com.cerner.jwala.ws.rest.v1.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.History;
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
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.Message;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ApplicationServiceRestImplTest.Config.class})
public class ApplicationServiceRestImplTest {
    @Autowired
    private ApplicationServiceRest applicationServiceRestInterface;

    Group group1 = new Group(Identifier.id(0L, Group.class), "");
    Application application = new Application(Identifier.id(1L, Application.class), "", "", "", group1, true, true, false, "testWar.war");
    Application applicationWithWar = new Application(Identifier.id(1L, Application.class), "", "D:\\APACHE\\TOMCAT\\WEBAPPS\\jwala-webapp-1.0-SNAPSHOT-b6349ade-d8f2-4a2f-bdc5-d92d644a1a67-.war", "", group1, true, true, false, "testWar.war");
    Application newlyCreatedApp = new Application(Identifier.id(2L, Application.class), "", "", "", group1, true, true, false, "testWar.war");

    List<Application> applications = new ArrayList<>(1);
    List<Application> applications2 = new ArrayList<>(2);
    List<Application> emptyList = new ArrayList<>(0);

    @Before
    public void setUp() {
        applications.add(application);

        applications2.add(application);
        applications2.add(newlyCreatedApp);

        List<MediaType> mtOk = new ArrayList<>();
        mtOk.add(MediaType.APPLICATION_JSON_TYPE);
        when(Config.mockHttpHeaders.getAcceptableMediaTypes()).thenReturn(mtOk);
        when(Config.mockMessageContext.getHttpHeaders()).thenReturn(Config.mockHttpHeaders);
        when(Config.mockMessageContext.getHttpServletRequest()).thenReturn(Config.mockHttpServletRequest);
        when(Config.authenticatedUser.getUser()).thenReturn(new User("unusedUser"));
        reset(Config.service);

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
        when(Config.service.getApplications()).thenReturn(applications);

        Response resp = applicationServiceRestInterface.getApplications(null);
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
        when(Config.service.getApplication(any(Identifier.class))).thenReturn(application);
        Response resp = applicationServiceRestInterface.getApplication(id(1L, Application.class));

        Application result = getApplicationFromResponse(resp);

        assertEquals(application, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByGroupIdNone() {
        when(Config.service.findApplications(any(Identifier.class))).thenReturn(emptyList);
        Response resp = applicationServiceRestInterface.getApplications(id(2L, Group.class));

        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(emptyList, result);
    }

    @Test
    public void testFindApplicationsByJvmId() {
        when(Config.service.findApplicationsByJvmId(Matchers.eq(id(2L, Jvm.class)))).thenReturn(applications);
        Response resp = applicationServiceRestInterface.findApplicationsByJvmId(id(2L, Jvm.class));
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(applications, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFindApplicationsByNullJvmId() {
        when(Config.service.findApplicationsByJvmId(any(Identifier.class))).thenReturn(emptyList);
        Response resp = applicationServiceRestInterface.findApplicationsByJvmId(null);
        List<Application> result = getApplicationsFromResponse(resp);

        assertEquals(emptyList, result);
    }

    /**
     * Testing: {@link com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest#createApplication(JsonCreateApplication, AuthenticatedUser)}
     */
    @Test
    public void testCreate() {
        when(Config.service.createApplication(any(CreateApplicationRequest.class), any(User.class))).thenReturn(Config.newlyCreatedApp);

        JsonCreateApplication jsonCreateAppRequest = new JsonCreateApplication();

        Response resp = applicationServiceRestInterface.createApplication(jsonCreateAppRequest, Config.authenticatedUser);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(Config.newlyCreatedApp, entity);
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    }

    /**
     * Testing: {@link com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest#updateApplication(JsonUpdateApplication, AuthenticatedUser)}
     */
    @Test
    public void testUpdate() throws ApplicationServiceException {
        when(Config.service.updateApplication(any(UpdateApplicationRequest.class), any(User.class))).thenReturn(Config.newlyCreatedApp);
        ArrayList<UpdateApplicationRequest> multiUpdate = new ArrayList<>();
        multiUpdate.add(new UpdateApplicationRequest(Identifier.id(0L, Application.class), Identifier.id(0L, Group.class), "", "", true, true, false));
        JsonUpdateApplication jsonUpdateAppRequest = new JsonUpdateApplication();
        Response resp = applicationServiceRestInterface.updateApplication(jsonUpdateAppRequest, Config.authenticatedUser);
        assertNotNull(resp.getEntity());
        ApplicationResponse appResponse = (ApplicationResponse) resp.getEntity();
        Object entity = appResponse.getApplicationResponseContent();
        assertEquals(Config.newlyCreatedApp, entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemove() {
        Response resp = applicationServiceRestInterface.removeApplication(Config.application.getId(), Config.authenticatedUser);
        Mockito.verify(Config.service, Mockito.times(1)).removeApplication(any(Identifier.class), any(User.class));
        assertNull(resp.getEntity());
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testGetResourceNames() {
        when(Config.service.getResourceTemplateNames(anyString(), anyString())).thenReturn(new ArrayList());
        Response response = applicationServiceRestInterface.getResourceNames(application.getName(), "any");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testUpdateResourceTemplate() {
        final String updateContent = "<server>updatedContent</server>";
        when(Config.service.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(updateContent);
        Response response = applicationServiceRestInterface.updateResourceTemplate(Config.application.getName(), "ServerXMLTemplate.tpl", updateContent, "jvmName", "groupName");
        assertNotNull(response.getEntity());

        when(Config.service.updateResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new ResourceTemplateUpdateException("jvmName", "server"));
        response = applicationServiceRestInterface.updateResourceTemplate(Config.application.getName(), "ServerXMLTemplate.tpl", updateContent, "jvmName", "groupName");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testGetApplicationByName() {
        when(Config.service.getApplication(anyString())).thenReturn(Config.mockApplication);
        Response response = applicationServiceRestInterface.getApplicationByName("application");
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeployConfw() {
        Response response = applicationServiceRestInterface.deployConf("appName", Config.authenticatedUser, "hostName");
        assertEquals(response.getStatus(), 200);
    }
    @Ignore
    @Test
    public void testCheckIfFileExists() {
        Response response = applicationServiceRestInterface.checkIfFileExists("filePath", Config.authenticatedUser, "hostName");
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeployWebArchive() {
        Set<Jvm> set = new HashSet<>();
        set.add(Config.mockJvm);
        when(Config.service.getApplication(Config.anAppToGet)).thenReturn(Config.mockApplication);
        when(Config.mockApplication.getGroup()).thenReturn( Config.mockGroup);
        when(Config.mockGroup.getJvms()).thenReturn(set);
        Response response = applicationServiceRestInterface.deployWebArchive(Config.anAppToGet, Config.authenticatedUser);
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testDeployConf() {
        CommandOutput mockExecData = mock(CommandOutput.class);
        when(Config.service.deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class))).thenReturn(mockExecData);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));

        Response response = applicationServiceRestInterface.deployConf(Config.application.getName(), Config.group1.getName(), "jvmName", "ServerXMLTemplate.tpl", Config.authenticatedUser);
        assertNotNull(response.getEntity());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        response = applicationServiceRestInterface.deployConf(Config.application.getName(), Config.group1.getName(), "jvmName", "ServerXMLTemplate.tpl", Config.authenticatedUser);
        assertNotNull(response.getEntity());

        when(Config.service.deployConf(anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class), any(User.class))).thenThrow(new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, "Target JVM must be stopped"));
        boolean exceptionThrown = false;
        try {
            applicationServiceRestInterface.deployConf(Config.application.getName(), Config.group1.getName(), "jvmName", "ServerXMLTemplate.tpl", Config.authenticatedUser);
        } catch (InternalErrorException ie) {
            exceptionThrown = true;
            assertEquals("Target JVM must be stopped", ie.getMessage());
        }
        assertTrue(exceptionThrown);

    }

    @Test
    public void testPreviewResourceTemplate() {
        when(Config.service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenReturn("preview content");
        Response response = applicationServiceRestInterface.previewResourceTemplate("myFile", Config.application.getName(), Config.group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());

        when(Config.service.previewResourceTemplate(anyString(), anyString(), anyString(), anyString(), anyString(), any(ResourceGroup.class))).thenThrow(new RuntimeException("Test fail preview"));
        response = applicationServiceRestInterface.previewResourceTemplate("myFile", Config.application.getName(), Config.group1.getName(), "jvmName", "ServerXMLTemplate.tpl");
        assertNotNull(response.getEntity());
    }

    @Configuration
    static class Config{
     //   private static HistoryFacadeService mockHistoryFacadeService = mock(HistoryFacadeService.class);
        private static HttpHeaders mockHttpHeaders = mock(HttpHeaders.class);
        private static MessageContext mockMessageContext = mock(MessageContext.class);
        private static HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        private static Identifier<Application> anAppToGet = mock(Identifier.class);
        private static Application mockApplication = mock(Application.class);
        private static GroupService mockGroupService = mock(GroupService.class);
        private static Group mockGroup = mock(Group.class);
        private static Jvm mockJvm = mock(Jvm.class);
        private static HistoryFacadeService historyFacadeService = mock(HistoryFacadeService.class);
        private static AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
        static Group group1 = new Group(Identifier.id(0L, Group.class), "");
        static Application application = new Application(Identifier.id(1L, Application.class), "", "", "", group1, true, true, false, "testWar.war");
        static Application applicationWithWar = new Application(Identifier.id(1L, Application.class), "", "D:\\APACHE\\TOMCAT\\WEBAPPS\\jwala-webapp-1.0-SNAPSHOT-b6349ade-d8f2-4a2f-bdc5-d92d644a1a67-.war", "", group1, true, true, false, "testWar.war");
        static Application newlyCreatedApp = new Application(Identifier.id(2L, Application.class), "", "", "", group1, true, true, false, "testWar.war");
        static ApplicationService service = mock(ApplicationService.class);
        static ResourceService resourceService = mock(ResourceService.class);
        static BinaryDistributionControlService binaryDistributionControlService=mock(BinaryDistributionControlService.class);

        @Bean
        public static BinaryDistributionControlService getBinaryDistributionControlService(){
            return binaryDistributionControlService;
        }


        @Bean
        public static ResourceService getResourceService() {
            return resourceService;
        }

        @Bean
        public static AuthenticatedUser getAuthenticatedUser() {
            return authenticatedUser;
        }

        @Bean
        public static ApplicationService getService() {
            return service;
        }




        @Bean
        public ApplicationServiceRest getApplicationServiceRest(ApplicationService applicationService, ResourceService resourceService, GroupService groupService){
            return new ApplicationServiceRestImpl(applicationService, resourceService, groupService);}

        @Bean
        public HistoryFacadeService getHistoryFacadeService(){
            return historyFacadeService;
        }

        @Bean
        public HttpHeaders getMockHttpHeaders(){
            return mockHttpHeaders;
        }

        @Bean
        public MessageContext getMockMessageContext(){
            return mockMessageContext;
        }

        @Bean
        public Group getMockGroup(){
            return mockGroup;
        }

        @Bean
        public Jvm getMockJvm(){
            return mockJvm;
        }

        @Bean
        public AuthenticatedUser getMockAuthenticatedUser(){
            return authenticatedUser;
        }

        @Bean
        public HttpServletRequest getMockHttpServletRequest(){
            return mockHttpServletRequest;
        }

       @Bean
        public Identifier<Application> getAnAppToGet(){
            return anAppToGet;
       }

       @Bean
        public Application getMockApplication(){
            return mockApplication;
       }

       @Bean
        public GroupService getMockGroupService(){
            return mockGroupService;
       }

       @Bean
        public Group getGroup1(){return group1;}

        @Bean
        public Application getApplication(){return application;}

        @Bean
        public Application getApplicationWithWar(){return  applicationWithWar;}

        @Bean
        public Application getNewlyCreatedApp(){return newlyCreatedApp;}



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
