package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;

import java.util.List;

public interface WebServerCrudService extends CrudService<JpaWebServer> {

    WebServer createWebServer(WebServer webServer, String createdBy);

    WebServer updateWebServer(WebServer webServer, String createdBy);

    WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException;

    List<WebServer> getWebServers();

    void removeWebServer(final Identifier<WebServer> aWebServerId);

    List<WebServer> findWebServersBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplications(final String aWebServerName);

    void removeWebServersBelongingTo(final Identifier<Group> aGroupId);

    WebServer findWebServerByName(final String aWebServerName);

    List<Jvm> findJvms(final String aWebServerName);

    List<String> getResourceTemplateNames(final String webServerName);

    String getResourceTemplate(final String webServerName, final String resourceTemplateName);

    JpaWebServerConfigTemplate uploadWebserverConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest);

    void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template);

    int updateState(Identifier<WebServer> id, WebServerReachableState state);

    int updateState(Identifier<WebServer> id, WebServerReachableState state, String errorStatus);

    Long getWebServerStartedCount(String groupName);

    Long getWebServerCount(String groupName);

    JpaWebServer getWebServerAndItsGroups(Long id);

    Long getWebServerStoppedCount(String groupName);

    int removeTemplate(String name);

    @Deprecated
    int removeTemplate(String webServerName, String templateName);

    List<WebServer> getWebServersByGroupName(String groupName);

    String getResourceTemplateMetaData(String webServerName, String resourceTemplateName);

    /**
     * Gets JpaWebServer if webserver exists under a group.
     *
     * @param groupName     name of the group under which webserver should exists
     * @param webServerName name of the webserver to search
     * @return JpaWebServer object if it exists, else returns null
     */
    JpaWebServer findWebServer(String groupName, String webServerName);

    /**
     * Checks if the resource file is present for a webserver
     *
     * @param groupName     name of the group in which the webserver exists
     * @param webServerName name of the webserver we are searching under
     * @param fileName      name of the resource file to be searched
     * @return return true if the resource exists, else returns false
     */
    boolean checkWebServerResourceFileName(String groupName, String webServerName, String fileName);

    void updateResourceMetaData(String webServerName, String resourceName, String metaData);

    Long getWebServerForciblyStoppedCount(String groupName);
}
