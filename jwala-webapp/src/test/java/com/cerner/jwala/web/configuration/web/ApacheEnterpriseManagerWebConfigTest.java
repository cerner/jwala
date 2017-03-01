package com.cerner.jwala.web.configuration.web;

import junit.framework.TestCase;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;

import com.cerner.jwala.web.configuration.web.ApacheEnterpriseManagerWebConfig;

public class ApacheEnterpriseManagerWebConfigTest extends TestCase {

    private ApacheEnterpriseManagerWebConfig aemWC;

    @Override
    protected void setUp() {
        aemWC = new ApacheEnterpriseManagerWebConfig();
    }

    public void testConfigureContentNegotiationContentNegotiationConfigurer() {
        // This method will be tested in integration tests.
    }

    public void testAddResourceHandlersResourceHandlerRegistry() {
        // This method will be tested in integration tests.
    }

    public void testViewResolver() {
        final ViewResolver viewResolver = aemWC.viewResolver();
        assertNotNull(viewResolver);
    }

    public void testMessageSource() {
        final MessageSource messageSource = aemWC.messageSource();
        assertNotNull(messageSource);
    }

    public void testLocaleResolver() {
        final LocaleResolver localeResolver = aemWC.localeResolver();
        assertNotNull(localeResolver);
    }
}
