package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.template.ResourceFileGenerator;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Implement {@link ResourceContentGeneratorService}
 * <p>
 * Created by Jedd Cuison on 7/26/2016.
 */
@Service
public class ResourceContentGeneratorServiceImpl implements ResourceContentGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceContentGeneratorServiceImpl.class);
    private final GroupPersistenceService groupPersistenceService;
    private final WebServerPersistenceService webServerPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;
    private final ApplicationPersistenceService applicationPersistenceService;
    private final HistoryFacadeService historyFacadeService;

    @Autowired
    public ResourceContentGeneratorServiceImpl(final GroupPersistenceService groupPersistenceService,
                                               final WebServerPersistenceService webServerPersistenceService,
                                               final JvmPersistenceService jvmPersistenceService,
                                               final ApplicationPersistenceService applicationPersistenceService,
                                               final HistoryFacadeService historyFacadeService) {
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.historyFacadeService = historyFacadeService;
    }

    @Override
    public <T> String generateContent(final String fileName, final String template, final ResourceGroup resourceGroup, final T entity, ResourceGeneratorType resourceGeneratorType) {
        try {
            return ResourceFileGenerator.generateResourceConfig(fileName, template, null == resourceGroup ? generateResourceGroup() : resourceGroup, entity);
        } catch (ResourceFileGeneratorException e) {
            final String logMessage = resourceGeneratorType.name() + ": " + e.getMessage();
            LOGGER.error(logMessage, e);
            final String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!resourceGeneratorType.equals(ResourceGeneratorType.PREVIEW)) {
                if (entity instanceof WebServer) {
                    WebServer webServer = (WebServer) entity;
                    historyFacadeService.write("Web Server " + webServer.getName(), new ArrayList<>(webServer.getGroups()), logMessage, EventType.SYSTEM_ERROR, userName);
                } else if (entity instanceof Jvm) {
                    Jvm jvm = (Jvm) entity;
                    historyFacadeService.write("JVM " + jvm.getJvmName(), new ArrayList<>(jvm.getGroups()), logMessage, EventType.SYSTEM_ERROR, userName);
                } else {
                    Application application = (Application) entity;
                    ArrayList<Group> groups = new ArrayList<>();
                    groups.add(application.getGroup());
                    historyFacadeService.write("App " + application.getName(), groups, logMessage, EventType.SYSTEM_ERROR, userName);
                }
            }
            throw new ResourceFileGeneratorException(logMessage, e);
        }
    }

    /**
     * Create pertinent data to pass to the template generator engine
     *
     * @return {@link ResourceGroup}
     */
    private ResourceGroup generateResourceGroup() {
        final List<Group> groups = groupPersistenceService.getGroups();
        List<Group> groupsToBeAdded = null;

        for (Group group : groups) {
            if (groupsToBeAdded == null) {
                groupsToBeAdded = new ArrayList<>(groups.size());
            }
            final List<Jvm> jvms = jvmPersistenceService.getJvmsByGroupName(group.getName());
            final List<WebServer> webServers = webServerPersistenceService.getWebServersByGroupName(group.getName());
            final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(group.getName());
            groupsToBeAdded.add(new Group(group.getId(),
                    group.getName(),
                    null != jvms ? new LinkedHashSet<>(jvms) : new LinkedHashSet<Jvm>(),
                    null != webServers ? new LinkedHashSet<>(webServers) : new LinkedHashSet<WebServer>(),
                    group.getHistory(),
                    null != applications ? new LinkedHashSet<>(applications) : new LinkedHashSet<Application>()));
        }
        return new ResourceGroup(groupsToBeAdded);
    }
}
