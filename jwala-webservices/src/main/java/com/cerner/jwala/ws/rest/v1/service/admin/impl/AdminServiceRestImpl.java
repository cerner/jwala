package com.cerner.jwala.ws.rest.v1.service.admin.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.files.FilesConfiguration;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.ws.rest.response.ResponseContent;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.admin.AdminServiceRest;

import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class AdminServiceRestImpl implements AdminServiceRest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminServiceRestImpl.class);
    public static final String JSON_RESPONSE_TRUE = "true";
    public static final String JSON_RESPONSE_FALSE = "false";

    private static final String JWALA_AUTHORIZATION = "jwala.authorization";

    private FilesConfiguration filesConfiguration;
    private ResourceService resourceService;

    @Autowired
    PropertySourcesPlaceholderConfigurer configurer;

    public AdminServiceRestImpl(FilesConfiguration theFilesConfiguration, ResourceService resourceService) {
        this.filesConfiguration = theFilesConfiguration;
        this.resourceService = resourceService;
    }

    @Override
    public Response reload() {
        ApplicationProperties.reload();
        Properties copyToReturn = ApplicationProperties.getProperties();
        configurer.setProperties(copyToReturn);

        filesConfiguration.reload();

        LogManager.resetConfiguration();
        DOMConfigurator.configure("../data/conf/log4j.xml");

        copyToReturn.put("logging-reload-state", "Property reload complete");

        return ResponseBuilder.ok(new TreeMap<>(copyToReturn));
    }

    @Override
    public Response view() {
        return ResponseBuilder.ok(new TreeMap<>(ApplicationProperties.getProperties()));
    }


    @Override
    public Response encrypt(String cleartext) {

        if (cleartext == null || cleartext.trim().length() == 0) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return ResponseBuilder.ok(resourceService.encryptUsingPlatformBean(cleartext));
        }
    }

    @Override
    public Response manifest(ServletContext context) {
        Attributes attributes = null;
        if (context != null) {
            try {
                InputStream manifestStream = context.getResourceAsStream("META-INF/MANIFEST.MF");
                Manifest manifest = new Manifest(manifestStream);
                attributes = manifest.getMainAttributes();
            } catch (IOException e) {
                LOGGER.debug("Error getting manifest for " + context.getServletContextName(), e);
                throw new InternalErrorException(FaultType.INVALID_PATH, "Failed to read MANIFEST.MF for "
                        + context.getServletContextName());
            }
        }
        return ResponseBuilder.ok(attributes);
    }

    @Override
    public Response isJwalaAuthorizationEnabled() {
        String auth = ApplicationProperties.get(JWALA_AUTHORIZATION, "true");
        if("false".equals(auth))
            return ResponseBuilder.ok(JSON_RESPONSE_FALSE);
        else
            return ResponseBuilder.ok(JSON_RESPONSE_TRUE);
    }

    @Override
    public Response getAuthorizationDetails() {
        return ResponseBuilder.ok(new ResponseContent() {
            private static final String TRUE = "true";
            private final String authEnabled = ApplicationProperties.get(JWALA_AUTHORIZATION, TRUE);

            public String getAuthorizationEnabled() {
                return authEnabled;
            }

            @SuppressWarnings("unchecked")
            public Collection<GrantedAuthority> getUserAuthorities() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (authEnabled.equalsIgnoreCase(TRUE) && auth != null) {
                    return (Collection<GrantedAuthority>) auth.getAuthorities();
                }
                return null;
            }
        });
    }
}
