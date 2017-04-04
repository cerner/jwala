package com.cerner.jwala.persistence.jpa.service.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import com.cerner.jwala.persistence.jpa.domain.builder.JpaAppBuilder;
import com.cerner.jwala.persistence.jpa.domain.builder.JpaWebServerBuilder;
import com.cerner.jwala.persistence.jpa.domain.builder.JvmBuilder;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaWebServerConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateMetaDataUpdateException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class WebServerCrudServiceImpl extends AbstractCrudServiceImpl<JpaWebServer> implements WebServerCrudService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerCrudServiceImpl.class);

    public WebServerCrudServiceImpl() {
    }

    @Override
    public WebServer createWebServer(final WebServer webServer, final String createdBy) {
        try {
            final JpaWebServer jpaWebServer = new JpaWebServer();

            jpaWebServer.setName(webServer.getName());
            jpaWebServer.setHost(webServer.getHost());
            jpaWebServer.setPort(webServer.getPort());
            jpaWebServer.setHttpsPort(webServer.getHttpsPort());
            jpaWebServer.setStatusPath(webServer.getStatusPath().getPath());
            jpaWebServer.setHttpConfigFile("");
            jpaWebServer.setSvrRoot(webServer.getSvrRoot().getPath());
            jpaWebServer.setDocRoot(webServer.getDocRoot().getPath());
            jpaWebServer.setCreateBy(createdBy);
            jpaWebServer.setState(webServer.getState());

            return webServerFrom(create(jpaWebServer));
        } catch (final EntityExistsException eee) {
            LOGGER.error("Error creating web server {}", webServer, eee);
            throw new EntityExistsException("Web server with name already exists: " + webServer,
                    eee);
        }

    }

    @Override
    public WebServer updateWebServer(final WebServer webServer, final String createdBy) {
        try {
            final JpaWebServer jpaWebServer = findById(webServer.getId().getId());

            jpaWebServer.setName(webServer.getName());
            jpaWebServer.setHost(webServer.getHost());
            jpaWebServer.setPort(webServer.getPort());
            jpaWebServer.setHttpsPort(webServer.getHttpsPort());
            jpaWebServer.setStatusPath(webServer.getStatusPath().getPath());
            jpaWebServer.setSvrRoot(webServer.getSvrRoot().getPath());
            jpaWebServer.setDocRoot(webServer.getDocRoot().getPath());
            jpaWebServer.setCreateBy(createdBy);

            return webServerFrom(update(jpaWebServer));
        } catch (final EntityExistsException eee) {
            LOGGER.error("Error updating web server {}", webServer, eee);
            throw new EntityExistsException("Web Server Name already exists", eee);
        }
    }

    @Override
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) throws NotFoundException {
        return webServerFrom(findById(aWebServerId.getId()));
    }

    @Override
    public List<WebServer> getWebServers() {
        return webServersFrom(findAll());
    }

    @Override
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {
        remove(aWebServerId.getId());
    }

    protected List<WebServer> webServersFrom(final List<JpaWebServer> someJpaWebServers) {

        final List<WebServer> webservers = new ArrayList<>(someJpaWebServers.size());

        for (final JpaWebServer jpaWebServer : someJpaWebServers) {
            webservers.add(webServerFrom(jpaWebServer));
        }

        return webservers;
    }

    protected WebServer webServerFrom(final JpaWebServer aJpaWebServer) {
        return new JpaWebServerBuilder(aJpaWebServer).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {

        final Query query = entityManager.createNamedQuery(JpaWebServer.FIND_WEBSERVERS_BY_GROUPID);
        query.setParameter("groupId", aGroupId.getId());

        final List<JpaWebServer> webservers = query.getResultList();
        for (final JpaWebServer webserver : webservers) {
            remove(webserver);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WebServer> findWebServersBelongingTo(final Identifier<Group> aGroup) {
        final Query query = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUP);
        query.setParameter("groupId", aGroup.getId());
        final JpaGroup group = (JpaGroup) query.getSingleResult();
        return webserversFrom(group.getWebServers());
    }

    protected WebServer webserverFrom(final JpaWebServer aJpaWebServer) {

        final JpaWebServerBuilder builder = new JpaWebServerBuilder(aJpaWebServer);

        return builder.build();
    }

    protected List<WebServer> webserversFrom(final List<JpaWebServer> someJpaWebServers) {

        final List<WebServer> webservers = new ArrayList<>();

        for (final JpaWebServer webserver : someJpaWebServers) {
            if (null != webserver) {
                webservers.add(webserverFrom(webserver));
            }
        }

        return webservers;
    }

    @Override
    public List<Application> findApplications(final String aWebServerName) {
        Query q = entityManager.createNamedQuery(JpaWebServer.FIND_WEB_SERVER_BY_QUERY);
        q.setParameter(JpaApplication.WEB_SERVER_NAME_PARAM, aWebServerName);
        final JpaWebServer webServer = (JpaWebServer) q.getSingleResult();

        q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_WEB_SERVER_NAME);
        q.setParameter(JpaApplication.GROUP_LIST_PARAM, webServer.getGroups());

        final List<Application> apps = new ArrayList<>();
        for (final JpaApplication jpa : (List<JpaApplication>) q.getResultList()) {
            apps.add(JpaAppBuilder.appFrom(jpa));
        }
        return apps;
    }

    @Override
    public WebServer findWebServerByName(final String aWebServerName) {
        final Query q = entityManager.createNamedQuery(JpaWebServer.FIND_WEB_SERVER_BY_QUERY);
        q.setParameter(JpaWebServer.WEB_SERVER_PARAM_NAME, aWebServerName);

        return webServerFrom((JpaWebServer) q.getSingleResult());
    }

    @Override
    public List<Jvm> findJvms(final String aWebServerName) {
        Query q = entityManager.createNamedQuery(JpaWebServer.FIND_WEB_SERVER_BY_QUERY);
        q.setParameter(JpaApplication.WEB_SERVER_NAME_PARAM, aWebServerName);
        final JpaWebServer webServer = (JpaWebServer) q.getSingleResult();
        q = entityManager.createNamedQuery(JpaWebServer.FIND_JVMS_QUERY);
        q.setParameter("groups", webServer.getGroups());

        final List<Jvm> jvms = new ArrayList<>(q.getResultList().size());
        for (final JpaJvm jpaJvm : (List<JpaJvm>) q.getResultList()) {
            jvms.add(new JvmBuilder(jpaJvm).build());
        }
        return jvms;
    }

    @Override
    public List<String> getResourceTemplateNames(final String webServerName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_RESOURCE_TEMPLATE_NAMES);
        q.setParameter("webServerName", webServerName);
        return q.getResultList();
    }

    @Override
    public String getResourceTemplate(final String webServerName, final String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("webServerName", webServerName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            LOGGER.error("Error getting resource template {} for web server {}", resourceTemplateName, webServerName, e);
            throw new NonRetrievableResourceTemplateContentException(webServerName, resourceTemplateName, e);
        }
    }

    @Override
    public String getResourceTemplateMetaData(String webServerName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_METADATA);
        q.setParameter("webServerName", webServerName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            LOGGER.error("Error getting resource meta data {} for web server {}", resourceTemplateName, webServerName, e);
            throw new NonRetrievableResourceTemplateContentException(webServerName, resourceTemplateName, e);
        }
    }

    @Override
    public JpaWebServerConfigTemplate uploadWebserverConfigTemplate(UploadWebServerTemplateRequest uploadWebServerTemplateRequest) {
        return uploadWebServerTemplate(uploadWebServerTemplateRequest);
    }
    @Transactional
    private JpaWebServerConfigTemplate uploadWebServerTemplate(UploadWebServerTemplateRequest request) {
        final WebServer webServer = request.getWebServer();
        Identifier<WebServer> id = webServer.getId();
        final JpaWebServer jpaWebServer = findById(id.getId());

        String templateContent = request.getTemplateContent();

        // get an instance and then do a create or update
        Query query = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE);
        query.setParameter("webServerName", webServer.getName());
        query.setParameter("templateName", request.getConfFileName());
        List<JpaWebServerConfigTemplate> templates = query.getResultList();
        JpaWebServerConfigTemplate jpaConfigTemplate;
        final String metaData = request.getMetaData();
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            //create
            jpaConfigTemplate = new JpaWebServerConfigTemplate();
            jpaConfigTemplate.setWebServer(jpaWebServer);
            jpaConfigTemplate.setTemplateName(request.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            LOGGER.error("Error uploading web server template for request {}", request);
            throw new BadRequestException(FaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for web server [" + request + "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;
    }

    @Override
    public void updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.UPDATE_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("webServerName", wsName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);

        int numEntities;
        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating resource template {} for web server {}", resourceTemplateName, wsName, re);
            throw new ResourceTemplateUpdateException(wsName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating resource template numEntities=0 {} for web server {}", resourceTemplateName, wsName);
            throw new ResourceTemplateUpdateException(wsName, resourceTemplateName);
        }
    }

    @Override
    public void updateResourceMetaData(String webServerName, String resourceName, String metaData) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.UPDATE_WEBSERVER_TEMPLATE_META_DATA);
        q.setParameter("webServerName", webServerName);
        q.setParameter("templateName", resourceName);
        q.setParameter("metaData", metaData);

        int numEntities;
        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating resource meta data {} for web server {}", resourceName, webServerName, re);
            throw new ResourceTemplateMetaDataUpdateException(webServerName, resourceName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating resource meta data numEntities=0 {} for web server {}", resourceName, webServerName);
            throw new ResourceTemplateMetaDataUpdateException(webServerName, resourceName);
        }
    }

    @Override
    public int updateState(final Identifier<WebServer> id, final WebServerReachableState state) {
        // Normally we would load the JpaWebServer then set the states but I reckon running an UPDATE query would be faster since
        // it's only one transaction vs 2 (find and update).
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_UPDATE_STATE_BY_ID);
        query.setParameter(JpaWebServer.QUERY_PARAM_STATE, state);
        query.setParameter(JpaWebServer.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public int updateErrorStatus(final Identifier<WebServer> id, final String errorStatus) {
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_UPDATE_ERROR_STATUS_BY_ID);
        query.setParameter(JpaWebServer.QUERY_PARAM_ERROR_STATUS, errorStatus);
        query.setParameter(JpaWebServer.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public int updateState(final Identifier<WebServer> id, final WebServerReachableState state, final String errorStatus) {
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID);
        query.setParameter(JpaWebServer.QUERY_PARAM_STATE, state);
        query.setParameter(JpaWebServer.QUERY_PARAM_ERROR_STATUS, errorStatus);
        query.setParameter(JpaWebServer.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public Long getWebServerStartedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_GET_WS_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaWebServer.QUERY_PARAM_STATE, WebServerReachableState.WS_REACHABLE);
        query.setParameter(JpaWebServer.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getWebServerCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_GET_WS_COUNT_BY_GROUP_NAME);
        query.setParameter(JpaWebServer.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public JpaWebServer getWebServerAndItsGroups(final Long id) {
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_GET_WS_AND_ITS_GROUPS);
        query.setParameter("id", id);
        return (JpaWebServer) query.getSingleResult();
    }

    @Override
    public Long getWebServerStoppedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaWebServer.QUERY_GET_WS_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaWebServer.QUERY_PARAM_STATE, WebServerReachableState.WS_UNREACHABLE);
        query.setParameter(JpaWebServer.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public int removeTemplate(final String templateName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.QUERY_DELETE_WEB_SERVER_TEMPLATE);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return q.executeUpdate();
    }

    @Override
    public int removeTemplate(final String webServerName, final String templateName) {
        final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.QUERY_DELETE_WEBSERVER_RESOURCE_BY_TEMPLATE_WEBSERVER_NAME);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return q.executeUpdate();
    }

    @Override
    public List<WebServer> getWebServersByGroupName(final String groupName) {
        final Query q = entityManager.createNamedQuery(JpaWebServer.QUERY_GET_WS_BY_GROUP_NAME);
        q.setParameter(JpaWebServer.QUERY_PARAM_GROUP_NAME, groupName);
        return buildWebServers(q.getResultList());
    }

    /**
     * Builds a list of Web Servers.
     *
     * @param jpaWebServers {@link JpaWebServer}
     * @return A list of web servers. Returns an empty list if there are no web servers.
     */
    private List<WebServer> buildWebServers(List<JpaWebServer> jpaWebServers) {
        List<WebServer> webServers = new ArrayList<>();
        for (JpaWebServer jpaWebServer : jpaWebServers) {
            webServers.add(new JpaWebServerBuilder(jpaWebServer).build());
        }
        return webServers;
    }

    @Override
    public JpaWebServer findWebServer(String groupName, String webServerName) {
        JpaWebServer jpaWebServer = null;
        final Query q = entityManager.createNamedQuery(JpaWebServer.FIND_WEBSERVER_BY_GROUP_QUERY);
        q.setParameter(JpaWebServer.WEB_SERVER_PARAM_NAME, webServerName);
        q.setParameter(JpaWebServer.QUERY_PARAM_GROUP_NAME, groupName);
        try {
            jpaWebServer = (JpaWebServer) q.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.error("error with getting data for webserverName: {} under group: {}, error: {}", webServerName, groupName, e);
        }
        return jpaWebServer;
    }

    @Override
    public boolean checkWebServerResourceFileName(String groupName, String webServerName, String fileName) {
        final JpaWebServer jpaWebServer = findWebServer(groupName, webServerName);
        if (jpaWebServer != null) {
            final Query q = entityManager.createNamedQuery(JpaWebServerConfigTemplate.GET_WEBSERVER_TEMPLATE_RESOURCE_NAME);
            q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
            q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, fileName);
            final List<String> result = q.getResultList();
            if (result != null && result.size() == 1) {
                return true;
            }
        }
        return false;
    }
}
