package com.cerner.jwala.persistence.jpa.service.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.common.request.group.UpdateGroupRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaApplication;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaGroupAppConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaGroupJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaGroupWebServerConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateMetaDataUpdateException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityExistsException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class GroupCrudServiceImpl extends AbstractCrudServiceImpl<JpaGroup> implements GroupCrudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupCrudServiceImpl.class);

    public GroupCrudServiceImpl() {
    }

    @Override
    public JpaGroup createGroup(CreateGroupRequest createGroupRequest) {
        final JpaGroup jpaGroup = new JpaGroup();
        jpaGroup.setName(createGroupRequest.getGroupName());

        try {
            return create(jpaGroup);
        } catch (final EntityExistsException eee) {
            LOGGER.error("Error creating group {}", createGroupRequest, eee);
            throw new EntityExistsException("Group Name already exists: " + createGroupRequest,
                    eee);
        }
    }

    @Override
    public void updateGroup(UpdateGroupRequest updateGroupRequest) {

        final JpaGroup jpaGroup = getGroup(updateGroupRequest.getId());

        jpaGroup.setName(updateGroupRequest.getNewName());

        try {
            update(jpaGroup);
        } catch (final EntityExistsException eee) {
            LOGGER.error("Error updating group {}", updateGroupRequest, eee);
            throw new EntityExistsException("Group Name already exists: " + updateGroupRequest,
                    eee);
        }
    }

    @Override
    public JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        return findById(aGroupId.getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public JpaGroup getGroup(final String name) throws NotFoundException {
        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE lower(g.name) = lower(:groupName)");
        query.setParameter("groupName", name);
        List<JpaGroup> jpaGroups = query.getResultList();
        if (jpaGroups == null || jpaGroups.isEmpty()) {
            LOGGER.error("Error getting the group {}", name);
            throw new NotFoundException(FaultType.GROUP_NOT_FOUND, "Group not found: " + name);
        }
        return jpaGroups.get(0);
    }

    @Override
    public List<JpaGroup> getGroups() {
        return findAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaGroup> findGroups(final String aName) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name=:groupName");
        query.setParameter("groupName", aName);

        return query.getResultList();
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {
        final JpaGroup group = getGroup(aGroupId);
        remove(group);
    }

    @Override
    public Long getGroupId(final String name) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUP_ID);
        q.setParameter("name", name);
        return (Long) q.getSingleResult();
    }

    @Override
    public void linkWebServer(final WebServer webServer) {
        linkWebServer(webServer.getId(), webServer);
    }

    @Override
    public void linkWebServer(final Identifier<WebServer> id, final WebServer webServer) {
        final JpaWebServer jpaWebServer = entityManager.find(JpaWebServer.class, id.getId());
        final List<JpaGroup> jpaGroups = getGroupsWithWebServer(jpaWebServer);

        // Unlink web server from all the groups.
        for (JpaGroup jpaGroup : jpaGroups) {
            jpaGroup.getWebServers().remove(jpaWebServer);
        }

        // Link web server's newly defined groups.
        List<JpaGroup> linkedGroups = new ArrayList<>();
        for (Group group : webServer.getGroups()) {
            final JpaGroup jpaGroup = getGroup(group.getId());
            jpaGroup.getWebServers().add(jpaWebServer);
            linkedGroups.add(jpaGroup);
        }
        jpaWebServer.setGroups(linkedGroups);
        entityManager.persist(jpaWebServer);

        entityManager.flush();
    }

    @SuppressWarnings("unchecked")
    private List<JpaGroup> getGroupsWithWebServer(final JpaWebServer jpaWebServer) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUPS_WITH_WEBSERVER);
        q.setParameter("webServer", jpaWebServer);
        return q.getResultList();
    }

    @Override
    public void uploadGroupJvmTemplate(UploadJvmTemplateRequest uploadJvmTemplateRequest, JpaGroup group) {
        String templateContent = uploadJvmTemplateRequest.getTemplateContent();

        Query query = entityManager.createQuery("SELECT t FROM JpaGroupJvmConfigTemplate t where t.templateName = :tempName and t.grp.name = :grpName");
        query.setParameter("grpName", group.getName());
        query.setParameter("tempName", uploadJvmTemplateRequest.getConfFileName());
        List<JpaGroupJvmConfigTemplate> templates = query.getResultList();
        JpaGroupJvmConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(uploadJvmTemplateRequest.getMetaData());
            entityManager.flush();
        } else if (templates.isEmpty()) {
            jpaConfigTemplate = new JpaGroupJvmConfigTemplate();
            jpaConfigTemplate.setJpaGroup(group);
            jpaConfigTemplate.setTemplateName(uploadJvmTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(uploadJvmTemplateRequest.getMetaData());
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        }
    }

    @Override
    public void uploadGroupWebServerTemplate(UploadWebServerTemplateRequest uploadWSTemplateRequest, JpaGroup group) {
        String templateContent = uploadWSTemplateRequest.getTemplateContent();

        Query query = entityManager.createQuery("SELECT t FROM JpaGroupWebServerConfigTemplate t where t.templateName = :tempName and t.grp.name = :grpName");
        query.setParameter("grpName", group.getName());
        query.setParameter("tempName", uploadWSTemplateRequest.getConfFileName());
        List<JpaGroupWebServerConfigTemplate> templates = query.getResultList();
        JpaGroupWebServerConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(uploadWSTemplateRequest.getMetaData());
            entityManager.flush();
        } else if (templates.isEmpty()) {
            jpaConfigTemplate = new JpaGroupWebServerConfigTemplate();
            jpaConfigTemplate.setJpaGroup(group);
            jpaConfigTemplate.setTemplateName(uploadWSTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(uploadWSTemplateRequest.getMetaData());
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        }
    }

    @Override
    public List<String> getGroupJvmsResourceTemplateNames(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_RESOURCE_NAMES);
        query.setParameter("grpName", groupName);
        return query.getResultList();
    }

    @Override
    public List<String> getGroupWebServersResourceTemplateNames(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAMES);
        query.setParameter("grpName", groupName);
        return query.getResultList();
    }

    @Override
    public void updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.UPDATE_GROUP_APP_TEMPLATE_CONTENT);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceTemplateName);
        q.setParameter("templateContent", content);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error update the group app resource template {} for {} {}", resourceTemplateName, groupName, appName, re);
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating group app template: numEntities=0 {} {} {}", resourceTemplateName, groupName, appName);
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName);
        }
    }

    @Override
    public void updateGroupAppResourceMetaData(String groupName, String webAppName, String resourceName, String metaData) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.UPDATE_GROUP_APP_TEMPLATE_META_DATA);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, webAppName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter("metaData", metaData);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating group app resource meta data for resource {} in group {} app {}", resourceName, groupName, webAppName, re);
            throw new ResourceTemplateMetaDataUpdateException(groupName, resourceName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating group app resource meta data numEntities=0 for resource {} in group {} app {}", resourceName, groupName, webAppName);
            throw new ResourceTemplateMetaDataUpdateException(groupName, resourceName);
        }
    }

    @Override
    public String getGroupAppResourceTemplateMetaDataWithAppname(String groupName, String templateName, String appName) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_META_DATA_WITH_APP);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", templateName);
        q.setParameter("appName", appName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting group app resource meta data for resource {} in group {}", templateName, groupName, re);
            throw new NonRetrievableResourceTemplateContentException(groupName, templateName, re);
        }
    }

    @Override
    public String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_CONTENT);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting group app resource template {} in group {} for app {}", resourceTemplateName, groupName, appName, re);
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }

    @Override
    public List<String> getGroupAppsResourceTemplateNames(String groupName) {
        final Query query = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES);
        query.setParameter("grpName", groupName);
        return query.getResultList();
    }

    @Override
    public List<String> getGroupAppsResourceTemplateNames(String groupName, String appName) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_APP_RESOURCE_NAMES);
        q.setParameter("grpName", groupName);
        q.setParameter("appName", appName);
        return q.getResultList();
    }

    @Override
    public void updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.UPDATE_GROUP_JVM_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", content);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating group JVM template {} in group {}", resourceTemplateName, groupName, re);
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating group JVM template numEntities=0 {} in group {}", resourceTemplateName, groupName);
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName);
        }
    }

    @Override
    public void updateGroupJvmResourceMetaData(String groupName, String resourceName, String metaData) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.UPDATE_GROUP_JVM_TEMPLATE_META_DATA);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceName);
        q.setParameter("metaData", metaData);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating group JVM resource meta data {} in group {}", resourceName, groupName, re);
            throw new ResourceTemplateMetaDataUpdateException(groupName, resourceName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating group JVM resource meta data numEntities==0 {} in group {}", resourceName, groupName);
            throw new ResourceTemplateMetaDataUpdateException(groupName, resourceName);
        }
    }

    @Override
    public String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting group JVM resource {} in group {}", resourceTemplateName, groupName, re);
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }

    @Override
    public String getGroupJvmResourceTemplateMetaData(String groupName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_META_DATA);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting group JVM resource meta data {} in group {}", resourceTemplateName, groupName, re);
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }

    @Override
    public void updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.UPDATE_GROUP_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", content);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating group web server resource {} in group {}", resourceTemplateName, groupName, re);
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating group web server resource numEntities=0 {} in group {}", resourceTemplateName, groupName);
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName);
        }
    }

    @Override
    public void updateGroupWebServerResourceMetaData(String groupName, String resourceName, String metaData) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.UPDATE_GROUP_WEBSERVER_TEMPLATE_META_DATA);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceName);
        q.setParameter("metaData", metaData);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating group web server resource meta data {} in group {}", resourceName, groupName, re);
            throw new ResourceTemplateMetaDataUpdateException(groupName, resourceName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating group web server resource meta data numEntities=0 {} in group {}", resourceName, groupName);
            throw new ResourceTemplateMetaDataUpdateException(groupName, resourceName);
        }

    }

    @Override
    public String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting group web server resource template {} in group {}", resourceTemplateName, groupName, re);
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }

    @Override
    public String getGroupWebServerResourceTemplateMetaData(String groupName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_META_DATA);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting group web server resource meta data {} in group {}", resourceTemplateName, groupName, re);
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }

    @Override
    public ConfigTemplate populateGroupAppTemplate(final String groupName, String appName, final String templateFileName, final String metaData,
                                                   final String templateContent) {
        Query query = entityManager.createQuery("SELECT t FROM JpaGroupAppConfigTemplate t where t.templateName = :tempName and t.grp.name = :grpName and t.app.name = :appName");
        query.setParameter("grpName", groupName);
        query.setParameter("tempName", templateFileName);
        query.setParameter("appName", appName);
        List<JpaGroupAppConfigTemplate> templates = query.getResultList();
        JpaGroupAppConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            jpaConfigTemplate = new JpaGroupAppConfigTemplate();
            jpaConfigTemplate.setJpaGroup(getGroup(groupName));
            jpaConfigTemplate.setTemplateName(templateFileName);
            jpaConfigTemplate.setMetaData(metaData);
            jpaConfigTemplate.setTemplateContent(templateContent);

            query = entityManager.createNamedQuery(JpaApplication.QUERY_BY_NAME);
            query.setParameter(JpaApplication.APP_NAME_PARAM, appName);
            jpaConfigTemplate.setApp((JpaApplication) query.getSingleResult());

            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            LOGGER.error("Error popuklating group app template {} for app {} in group {}", templateFileName, appName, groupName);
            throw new BadRequestException(FaultType.APP_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for GROUP APP Template [" + groupName + "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;
    }

    @Override
    public boolean checkGroupJvmResourceFileName(final String groupName, final String fileName) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_RESOURCE_NAME);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, fileName);
        final List<String> result = q.getResultList();
        if (result != null && result.size() == 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkGroupWebServerResourceFileName(String groupName, String fileName) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAME);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, fileName);
        final List<String> result = q.getResultList();
        if (result != null && result.size() == 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean checkGroupAppResourceFileName(String groupName, String fileName) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_RESOURCE_NAME);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, fileName);
        final List<String> result = q.getResultList();
        if (result != null && result.size() == 1) {
            return true;
        }
        return false;
    }
}

