package com.cerner.jwala.service.configuration.service;

import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.KeyedPooledJschChannelFactory;
import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.configuration.AemCommandExecutorConfig;
import com.cerner.jwala.control.configuration.AemSshConfig;
import com.cerner.jwala.persistence.configuration.AemPersistenceServiceConfiguration;
import com.cerner.jwala.persistence.jpa.service.*;
import com.cerner.jwala.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.cerner.jwala.persistence.service.*;
import com.cerner.jwala.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import com.cerner.jwala.persistence.service.impl.ResourceDaoImpl;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.app.ApplicationCommandService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.app.impl.ApplicationCommandServiceImpl;
import com.cerner.jwala.service.app.impl.ApplicationServiceImpl;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerHtmlParser;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerHttpClient;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerServiceImpl;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerXmlParser;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionControlServiceImpl;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionLockManagerImpl;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionServiceImpl;
import com.cerner.jwala.service.bootstrap.ApplicationContextListener;
import com.cerner.jwala.service.group.*;
import com.cerner.jwala.service.group.impl.GroupControlServiceImpl;
import com.cerner.jwala.service.group.impl.GroupJvmControlServiceImpl;
import com.cerner.jwala.service.group.impl.GroupServiceImpl;
import com.cerner.jwala.service.group.impl.GroupWebServerControlServiceImpl;
import com.cerner.jwala.service.impl.HistoryServiceImpl;
import com.cerner.jwala.service.initializer.JGroupsClusterInitializer;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.jvm.impl.JvmControlServiceImpl;
import com.cerner.jwala.service.jvm.impl.JvmServiceImpl;
import com.cerner.jwala.service.jvm.state.JvmStateReceiverAdapter;
import com.cerner.jwala.service.repository.RepositoryService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceServiceImpl;
import com.cerner.jwala.service.resource.impl.handler.WebServerResourceHandler;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.state.impl.InMemoryStateManagerServiceImpl;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.WebServerStateRetrievalScheduledTaskHandler;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import com.cerner.jwala.service.webserver.component.WebServerStateSetterWorker;
import com.cerner.jwala.service.webserver.impl.WebServerCommandServiceImpl;
import com.cerner.jwala.service.webserver.impl.WebServerControlServiceImpl;
import com.cerner.jwala.service.webserver.impl.WebServerServiceImpl;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan({"com.cerner.jwala.service.webserver.component",
                "com.cerner.jwala.service.state",
                "com.cerner.jwala.service.spring.component",
                "com.cerner.jwala.commandprocessor.jsch.impl.spring.component",
                "com.cerner.jwala.service.group.impl.spring.component",
                "com.cerner.jwala.service.jvm.impl.spring.component",
                "com.cerner.jwala.service.impl.spring.component",
                "com.cerner.jwala.service.resource.impl",
                "com.cerner.jwala.common",
                "com.cerner.jwala.service.impl",
                "com.cerner.jwala.service.media.impl",
                "com.cerner.jwala.common.jsch.impl",
                "com.cerner.jwala.service.jvm.impl",
                "com.cerner.jwala.service.jvm.operation.impl",
                "com.cerner.jwala.control.jvm.command",
                "com.cerner.jwala.control.webserver.command",
                "com.cerner.jwala.commandprocessor.impl.jsch",
                "com.cerner.jwala.control.command.common"})
public class AemServiceConfiguration {

    @Autowired
    private AemPersistenceServiceConfiguration aemPersistenceServiceConfiguration;

    @Autowired
    private AemCommandExecutorConfig aemCommandExecutorConfig;

    @Autowired
    private AemSshConfig aemSshConfig;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Autowired
    private GroupStateNotificationService groupStateNotificationService;

    @Autowired
    private BinaryDistributionService binaryDistributionService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    /**
     * Make vars.properties available to spring integration configuration
     * System properties are only used if there is no setting in vars.properties.
     */
    @Bean(name = "aemServiceConfigurationPropertiesConfigurer")
    public static PropertySourcesPlaceholderConfigurer configurer() {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        ppc.setLocation(new ClassPathResource("META-INF/spring/jwala-defaults.properties"));
        ppc.setLocalOverride(true);
        ppc.setProperties(ApplicationProperties.getProperties());
        return ppc;
    }

