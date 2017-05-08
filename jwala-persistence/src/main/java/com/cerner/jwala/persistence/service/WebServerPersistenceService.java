package com.cerner.jwala.persistence.service;

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

/**
 * Web server persistence service.
 *
 * Created by Jedd Cuison on 12/17/2015.
 */
public interface WebServerPersistenceService {

    WebServer createWebServer(WebServer webServer, String createdBy);

    WebServer updateWebServer(WebServer webServer, String updatedBy);

    WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException;

    List<WebServer> getWebServers();

    void removeWebServer(final Identifier<WebServer> aWebServerId);

    List<WebServer> findWebServersBelongingTo(Identifier<Group> aGroupId);

    List<Application> findApplications(final String aWebServerName);

    WebServer findWebServerByName(final String aWebServerName);

    List<Jvm> findJvms(final String aWebServerName);

    List<String> getResourceTemplateNames(final String webServerName);

    String getResourceTemplate(final String webServerName, final String resourceTemplateName);

    JpaWebServerConfigTemplate uploadWebServerConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest, String absoluteDeployPath, String userId);

    void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template);

    int updateState(Identifier<WebServer> id, WebServerReachableState state);

    int updateState(Identifier<WebServer> id, WebServerReachableState state, String errorStatus);

    Long getWebServerStartedCount(String groupName);

    Long getWebServerCount(String groupName);

    Long getWebServerStoppedCount(String groupName);

    List<WebServer> getWebServersByGroupName(String groupName);

    String getResourceTemplateMetaData(String webServerName, String resourceTemplateName);

    /**
     *
     * @param groupName
     * @param webServerName
     * @param fileName
     * @return
     */
    boolean checkWebServerResourceFileName(String groupName, String webServerName, String fileName);

    String updateResourceMetaData(String webServerName, String resourceName, String metaData);

    /*** methods that uses JPA entities ***/

    /**
     * Creates a web server
     * @param jpaWebServer the JPA web server entity
     * @return the new web server
     */
    JpaWebServer createWebServer(JpaWebServer jpaWebServer);

    /**
     * Updates a web server
     * @param jpaWebServer the JPA web server entity
     * @return the update web server
     */
    JpaWebServer updateWebServer(JpaWebServer jpaWebServer);

    /**
     * Find a web server
     * @param id
     * @return web server entity
     */
    JpaWebServer findWebServer(Long id);

}
