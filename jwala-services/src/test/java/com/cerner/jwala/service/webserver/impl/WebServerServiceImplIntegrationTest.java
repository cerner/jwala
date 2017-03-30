package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.persistence.service.impl.WebServerPersistenceServiceImpl;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.configuration.TestJpaConfiguration;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.state.impl.InMemoryStateManagerServiceImpl;
import com.cerner.jwala.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        WebServerServiceImplIntegrationTest.CommonConfiguration.class,
        TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class WebServerServiceImplIntegrationTest {

    private InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemService = new InMemoryStateManagerServiceImpl<>();

    @Configuration
    static class CommonConfiguration {

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService() {
            return new WebServerPersistenceServiceImpl(getGroupCrudService(), getWebServerCrudService());
        }

        @Bean
        public WebServerCrudService getWebServerCrudService() {
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

    }

    @Autowired
    private WebServerPersistenceService webServerPersistenceService;

    private WebServerService webServerService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Before
    public void setup() {
        webServerService = new WebServerServiceImpl(webServerPersistenceService, resourceService, inMemService, "d:/jwala/app/data/types", binaryDistributionLockManager);
    }

    @Test(expected = NotFoundException.class)
    @Ignore
    // TODO: Fix this!
    public void testServiceLayer() {
        webServerService.getWebServer(new Identifier<WebServer>(0L));
    }
}
