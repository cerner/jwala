package com.cerner.jwala.web.controller;

import junit.framework.TestCase;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IndexControllerTest extends TestCase {
    final IndexController ic = new IndexController();

    public void testIndex() {
        assertEquals("jwala/index", ic.index());
    }

    public void testAbout() {
        assertEquals("jwala/about", ic.about());
    }

    public void testIndexPageScripts() {
        String result = ic.indexPageScripts("true", false);
        assertEquals("jwala/dev-index-page-scripts", result);
        result = ic.indexPageScripts("true", true);
        assertEquals("jwala/dev-index-page-scripts", result);
        result = ic.indexPageScripts("false", false);
        assertEquals("jwala/prod-index-page-scripts", result);
        result = ic.indexPageScripts("false", true);
        assertEquals("jwala/prod-index-page-scripts", result);
        result = ic.indexPageScripts(null, true);
        assertEquals("jwala/dev-index-page-scripts", result);
        result = ic.indexPageScripts(null, false);
        assertEquals("jwala/prod-index-page-scripts", result);
    }

    public void testDevModeTrue() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ModelAndView mv = ic.devMode("true", resp);
        verify(resp).addCookie(any(Cookie.class));
        assertNotNull(mv);
        assertEquals("{devMode=true}", mv.getModel().toString());
    }

    public void testDevModeFalse() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ModelAndView mv = ic.devMode("false", resp);
        verify(resp).addCookie(any(Cookie.class));
        assertNotNull(mv);
        assertEquals("{devMode=false}", mv.getModel().toString());
    }

    public void testLogin() {
        assertEquals("jwala/login", ic.login());
    }

    public void testLoginPageScripts() {
        String result = ic.loginPageScripts("true", false);
        assertEquals("jwala/dev-login-page-scripts", result);
        result = ic.loginPageScripts("true", true);
        assertEquals("jwala/dev-login-page-scripts", result);
        result = ic.loginPageScripts("false", false);
        assertEquals("jwala/prod-login-page-scripts", result);
        result = ic.loginPageScripts("false", true);
        assertEquals("jwala/prod-login-page-scripts", result);
        result = ic.loginPageScripts(null, true);
        assertEquals("jwala/dev-login-page-scripts", result);
        result = ic.loginPageScripts(null, false);
        assertEquals("jwala/prod-login-page-scripts", result);
    }
}
