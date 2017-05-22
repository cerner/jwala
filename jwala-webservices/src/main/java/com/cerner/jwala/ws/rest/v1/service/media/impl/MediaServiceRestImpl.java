package com.cerner.jwala.ws.rest.v1.service.media.impl;

import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.service.media.MediaService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.media.MediaServiceRest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link MediaServiceRest}
 */
@Service
public class MediaServiceRestImpl implements MediaServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaServiceRestImpl.class);

    @Autowired
    private MediaService mediaService;

    @Override
    public Response createMedia(final List<Attachment> attachments) {
        LOGGER.info("createMedia {}", attachments);
        if (attachments == null || attachments.isEmpty()) {
            LOGGER.error("Expected non-empty attachments. Returning without creating Media.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final Map<String, String> mediaDataMap = new HashMap<>();
        final Map<String, Object> mediaFileDataMap = new HashMap<>();
        attachments.forEach(attachment -> {
            try {
                // when using the REST API from Chef a null attachment gets added somewhere between the execute_rest and here
                if (attachment.getHeaders().size() < 2) {
                    return;
                }
                if (attachment.getHeader("Content-Type") == null) {
                    mediaDataMap.put(attachment.getDataHandler().getName(),
                            IOUtils.toString(attachment.getDataHandler().getInputStream(), Charset.defaultCharset()));
                } else {
                    mediaFileDataMap.put("filename", attachment.getDataHandler().getName());
                    mediaFileDataMap.put("content", new BufferedInputStream(attachment.getDataHandler().getInputStream()));
                }
            } catch (final IOException e) {
                LOGGER.error("Failed to retrieve attachments!", e);
                Response.status(Response.Status.BAD_REQUEST).build();
            }
        });

        return ResponseBuilder.created(mediaService.create(mediaDataMap, mediaFileDataMap));
    }

    @Override
    public Response updateMedia(final JpaMedia media, final AuthenticatedUser aUser) {
        LOGGER.info("updateMedia {} by user {}", media, aUser.getUser().getId());
        return ResponseBuilder.ok(mediaService.update(media));
    }

    @Override
    public Response removeMedia(final String name, String type, final AuthenticatedUser aUser) {
        MediaType mediaType = null;
        if (MediaType.JDK.getDisplayName().equals(type)) {
            mediaType = MediaType.JDK;
        }
        if (MediaType.APACHE.getDisplayName().equals(type)) {
            mediaType = MediaType.APACHE;
        }
        if (MediaType.TOMCAT.getDisplayName().equals(type)) {
            mediaType = MediaType.TOMCAT;
        }
        LOGGER.info("removeMedia {} by user {}", name, aUser.getUser().getId());
        mediaService.remove(name, mediaType);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getMedia(final Long id, final String mediaName, final AuthenticatedUser aUser) {
        LOGGER.debug("getMedia with ID {} and name {} by user", id, mediaName, aUser.getUser().getId());
        if (id == null && StringUtils.isEmpty(mediaName)) {
            return ResponseBuilder.ok(mediaService.findAll());
        } else if (id != null) {
            return ResponseBuilder.ok(mediaService.find(id));
        }
        return ResponseBuilder.ok(mediaService.find(mediaName));
    }

    @Override
    public Response getMediaTypes() {
        LOGGER.debug("getMediaTypes");
        return ResponseBuilder.ok(mediaService.getMediaTypes());
    }

    public void setMediaService(MediaService mediaService) {
        LOGGER.debug("setMediaService");
        this.mediaService = mediaService;
    }
}