    @Bean
    public GroupService getGroupService(final HistoryFacadeService historyFacadeService) {
        return new GroupServiceImpl(aemPersistenceServiceConfiguration.getGroupPersistenceService(),
                                    aemPersistenceServiceConfiguration.getApplicationPersistenceService(),
                                    resourceService);
    }

    @Bean(name = "jvmService")
    public JvmService getJvmService(final GroupPersistenceService groupPersistenceService,
                                    final ApplicationService applicationService,
                                    final ResourceService resourceService, final ClientFactoryHelper clientFactoryHelper,
                                    @Value("${spring.messaging.topic.serverStates:/topic/server-states}") final String topicServerStates,
                                    final JvmControlService jvmControlService, final HistoryFacadeService historyFacadeService,
                                    final FileUtility fileUtility) {
        final JvmPersistenceService jvmPersistenceService = aemPersistenceServiceConfiguration.getJvmPersistenceService();
        return new JvmServiceImpl(jvmPersistenceService, groupPersistenceService, applicationService,
                messagingTemplate, groupStateNotificationService, resourceService,
                clientFactoryHelper, topicServerStates, jvmControlService, binaryDistributionService, binaryDistributionLockManager,
                historyFacadeService, fileUtility);
    }

    @Bean(name = "binaryDistributionLockManager")
    public BinaryDistributionLockManager getBinaryDistributionLockManager() {
        return new BinaryDistributionLockManagerImpl();
    }

    @Bean(name = "balancermanagerService")
    public BalancerManagerService getBalancermanagerService(final GroupService groupService,
                                                            final ApplicationService applicationService,
                                                            final WebServerService webServerService,
                                                            final JvmService jvmService,
                                                            final ClientFactoryHelper clientFactoryHelper,
                                                            final HistoryFacadeService historyFacadeService) {
        return new BalancerManagerServiceImpl(groupService, applicationService, webServerService, jvmService, clientFactoryHelper,
                                              new BalancerManagerHtmlParser(), new BalancerManagerXmlParser(jvmService),
                                              new BalancerManagerHttpClient(), historyFacadeService);
    }

    @Bean(name = "webServerService")
    public WebServerService getWebServerService(final ResourceService resourceService,
                                                @Qualifier("webServerInMemoryStateManagerService")
                                                final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService,
                                                @Value("${paths.resource-templates:../data/templates}") final String templatePath) {
        return new WebServerServiceImpl(
                aemPersistenceServiceConfiguration.getWebServerPersistenceService(),
                resourceService,
                inMemoryStateManagerService,
                templatePath,
                binaryDistributionLockManager);
    }

    @Bean
    public GroupJvmRelationshipService groupJvmRelationshipService(final GroupCrudService groupCrudService,
                                                                   final JvmCrudService jvmCrudService) {
        return new GroupJvmRelationshipServiceImpl(groupCrudService, jvmCrudService);
    }

    @Bean
    public JvmPersistenceService getJvmPersistenceService(final JvmCrudService jvmCrudService,
                                                          final ApplicationCrudService applicationCrudService,
                                                          final GroupJvmRelationshipService groupJvmRelationshipService) {
        return new JpaJvmPersistenceServiceImpl(jvmCrudService, applicationCrudService, groupJvmRelationshipService);
    }

    @Bean
    public ApplicationService getApplicationService(final JvmPersistenceService jvmPersistenceService, final GroupPersistenceService groupPersistenceService,
                                                    final ResourceService resourceService, final HistoryFacadeService historyFacadeService, BinaryDistributionLockManager lockManager) {
        return new ApplicationServiceImpl(aemPersistenceServiceConfiguration.getApplicationPersistenceService(),
                jvmPersistenceService, groupPersistenceService,
                resourceService,
                binaryDistributionService, historyFacadeService, lockManager);
    }

