package com.cerner.jwala.ws.rest.v1.configuration;

import com.cerner.jwala.files.FilesConfiguration;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.group.GroupControlService;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerControlService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.ws.rest.RestServiceErrorHandler;
import com.cerner.jwala.ws.rest.v1.exceptionmapper.*;
import com.cerner.jwala.ws.rest.v1.service.impl.HistoryServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.response.ApplicationResponse;
import com.cerner.jwala.ws.rest.v1.response.ResponseMessageBodyWriter;
import com.cerner.jwala.ws.rest.v1.service.HistoryServiceRest;
import com.cerner.jwala.ws.rest.v1.service.admin.AdminServiceRest;
import com.cerner.jwala.ws.rest.v1.service.admin.impl.AdminServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.app.ApplicationServiceRest;
import com.cerner.jwala.ws.rest.v1.service.app.impl.ApplicationServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.BalancerManagerServiceRest;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.impl.BalancerManagerServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.group.GroupServiceRest;
import com.cerner.jwala.ws.rest.v1.service.group.impl.GroupServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.jvm.JvmServiceRest;
import com.cerner.jwala.ws.rest.v1.service.jvm.impl.JvmServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.media.MediaServiceRest;
import com.cerner.jwala.ws.rest.v1.service.resource.ResourceServiceRest;
import com.cerner.jwala.ws.rest.v1.service.resource.impl.ResourceServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.springboot.SpringBootServiceRest;
import com.cerner.jwala.ws.rest.v1.service.user.UserServiceRest;
import com.cerner.jwala.ws.rest.v1.service.user.impl.UserServiceRestImpl;
import com.cerner.jwala.ws.rest.v1.service.webserver.WebServerServiceRest;
import com.cerner.jwala.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ext.MessageBodyWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Configuration
@ComponentScan({"com.cerner.jwala.ws.rest.v1.service"})
public class AemWebServiceConfiguration {

    @Autowired
    private MediaServiceRest mediaServiceRest;

    @Autowired
    private SpringBootServiceRest springBootRestService;

    @Autowired
    private FilesConfiguration filesConfiguration;

    @Autowired
    private GroupService groupService;

    @Autowired
    private JvmService jvmService;

    @Autowired
    private WebServerService webServerService;

    @Autowired
    private JvmControlService jvmControlService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private WebServerControlService webServerControlService;

    @Autowired
    private WebServerCommandService webServerCommandService;

    @Autowired
    private ResourceService resourceService;


    @Autowired
    private HistoryService historyService;

    @Autowired
    private HistoryFacadeService historyFacadeService;

    @Autowired
    private GroupControlService groupControlService;

    @Autowired
    private GroupJvmControlService groupJvmControlService;

    @Autowired
    private GroupWebServerControlService groupWebServerControlService;


    @Autowired
    private BalancerManagerService balancerManagerService;

    @Autowired
    private BinaryDistributionService binaryDistributionService;

    private final Map<String, ReentrantReadWriteLock> wsWriteLockMap = new ConcurrentHashMap<>();

    @Bean
    public Server getV1JaxResServer() {
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress("/v1.0");
        factory.setServiceBeans(getV1ServiceBeans());
        factory.setProviders(getV1Providers());
        return factory.create();
    }

    @Bean
    public List<Object> getV1ServiceBeans() {
        final List<Object> serviceBeans = new ArrayList<>();

        serviceBeans.add(getV1GroupServiceRest());
        serviceBeans.add(getV1JvmServiceRest());
        serviceBeans.add(mediaServiceRest);
        serviceBeans.add(springBootRestService);
        serviceBeans.add(getV1WebServerServiceRest());
        serviceBeans.add(getV1ApplicationServiceRest());
        serviceBeans.add(getV1UserServiceRest());
        serviceBeans.add(getV1AdminServiceRest());
        serviceBeans.add(getV1ResourceServiceRest());
        serviceBeans.add(getV1HistoryServiceRest());
        serviceBeans.add(getV1BalancermanagerServiceRest());

        return serviceBeans;
    }

