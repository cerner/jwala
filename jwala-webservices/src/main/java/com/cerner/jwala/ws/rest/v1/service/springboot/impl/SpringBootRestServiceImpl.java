package com.cerner.jwala.ws.rest.v1.service.springboot.impl;

import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;
import com.cerner.jwala.service.springboot.SpringBootService;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.springboot.SpringBootRestService;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 6/1/2017.
 */
public class SpringBootRestServiceImpl implements SpringBootRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootRestServiceImpl.class);

    @Autowired
    SpringBootService springBootService;

    @Override
    public Response controlSpringBoot(String name, String command) {
        LOGGER.info("Control Spring Boot {} with command {}", name, command);
        return ResponseBuilder.ok(springBootService.controlSpringBoot(name, command));
    }

    @Override
    public Response generateAndDeploy(String name) {
        LOGGER.info("Generate and deploy Spring Boot {}", name);
        return ResponseBuilder.ok(springBootService.generateAndDeploy(name));
    }

    @Override
    public Response createSpringBoot(List<Attachment> attachments) {
        LOGGER.info("Create Spring Boot {}", attachments);

        if (attachments == null || attachments.isEmpty()) {
            LOGGER.error("Expected non-empty attachments. Returning without creating Media.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final Map<String, String> springBootDataMap = new HashMap<>();
        final Map<String, Object> springBootFileDataMap = new HashMap<>();

        attachments.forEach(attachment -> {
            try {
                // when using the REST API from Chef a null attachment gets added somewhere between the execute_rest and here
                if (attachment.getHeaders().size() < 2) {
                    return;
                }
                if (attachment.getHeader("Content-Type") == null) {
                    springBootDataMap.put(attachment.getDataHandler().getName(),
                            IOUtils.toString(attachment.getDataHandler().getInputStream(), Charset.defaultCharset()));
                } else {
                    springBootFileDataMap.put("filename", attachment.getDataHandler().getName());
                    springBootFileDataMap.put("content", new BufferedInputStream(attachment.getDataHandler().getInputStream()));
                }
            } catch (final IOException e) {
                LOGGER.error("Failed to retrieve attachments!", e);
                Response.status(Response.Status.BAD_REQUEST).build();
            }
        });

        return ResponseBuilder.created(springBootService.createSpringBoot(springBootDataMap, springBootFileDataMap));
    }

    @Override
    public Response updateSpringBoot(JpaSpringBootApp springBootApp) {
        LOGGER.info("Update Spring Boot {}", springBootApp);
        return ResponseBuilder.ok(springBootService.update(springBootApp));
    }

    @Override
    public Response removeSpringBoot(JpaSpringBootApp  springBootApp) {
        LOGGER.info("Remove Spring Boot {}", springBootApp);
        springBootService.remove(springBootApp);
        return ResponseBuilder.ok();
    }

    @Override
    public Response findSpringBoot(String name) {
        LOGGER.info("Get Spring Boot {}", name);
        return ResponseBuilder.ok(springBootService.find(name));
    }
}
