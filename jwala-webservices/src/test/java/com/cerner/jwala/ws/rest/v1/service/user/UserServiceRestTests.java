package com.cerner.jwala.ws.rest.v1.service.user;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.service.user.impl.UserServiceRestImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceRestTests {

    String authFlag;
    public UserServiceRestImpl impl;
    String JWALA_ROLE_ADMIN;

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        authFlag = ApplicationProperties.get("jwala.authorization");
        impl = new UserServiceRestImpl();
        JWALA_ROLE_ADMIN = ApplicationProperties.get("jwala.role.admin");

    }

    @After
    public void tearDown() throws IOException {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testIsUserAdmin() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Response response = impl.isUserAdmin(mockRequest, mockResponse);
        assertNotNull(response.getEntity());
        ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        Object content = applicationResponse.getApplicationResponseContent();
        assertEquals(content, UserServiceRestImpl.JSON_RESPONSE_FALSE);
    }

    @Test
    public void testLogout() {
        HttpSession httpSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockRequest.getSession()).thenReturn(httpSession);
        Response response = impl.logout(mockRequest, mockResponse);
        assertEquals(response.getStatus(), 200);
    }


}