    @Bean
    public AdminServiceRest getV1AdminServiceRest() {
        return new AdminServiceRestImpl(filesConfiguration, resourceService);
    }

    @Bean
    public UserServiceRest getV1UserServiceRest() {
        return new UserServiceRestImpl();
    }

    @Bean
    public GroupServiceRest getV1GroupServiceRest() {
        return new GroupServiceRestImpl(groupService, resourceService, groupControlService, groupJvmControlService,
                groupWebServerControlService, jvmService, webServerService, applicationService, getV1ApplicationServiceRest(),
                getV1WebServerServiceRest());
    }

    @Bean
    public BalancerManagerServiceRest getV1BalancermanagerServiceRest() {
        return new BalancerManagerServiceRestImpl(balancerManagerService);
    }

    @Bean
    public JvmServiceRest getV1JvmServiceRest() {
        return new JvmServiceRestImpl(
                jvmService,
                jvmControlService
        );
    }

    @Bean
    public WebServerServiceRest getV1WebServerServiceRest() {
        return new WebServerServiceRestImpl(webServerService,
                webServerControlService,
                webServerCommandService,
                resourceService,
                groupService,
                binaryDistributionService,
                historyFacadeService);
    }

    @Bean
    @Autowired
    public HistoryServiceRest getV1HistoryServiceRest() {
        return new HistoryServiceRestImpl(historyService);
    }

    @Bean
    public ApplicationServiceRest getV1ApplicationServiceRest() {
        return new ApplicationServiceRestImpl(applicationService, resourceService, groupService);
    }

    @Bean
    public ServletFileUpload getServletFileUpload() {
        return new ServletFileUpload();
    }

    @Bean
    public ResourceServiceRest getV1ResourceServiceRest() {
        return new ResourceServiceRestImpl(resourceService);
    }

    @Bean
    public List<?> getV1Providers() {
        final List<? super Object> providers = new ArrayList<>();

        providers.add(getV1FormUploadProvider());
        providers.add(getV1JsonProvider());

        providers.add(getV1NotFoundExceptionMapper());
        providers.add(getV1BadRequestExceptionMapper());
        providers.add(getV1InternalErrorExceptionMapper());
        providers.add(getV1ExternalSystemErrorExceptionMapper());
        providers.add(getV1TransactionRequiredExceptionMapper());

        providers.add(getInternalServerErrorHandler());

        return providers;
    }

    @Bean
    public MessageBodyWriter<ApplicationResponse> getV1FormUploadProvider() {
        return new ResponseMessageBodyWriter();
    }

    @Bean
    public JacksonJsonProvider getV1JsonProvider() {
        return new JacksonJsonProvider();
    }

    @Bean
    public NotFoundExceptionMapper getV1NotFoundExceptionMapper() {
        return new NotFoundExceptionMapper();
    }

    @Bean
    public BadRequestExceptionMapper getV1BadRequestExceptionMapper() {
        return new BadRequestExceptionMapper();
    }

    @Bean
    public InternalErrorExceptionMapper getV1InternalErrorExceptionMapper() {
        return new InternalErrorExceptionMapper();
    }

    @Bean
    public ExternalSystemErrorExceptionMapper getV1ExternalSystemErrorExceptionMapper() {
        return new ExternalSystemErrorExceptionMapper();
    }

    @Bean
    public TransactionRequiredExceptionMapper getV1TransactionRequiredExceptionMapper() {
        return new TransactionRequiredExceptionMapper();
    }

    @Bean(destroyMethod = "shutdownNow")
    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(12);
    } // TODO: why 12? is this configurable with a property?

    @Bean
    public RestServiceErrorHandler getInternalServerErrorHandler() {
        return new RestServiceErrorHandler();
    }

}
