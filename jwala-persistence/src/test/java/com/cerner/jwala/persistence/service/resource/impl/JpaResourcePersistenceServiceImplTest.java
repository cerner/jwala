package com.cerner.jwala.persistence.service.resource.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.ResourcePersistenceService;
import com.cerner.jwala.persistence.service.impl.JpaApplicationPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.JpaResourcePersistenceServiceImpl;
import com.cerner.jwala.persistence.service.resource.AbstractResourcePersistenceServiceTest;

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
        classes = {JpaResourcePersistenceServiceImplTest.Config.class
        })

public class JpaResourcePersistenceServiceImplTest extends AbstractResourcePersistenceServiceTest{

    @Configuration
    @Import(TestJpaConfiguration.class)

    static class Config {

        @Bean
        public ResourcePersistenceService getResourcePersistenceService(){
            return new JpaResourcePersistenceServiceImpl();
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(), getGroupJvmRelationshipService());
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(), getJvmCrudService());
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public ApplicationPersistenceService getApplicationPersistenceService(){
            return new JpaApplicationPersistenceServiceImpl(getApplicationCrudService(), getGroupCrudService());
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }
    }
}
