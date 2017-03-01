package com.cerner.jwala.ws.rest.v1.service.admin.impl;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.files.FilesConfiguration;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Jeffery Mahmood on 10/26/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminServiceRestImplTest {
    String authFlag;

    @InjectMocks
    private AdminServiceRestImpl cut;

    @Mock
    private FilesConfiguration theFilesConfiguration = mock(FilesConfiguration.class);

    @Mock
    private ResourceService theResourceService = mock(ResourceService.class);

    @Mock
    PropertySourcesPlaceholderConfigurer thePropConfigurer = mock(PropertySourcesPlaceholderConfigurer.class);

    public AdminServiceRestImplTest() {
        cut = new AdminServiceRestImpl(theFilesConfiguration, theResourceService);
    }

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        authFlag = ApplicationProperties.get("jwala.authorization");
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testReload() {
        Response response = cut.reload();
        assertNotNull(response.getEntity());

        System.setProperty("log4j.configuration", "testlog4j.xml");
        response = cut.reload();
        assertNotNull(response.getEntity());

        System.setProperty("log4j.configuration", "testlog4j.properties");
        response = cut.reload();
        assertNotNull(response.getEntity());

        System.setProperty("log4j.configuration", "testlog4j_NO_SUCH_FILE.xml");
        response = cut.reload();
        assertNotNull(response.getEntity());

        System.clearProperty("log4j.configuration");
    }


    @Test
    public void testEncrypt() {
        Response response = cut.encrypt("");
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        response = cut.encrypt("encrypt me");
        assertNotNull(response.getEntity());
    }

    @Test
    public void testManifest() throws IOException {
        final ServletContext contextMock = mock(ServletContext.class);
        when(contextMock.getResourceAsStream(anyString())).thenReturn(new FileInputStream(new File("./src/test/resources/META-INF/MANIFEST.MF")));
        Response response = cut.manifest(contextMock);
        assertNotNull(response.getEntity());

        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Test bad manifest file"));
        when(contextMock.getResourceAsStream(anyString())).thenReturn(mockInputStream);
        boolean exceptionThrown = false;
        try {
            cut.manifest(contextMock);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testView() {
        Response response = cut.view();
        assertNotNull(response.getEntity());
    }

    @Test
    public void testIsJwalaAuthorizationEnabled() throws Exception {
        Response response = cut.isJwalaAuthorizationEnabled();
        ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        Object content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, AdminServiceRestImpl.JSON_RESPONSE_TRUE);
        System.setProperty("jwala.authorization", "false");
    }

    @Test
    public void testGetAuthorizationDetails() {
        Response response = cut.getAuthorizationDetails();
        assertEquals(response.getStatus(), 200);
    }
}
