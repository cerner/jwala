package com.cerner.jwala.persistence.service.webserver.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.service.*;
import com.cerner.jwala.persistence.jpa.service.impl.*;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.WebServerPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.webserver.AbstractWebServerPersistenceServiceTest;

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
        classes = {JpaWebServerPersistenceServiceImplTest.Config.class
        })
public class JpaWebServerPersistenceServiceImplTest extends AbstractWebServerPersistenceServiceTest{

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService(){
            return new WebServerPersistenceServiceImpl(getGroupCrudServiceImpl(), getWebServerCrudServiceImpl());
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService(){
            return new JpaGroupPersistenceServiceImpl(
                    getGroupCrudServiceImpl(),
                    getGroupJvmRelationshipService(),
                    getApplicationCrudService());
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public GroupCrudService getGroupCrudServiceImpl(){
            return new GroupCrudServiceImpl();
        }

        @Bean
        public WebServerCrudService getWebServerCrudServiceImpl(){
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService(){
            return new GroupJvmRelationshipServiceImpl(getGroupCrudServiceImpl(), getJvmCrudServiceImpl());
        }

        @Bean
        public JvmCrudService getJvmCrudServiceImpl(){
            return new JvmCrudServiceImpl();
        }

    }
}