    @Bean(name = "jvmControlService")
    public JvmControlService getJvmControlService(final JvmStateService jvmStateService,
                                                  final HistoryFacadeService historyFacadeService) {
        return new JvmControlServiceImpl(
                aemPersistenceServiceConfiguration.getJvmPersistenceService(),
                jvmStateService,
                historyFacadeService);
    }

    @Bean(name = "groupControlService")
    public GroupControlService getGroupControlService(final GroupJvmControlService groupJvmControlService,
                                                      final GroupWebServerControlService groupWebServerControlService) {
        return new GroupControlServiceImpl(groupWebServerControlService, groupJvmControlService);
    }

    @Bean(name = "groupJvmControlService")
    public GroupJvmControlService getGroupJvmControlService(final GroupService groupService, final JvmControlService jvmControlService) {
        return new GroupJvmControlServiceImpl(groupService, jvmControlService);
    }

    @Bean(name = "groupWebServerControlService")
    public GroupWebServerControlService getGroupWebServerControlService(final GroupService groupService, final WebServerControlService webServerControlService) {
        return new GroupWebServerControlServiceImpl(groupService, webServerControlService);
    }

    @Bean(name = "webServerControlService")
    public WebServerControlService getWebServerControlService() {
        return new WebServerControlServiceImpl();
    }

    @Bean(name = "webServerCommandService")
    public WebServerCommandService getWebServerCommandService(final WebServerService webServerService,
                                                              final RemoteCommandExecutorService remoteCommandExecutorService) {
        final SshConfiguration sshConfig = aemSshConfig.getSshConfiguration();

        final JschBuilder jschBuilder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                .setKnownHostsFileName(sshConfig.getKnownHostsFile());

        return new WebServerCommandServiceImpl(
                webServerService,
                sshConfig,
                remoteCommandExecutorService);
    }

    @Bean
    public ApplicationCommandService getApplicationCommandService() {
        return new ApplicationCommandServiceImpl(aemSshConfig.getSshConfiguration(), aemSshConfig.getJschBuilder());
    }

    @Bean(name = "resourceService")
    public ResourceService getResourceService(final ApplicationPersistenceService applicationPersistenceService,
                                              final JvmPersistenceService jvmPersistenceService,
                                              final WebServerPersistenceService webServerPersistenceService,
                                              final ResourceDao resourceDao,
                                              final WebServerResourceHandler webServerResourceHandler,
                                              final ResourceContentGeneratorService resourceContentGeneratorService,
                                              @Qualifier("resourceRepositoryService")
                                              final RepositoryService repositoryService) {
        return new ResourceServiceImpl(aemPersistenceServiceConfiguration.getResourcePersistenceService(),
                aemPersistenceServiceConfiguration.getGroupPersistenceService(), applicationPersistenceService,
                jvmPersistenceService, webServerPersistenceService, resourceDao, webServerResourceHandler,
                resourceContentGeneratorService, binaryDistributionService, new Tika(), repositoryService);
    }

    @Bean
    public ResourceDao getResourceDao() {
        return new ResourceDaoImpl();
    }

    @Bean(name = "httpRequestFactory")
    public HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory(
            @Value("${ping.http.connectionRequestTimeout:60000}") final int connectionRequestTimeout,
            @Value("${ping.http.readTimeout:600000}") final int readTimeout) {
        final HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
        httpRequestFactory.setReadTimeout(readTimeout);
        return httpRequestFactory;
    }

    @Bean(name = "webServerStateRetrievalScheduledTaskHandler")
    public WebServerStateRetrievalScheduledTaskHandler getWebServerStateRetrievalScheduledTaskHandler(
            final WebServerService webServerService, final WebServerStateSetterWorker webServerStateSetterWorker) {
        return new WebServerStateRetrievalScheduledTaskHandler(webServerService, webServerStateSetterWorker, true);
    }

