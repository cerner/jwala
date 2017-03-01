package com.cerner.jwala.service.bootstrap;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.media.MediaService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 2/6/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ApplicationContextListenerTest.Config.class})
public class ApplicationContextListenerTest {

    @Autowired
    private ApplicationContextListener applicationContextListener;
    private ContextRefreshedEvent mockStartupEvent;
    private Jvm mockJvm;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        reset(Config.jvmServiceMock);
        reset(Config.mediaServiceMock);

        mockStartupEvent = mock(ContextRefreshedEvent.class);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ApplicationContext mockParent = mock(ApplicationContext.class);
        when(mockApplicationContext.getParent()).thenReturn(mockParent);
        when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

        mockJvm = mock(Jvm.class);
        Group mockGroup = mock(Group.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(mockJvm.getJvmName()).thenReturn("test-jvm-name");
        when(mockJvm.getHostName()).thenReturn("test-host-name");
        when(mockJvm.getGroups()).thenReturn(Collections.singleton(mockGroup));
        when(mockJvm.getHttpPort()).thenReturn(100);
        when(mockJvm.getHttpsPort()).thenReturn(101);
        when(mockJvm.getRedirectPort()).thenReturn(102);
        when(mockJvm.getShutdownPort()).thenReturn(-1);
        when(mockJvm.getAjpPort()).thenReturn(103);
        when(mockJvm.getStatusPath()).thenReturn(new Path("http://test-host-name:101/tomcat-power.gif"));
        when(mockJvm.getSystemProperties()).thenReturn("");
        when(mockJvm.getUserName()).thenReturn("");
        when(mockJvm.getEncryptedPassword()).thenReturn("");
    }

    @Test
    public void testNoApplicationContext() {
        JpaMedia mockJdkMedia = mock(JpaMedia.class);
        when(mockJdkMedia.getType()).thenReturn(MediaType.JDK);
        List<JpaMedia> mediaList = Collections.singletonList(mockJdkMedia);

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        applicationContextListener.handleEvent(mockStartupEvent);

        verify(Config.jvmServiceMock, never()).updateJvm(any(UpdateJvmRequest.class), eq(true));
        verify(Config.mediaServiceMock, never()).create(anyMap(), anyMap());
    }

    @Test
    public void testNoParentForApplicationContext() {
        JpaMedia mockJdkMedia = mock(JpaMedia.class);
        when(mockJdkMedia.getType()).thenReturn(MediaType.JDK);
        List<JpaMedia> mediaList = Collections.singletonList(mockJdkMedia);

        when(Config.mediaServiceMock.findAll()).thenReturn(mediaList);
        ContextRefreshedEvent mockStartupEvent = mock(ContextRefreshedEvent.class);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockStartupEvent.getApplicationContext()).thenReturn(mockApplicationContext);

        applicationContextListener.handleEvent(mockStartupEvent);

        verify(Config.jvmServiceMock, never()).updateJvm(any(UpdateJvmRequest.class), eq(true));
        verify(Config.mediaServiceMock, never()).create(anyMap(), anyMap());
    }

    @Test
    public void testApplicationContextWithParent() {
        applicationContextListener.handleEvent(mockStartupEvent);

        verify(Config.jvmServiceMock, never()).updateJvm(any(UpdateJvmRequest.class), eq(true));
        verify(Config.mediaServiceMock, never()).create(anyMap(), anyMap());
    }

    @Configuration
    static class Config {

        private static final MediaService mediaServiceMock = mock(MediaService.class);
        private static final JvmService jvmServiceMock = mock(JvmService.class);

        @Bean
        public MediaService getMediaService() {
            return mediaServiceMock;
        }

        @Bean
        public JvmService getJvmService() {
            return jvmServiceMock;
        }

        @Bean
        public ApplicationContextListener getApplicationContextListener() {
            return new ApplicationContextListener();
        }

    }
}
