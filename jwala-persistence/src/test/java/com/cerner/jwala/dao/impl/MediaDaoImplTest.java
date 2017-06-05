package com.cerner.jwala.dao.impl;

import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.dao.configuration.TestConfiguration;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link MediaDaoImpl}
 *
 * Created by Jedd Anthony Cuison on 12/6/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {MediaDaoImplTest.Config.class})
@Transactional
public class MediaDaoImplTest {

    @Autowired
    private MediaDao mediaDao;

    @Test
    public void testCrud() {
        final JpaMedia media = new JpaMedia();
        media.setName("jdk 1.8");
        media.setType(MediaType.JDK);
        media.setLocalPath(Paths.get("c:/java/jdk.zip"));
        media.setRemoteDir(Paths.get("c:/ctp"));
        media.setRootDir(Paths.get("jdk-1.8"));
        mediaDao.create(media);
        final JpaMedia foundMedia = mediaDao.find("jdk 1.8");
        assertEquals(media, foundMedia);
        assertEquals(mediaDao.findAll().size(), 1);
        mediaDao.remove(media);
        assertEquals(mediaDao.findAll().size(), 0);
    }

    @Configuration
    @Import(TestConfiguration.class)
    @ComponentScan({"com.cerner.jwala.dao.impl"})
    static class Config {

        @PersistenceContext(unitName = "jwala-unit")
        private EntityManager em;

        @Bean
        public EntityManager getEntityManager() {
            return em;
        }

    }

}