    @Bean(name = "webServerTaskExecutor")
    public TaskExecutor getWebServerTaskExecutor(@Qualifier("pollingThreadFactory") final ThreadFactory threadFactory,
                                                 @Value("${webserver.thread-task-executor.pool.size}") final int corePoolSize,
                                                 @Value("${webserver.thread-task-executor.pool.max-size}") final int maxPoolSize,
                                                 @Value("${webserver.thread-task-executor.pool.queue-capacity}") final int queueCapacity,
                                                 @Value("${webserver.thread-task-executor.pool.keep-alive-sec}") final int keepAliveSeconds) {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setThreadFactory(threadFactory);
        return threadPoolTaskExecutor;
    }

    @Bean(name = "jvmTaskExecutor")
    public TaskExecutor getJvmTaskExecutor(@Qualifier("pollingThreadFactory") final ThreadFactory threadFactory,
                                           @Value("${jvm.thread-task-executor.pool.size}") final int corePoolSize,
                                           @Value("${jvm.thread-task-executor.pool.max-size}") final int maxPoolSize,
                                           @Value("${jvm.thread-task-executor.pool.queue-capacity}") final int queueCapacity,
                                           @Value("${jvm.thread-task-executor.pool.keep-alive-sec}") final int keepAliveSeconds) {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setThreadFactory(threadFactory);
        return threadPoolTaskExecutor;
    }

    @Bean
    public HistoryService getHistoryService(final HistoryCrudService historyCrudService) {
        return new HistoryServiceImpl(historyCrudService);
    }

    @Bean
    public GenericKeyedObjectPool<ChannelSessionKey, Channel> getChannelPool(final AemSshConfig sshConfig) throws JSchException {
        final GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(10);
        genericKeyedObjectPoolConfig.setBlockWhenExhausted(true);
        return new GenericKeyedObjectPool(new KeyedPooledJschChannelFactory(sshConfig.getJschBuilder().build()));
    }

    @Bean
    public JvmStateReceiverAdapter getJvmReceiverAdapter(final JvmStateService jvmStateService,
                                                         final JvmPersistenceService jvmPersistenceService) {
        return new JvmStateReceiverAdapter(jvmStateService, jvmPersistenceService);
    }

    @Bean
    public JGroupsClusterInitializer jGroupsClusterInitializer(final JvmStateReceiverAdapter jvmStateReceiverAdapter) {
        return new JGroupsClusterInitializer(jvmStateReceiverAdapter);
    }

    @Bean(name = "webServerInMemoryStateManagerService")
    InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> getWebServerInMemoryStateManagerService() {
        return new InMemoryStateManagerServiceImpl<>();
    }

    @Bean(name = "jvmInMemoryStateManagerService")
    InMemoryStateManagerService<Identifier<Jvm>, CurrentState<Jvm, JvmState>> getJvmInMemoryStateManagerService() {
        return new InMemoryStateManagerServiceImpl<>();
    }

    @Bean
    public JSch getJSch() {
        return new JSch();
    }

    /**
     * Bean method to create a thread factory that creates daemon threads.
     * <code>
     * <bean id="pollingThreadFactory" class="org.springframework.scheduling.concurrent.CustomizableThreadFactory">
     * <constructor-arg value="polling-"/>
     * </bean></code>
     */
    @Bean(name = "pollingThreadFactory")
    public ThreadFactory getPollingThreadFactory() {
        CustomizableThreadFactory tf = new CustomizableThreadFactory("polling-");
        tf.setDaemon(true);
        return tf;
    }

    @Bean(name = "binaryDistributionControlService")
    public BinaryDistributionControlServiceImpl getBinaryDistributionControlService() {
        return new BinaryDistributionControlServiceImpl();
    }

    @Bean(name = "binaryDistributionService")
    public BinaryDistributionService getBinaryDistributionService(BinaryDistributionControlService binaryDistributionControlService, HistoryFacadeService historyFacadeService) {
        return new BinaryDistributionServiceImpl();
    }

    @Bean
    public ApplicationContextListener getApplicationContextListener() {
        return new ApplicationContextListener();
    }
}