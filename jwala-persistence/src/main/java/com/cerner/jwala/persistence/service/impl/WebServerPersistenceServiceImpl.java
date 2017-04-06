package com.cerner.jwala.persistence.service.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;

import java.util.List;

/**
 * {@link WebServerPersistenceService} implementation.
 * <p/>
 * Created by Jedd Cuison on 12/17/2015.
 */
public class WebServerPersistenceServiceImpl implements WebServerPersistenceService {

    private final GroupCrudService groupCrudService;
    private final WebServerCrudService webServerCrudService;

    public WebServerPersistenceServiceImpl(final GroupCrudService groupCrudService,
                                           final WebServerCrudService webServerCrudService) {
        this.groupCrudService = groupCrudService;
        this.webServerCrudService = webServerCrudService;
    }

    @Override
    public WebServer createWebServer(final WebServer webServer, final String createdBy) {
        final WebServer createdWebServer = webServerCrudService.createWebServer(webServer, createdBy);
        groupCrudService.linkWebServer(createdWebServer.getId(), webServer);
        return webServerCrudService.getWebServer(createdWebServer.getId());
    }

    @Override
    public WebServer updateWebServer(final WebServer webServer, final String updatedBy) {
        webServerCrudService.updateWebServer(webServer, updatedBy);
        groupCrudService.linkWebServer(webServer);
        return webServerCrudService.getWebServer(webServer.getId());
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerCrudService.getWebServer(aWebServerId);
    }

    @Override
    public List<WebServer> getWebServers() {
        return webServerCrudService.getWebServers();
    }

    @Override
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {
        webServerCrudService.removeWebServer(aWebServerId);
    }

    @Override
    public List<WebServer> findWebServersBelongingTo(final Identifier<Group> aGroupId) {
        return webServerCrudService.findWebServersBelongingTo(aGroupId);
    }

    @Override
    public List<Application> findApplications(final String aWebServerName) {
        return webServerCrudService.findApplications(aWebServerName);
    }

    @Override
    public WebServer findWebServerByName(final String aWebServerName) {
        return webServerCrudService.findWebServerByName(aWebServerName);
    }

    @Override
    public List<Jvm> findJvms(final String aWebServerName) {
        return webServerCrudService.findJvms(aWebServerName);
    }

    @Override
    public List<String> getResourceTemplateNames(final String webServerName) {
        return webServerCrudService.getResourceTemplateNames(webServerName);
    }

    @Override
    public String getResourceTemplate(final String webServerName, final String resourceTemplateName) {
        return webServerCrudService.getResourceTemplate(webServerName, resourceTemplateName);
    }

    @Override
    public JpaWebServerConfigTemplate uploadWebServerConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest, String absoluteDeployPath, String userId) {

        if (absoluteDeployPath.endsWith("/httpd.conf")) {
            // check for an existing httpd.conf
            WebServer webServer = uploadWebServerTemplateRequest.getWebServer();
            final String webServerName = webServer.getName();
            WebServer updateWebServer = new WebServer(
                    webServer.getId(),
                    webServer.getGroups(),
                    webServerName,
                    webServer.getHost(),
                    webServer.getPort(),
                    webServer.getHttpsPort(),
                    webServer.getStatusPath(),
                    webServer.getState());
            updateWebServer(updateWebServer, userId);
        }

        return webServerCrudService.uploadWebserverConfigTemplate(uploadWebServerTemplateRequest);
    }

    @Override
    public void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        webServerCrudService.updateResourceTemplate(wsName, resourceTemplateName, template);
    }

    @Override
    public String updateResourceMetaData(String webServerName, String resourceName, String metaData) {
        webServerCrudService.updateResourceMetaData(webServerName, resourceName, metaData);
        return webServerCrudService.getResourceTemplateMetaData(webServerName, resourceName);
    }

    @Override
    public int updateState(final Identifier<WebServer> id, final WebServerReachableState state) {
        return webServerCrudService.updateState(id, state);
    }

    @Override
    public int updateState(final Identifier<WebServer> id, final WebServerReachableState state, final String errorStatus) {
        return webServerCrudService.updateState(id, state, errorStatus);
    }

    @Override
    public Long getWebServerStartedCount(final String groupName) {
        return webServerCrudService.getWebServerStartedCount(groupName);
    }

    @Override
    public Long getWebServerCount(final String groupName) {
        return webServerCrudService.getWebServerCount(groupName);
    }

    @Override
    public Long getWebServerStoppedCount(final String groupName) {
        return webServerCrudService.getWebServerStoppedCount(groupName);
    }

    @Override
    public String getResourceTemplateMetaData(String webServerName, String resourceTemplateName) {
        return webServerCrudService.getResourceTemplateMetaData(webServerName, resourceTemplateName);
    }

    @Override
    public List<WebServer> getWebServersByGroupName(String groupName) {
        return webServerCrudService.getWebServersByGroupName(groupName);
    }

    @Override
    public boolean checkWebServerResourceFileName(final String groupName, final String webServerName, final String fileName) {
        return webServerCrudService.checkWebServerResourceFileName(groupName, webServerName, fileName);
    }
}
