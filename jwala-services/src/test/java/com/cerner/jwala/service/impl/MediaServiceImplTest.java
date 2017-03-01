package com.cerner.jwala.service.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.service.media.MediaService;
import com.cerner.jwala.service.media.impl.MediaServiceImpl;
import com.cerner.jwala.service.repository.RepositoryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * {@link MediaServiceImpl} unit tests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {MediaServiceImplTest.Config.class})
public class MediaServiceImplTest {

    @Autowired
    private MediaService mediaService;

    @Mock
    private JpaMedia mockMedia;

    @Before
    public void setUp() {
        initMocks(this);
        when(mockMedia.getName()).thenReturn("tomcat");
    }

    @Test
    public void testFindById() {
        when(Config.MOCK_MEDIA_DAO.findById(eq(1L))).thenReturn(mockMedia);
        assertEquals(mediaService.find(1L).getName(), "tomcat");
    }

    @Test
    public void testFindByName() {
        when(Config.MOCK_MEDIA_DAO.find(anyString())).thenReturn(mockMedia);
        JpaMedia result = mediaService.find("tomcat");
        assertEquals(mockMedia.getName(), result.getName());
    }

    @Test
    public void testCreate() {
        final Map<String, String> dataMap = new HashMap<>();
        dataMap.put("name", "tomcat");
        dataMap.put("type", "TOMCAT");
        dataMap.put("remoteDir", "c:/tomcat");

        final Map<String, Object> mediaFileDataMap = new HashMap<>();
        mediaFileDataMap.put("filename", "apache-tomcat-8.5.9.zip");
        mediaFileDataMap.put("content", new BufferedInputStream(new ByteArrayInputStream("the content".getBytes())));

        when(Config.MOCK_MEDIA_REPOSITORY_SERVICE.upload(anyString(), any(InputStream.class)))
                .thenReturn("c:/jwala/toc/data/bin/apache-tomcat-8.5.9-89876567321.zip");
        final Set<String> rootDirSet = new HashSet<>();
        rootDirSet.add("apache-tomcat-8.5.9");
        when(Config.MOCK_FILE_UTILITY.getZipRootDirs(eq("c:/jwala/toc/data/bin/apache-tomcat-8.5.9-89876567321.zip")))
                .thenReturn(rootDirSet);
        mediaService.create(dataMap, mediaFileDataMap);
        verify(Config.MOCK_MEDIA_DAO).create(any(JpaMedia.class));
    }

    @Test
    public void testUpdate() {
        mediaService.update(mockMedia);
        verify(Config.MOCK_MEDIA_DAO).update(any(JpaMedia.class));
    }

    @Test
    public void testRemove() {
        when(Config.MOCK_MEDIA_DAO.find(eq("tomcat"))).thenReturn(mockMedia);
        when(mockMedia.getLocalPath()).thenReturn(Paths.get("/apache/tomcat.zip"));
        mediaService.remove("tomcat");
        verify(Config.MOCK_MEDIA_REPOSITORY_SERVICE).delete(eq("tomcat.zip"));
        verify(Config.MOCK_MEDIA_DAO).remove(any(JpaMedia.class));
    }

    @Test
    public void testFindAll() {
        final List<JpaMedia> mediaList = new ArrayList<>();
        mediaList.add(mockMedia);
        when(Config.MOCK_MEDIA_DAO.findAll()).thenReturn(mediaList);
        final List<JpaMedia> result = mediaService.findAll();
        assertEquals(result.get(0).getName(), mockMedia.getName());
    }

    @Test
    public void testGetMediaTypes() {
        assertEquals(MediaType.values().length, mediaService.getMediaTypes().length);
    }

    @Configuration
    static class Config {

        private static final MediaDao MOCK_MEDIA_DAO = mock(MediaDao.class);
        private static final RepositoryService MOCK_MEDIA_REPOSITORY_SERVICE = mock(RepositoryService.class);
        private static final FileUtility MOCK_FILE_UTILITY = mock(FileUtility.class);

        @Bean
        public MediaDao getMediaDao() {
            return MOCK_MEDIA_DAO;
        }

        @Bean(name = "mediaRepositoryService")
        public RepositoryService getMediaRepositoryService() {
            return MOCK_MEDIA_REPOSITORY_SERVICE;
        }

        @Bean
        public FileUtility getFileUtility() {
            return MOCK_FILE_UTILITY;
        }

        @Bean
        public MediaService getMediaService() {
            return new MediaServiceImpl();
        }

    }

}
