package com.cerner.jwala.ws.rest.v1.service.media.impl;

import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.service.media.MediaService;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.activation.DataHandler;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Rahul Sayini on 12/13/2016.
 */

@RunWith(MockitoJUnitRunner.class)
public class MediaServiceRestImplTest {

    @Mock
    private MediaService mediaService;
    @Mock
    private AuthenticatedUser authenticatedUser;

    private static final List<JpaMedia> mediaList = createMediaList();

    private static List<JpaMedia> createMediaList() {
        JpaMedia ms = new JpaMedia();
        ms.setId(1L);
        ms.setName("jdk 1.8");
        ms.setType(MediaType.JDK);
        ms.setLocalPath(Paths.get("c:/java/jdk.zip"));
        ms.setRemoteDir(Paths.get("c:/ctp"));
        ms.setMediaDir(Paths.get("jdk-1.8"));
        final List<JpaMedia> result = new ArrayList<>();
        result.add(ms);

        return result;
    }

    @Mock
    private MediaServiceRestImpl mediaServiceRest;

    private JpaMedia media;

    @Before
    public void setUp() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, "./src/test/resources");
        mediaServiceRest = new MediaServiceRestImpl();
        mediaServiceRest.setMediaService(mediaService);
        when(authenticatedUser.getUser()).thenReturn(new User("Unused"));

        media = new JpaMedia();
        media.setId(1L);
        media.setName("jdk 1.8");
        media.setType(MediaType.JDK);
        media.setLocalPath(Paths.get("c:/java/jdk.zip"));
        media.setRemoteDir(Paths.get("c:/ctp"));
        media.setMediaDir(Paths.get("jdk-1.8"));

        mediaService = mock(MediaService.class);
    }

    @Test
    public void testGetAllMedia() {
        when(mediaService.findAll()).thenReturn(mediaList);

        final Response response = mediaServiceRest.getMedia(null, "", authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetMediaById() {
        when(mediaService.find(anyLong())).thenReturn(media);

        final Response response = mediaServiceRest.getMedia(1L, "jdk 1.8", authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetMediaByName() {
        when(mediaService.find(anyString())).thenReturn(media);

        final Response response = mediaServiceRest.getMedia(null, "jdk 1.8", authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRemoveMedia() {
        Response response = mediaServiceRest.removeMedia("jdk 1.8", authenticatedUser);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGetMediaTypes() {
        when(mediaService.getMediaTypes()).thenReturn(MediaType.values());
        Response response = mediaServiceRest.getMediaTypes();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateMediaCheckForNull() {
        List<Attachment> attachments = null;
        Response response = mediaServiceRest.createMedia(attachments);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateMedia() throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        InputStream inputStream = mock(InputStream.class);
        Attachment attachment = mock(Attachment.class);
        DataHandler dataHandler = mock(DataHandler.class);
        when(attachment.getDataHandler()).thenReturn(dataHandler);
        when(dataHandler.getName()).thenReturn("mockString");
        when(dataHandler.getInputStream()).thenReturn(inputStream);
        MultivaluedMap<String, String> multimap = mock(MultivaluedMap.class);
        when(multimap.size()).thenReturn(2);
        when(attachment.getHeaders()).thenReturn(multimap);

        attachments.add(attachment);
        Response response = mediaServiceRest.createMedia(attachments);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    }

    @Test
    public void testUpdateMedia() {
        when(mediaService.update(any(JpaMedia.class))).thenReturn(media);
        Response response = mediaServiceRest.updateMedia(media, authenticatedUser);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

}
