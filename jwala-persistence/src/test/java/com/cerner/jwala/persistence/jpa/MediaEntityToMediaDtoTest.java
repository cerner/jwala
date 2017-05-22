package com.cerner.jwala.persistence.jpa;

import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by Jedd Cuison on 4/25/2017
 */

public class MediaEntityToMediaDtoTest {

    @Test
    public void testMediaEntityToMediaDto() throws Exception {
        final JpaMedia jpaMedia = new JpaMedia();
        jpaMedia.setId(1L);
        jpaMedia.setName("JDK Lite");
        jpaMedia.setType(MediaType.JDK);
        jpaMedia.setLocalPath(Paths.get("localPath"));
        jpaMedia.setMediaDir(Paths.get("mediaDir"));
        jpaMedia.setRemoteDir(Paths.get("remoteDir"));
        final Media media = new ModelMapper().map(jpaMedia, Media.class);

        assertEquals(1L, media.getId().longValue());
        assertEquals("JDK Lite", media.getName());
        assertEquals(MediaType.JDK, media.getType());
        assertEquals("localPath", media.getLocalPath().toString());
        assertEquals("mediaDir", media.getMediaDir().toString());
        assertEquals("remoteDir", media.getRemoteDir().toString());
    }

}
