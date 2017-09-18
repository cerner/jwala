package com.cerner.jwala.persistence.service.jvm.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.dao.impl.MediaDaoImpl;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.jvm.AbstractJvmPersistenceServiceTest;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {JpaJvmPersistenceServiceImplTest.Config.class
        })
public class JpaJvmPersistenceServiceImplTest extends AbstractJvmPersistenceServiceTest {

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(), getGroupJvmRelationshipService(),
                    getApplicationCrudService());
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return new JpaJvmPersistenceServiceImpl(getJvmCrudService(), getApplicationCrudService(), getGroupJvmRelationshipService());
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(),
                    getJvmCrudService());
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public MediaDao getMediaDao() {
            return new MediaDaoImpl();
        }
    }

}
