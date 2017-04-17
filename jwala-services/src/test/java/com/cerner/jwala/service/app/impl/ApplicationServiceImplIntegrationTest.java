package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.configuration.SshConfig;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.dao.impl.MediaDaoImpl;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.persistence.jpa.service.impl.*;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.persistence.service.impl.JpaApplicationPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.app.ApplicationCommandService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionLockManagerImpl;
import com.cerner.jwala.service.configuration.TestJpaConfiguration;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        ApplicationServiceImplIntegrationTest.CommonConfiguration.class,
        TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class ApplicationServiceImplIntegrationTest {

    @Mock
    private SshConfig sshConfig;

    @Mock
    private GroupPersistenceService mockGroupPersistenceService;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private ResourceService mockResourceService;

    @Configuration
    static class CommonConfiguration {

        @Bean
        public WebServerCrudService getWebServerDao() {
            return new WebServerCrudServiceImpl();
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
        public ApplicationPersistenceService getApplicationPersistenceService() {
            return new JpaApplicationPersistenceServiceImpl(getApplicationCrudService(), getGroupCrudService());
        }

        @Bean
        @Autowired
        public JvmPersistenceService getJvmPersistenceService(final GroupJvmRelationshipService groupJvmRelationshipService) {
            return new JpaJvmPersistenceServiceImpl(new JvmCrudServiceImpl(), getApplicationCrudService(), groupJvmRelationshipService);
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(new GroupCrudServiceImpl(), new JvmCrudServiceImpl());
        }

        @Bean
        public ApplicationCommandService getApplicationCommandService() {
            final SshConfiguration sshConfiguration = new SshConfiguration("JeddCuison", 22, "", "", "MrI6SA43vbcIws0pJygEDA==".toCharArray());
            JschBuilder jschBuilder = new JschBuilder();
            return new ApplicationCommandServiceImpl(sshConfiguration, jschBuilder);
        }

        @Bean(name = "httpRequestFactory")
        public HttpComponentsClientHttpRequestFactory getHttpClientRequestFactory() throws Exception {
            return new HttpComponentsClientHttpRequestFactory();
        }

        @Bean
        public ClientFactoryHelper getClientFactoryHelper() {
            return new ClientFactoryHelper();
        }

        @Bean
        public MediaDao getMediaDao() {
            return new MediaDaoImpl();
        }

    }

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    private ApplicationService applicationService;

    BinaryDistributionLockManager binaryDistributionLockManager;

    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    private BinaryDistributionService binaryDistributionService;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        sshConfig = mock(SshConfig.class);
        mockGroupPersistenceService = mock(GroupPersistenceService.class);
        binaryDistributionService = mock(BinaryDistributionService.class);
        when(mockSshConfig.getUserName()).thenReturn("mockUser");
        when(sshConfig.getSshConfiguration()).thenReturn(mockSshConfig);
        binaryDistributionLockManager = new BinaryDistributionLockManagerImpl();
        applicationService = new ApplicationServiceImpl(
                applicationPersistenceService,
                jvmPersistenceService,
                mockGroupPersistenceService,
                mockResourceService,
                binaryDistributionService,
                mockHistoryFacadeService,
                binaryDistributionLockManager
        );
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }


    /**
     * With this revision there is no capacity to create. Therefore integration
     * testing at the service layer can only return NotFound.
     * We'll mock the rest.
     */
    @Test(expected = NotFoundException.class)
    public void testGetApplication() {
        applicationService.getApplication(new Identifier<Application>(0L));
    }

    /**
     * Test getting the full list.
     */
    @Test
    public void testGetAllApplications() {
        List<Application> all = applicationService.getApplications();
        assertEquals(0, all.size());
    }

    /**
     * Test getting the partial list.
     */
    @Test
    public void testGetApplicationsByGroup() {
        List<Application> partial = applicationService.findApplications(new Identifier<Group>(0L));
        assertEquals(0, partial.size());
    }

}
