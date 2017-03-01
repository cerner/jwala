package com.cerner.jwala.persistence.service.impl;

import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.*;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.cerner.jwala.persistence.service.ResourceDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Implements {@link ResourceDao}
 *
 * Created by Jedd Cuison on 6/3/2016.
 */
public class ResourceDaoImpl implements ResourceDao {

    @PersistenceContext(unitName = "jwala-unit")
    private EntityManager em;

    @Override
    public int deleteWebServerResource(final String templateName, final String webServerName) {
        final Query q = em.createNamedQuery(JpaWebServerConfigTemplate.QUERY_DELETE_WEBSERVER_RESOURCE_BY_TEMPLATE_WEBSERVER_NAME);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelWebServerResource(final String templateName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupWebServerConfigTemplate.QUERY_DELETE_GROUP_LEVEL_WEBSERVER_RESOURCE_BY_TEMPLATE_GROUP_NAME);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteJvmResource(final String templateName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCE_BY_TEMPLATE_JVM_NAME);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelJvmResource(final String templateName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCE_BY_TEMPLATE_GROUP_NAME);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteAppResource(final String templateName, final String appName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplicationConfigTemplate.QUERY_DELETE_APP_RESOURCE_BY_TEMPLATE_APP_JVM_NAME);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelAppResource(String appName, final String groupName, final String templateName) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_DELETE_GROUP_LEVEL_APP_RESOURCE_BY_APP_GROUP_TEMPLATE_NAME);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return q.executeUpdate();
    }

    @Override
    public int deleteWebServerResources(final List<String> templateNameList, final String webServerName) {
        final Query q = em.createNamedQuery(JpaWebServerConfigTemplate
                .QUERY_DELETE_WEBSERVER_RESOURCES_BY_TEMPLATE_NAME_LIST_WEBSERVER_NAME);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelWebServerResources(final List<String> templateNameList, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupWebServerConfigTemplate
                .QUERY_DELETE_GROUP_LEVEL_WEBSERVER_RESOURCES_BY_TEMPLATE_NAME_LIST_GROUP_NAME);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteJvmResources(final List<String> templateNameList, final String jvmName) {
        final Query q = em.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_JVM_NAME);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelJvmResources(final List<String> templateNameList, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_GROUP_NAME);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteAppResources(final List<String> templateNameList, final String appName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplicationConfigTemplate.QUERY_DELETE_APP_RESOURCES_BY_TEMPLATE_NAME_LIST_APP_JVM_NAME);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelAppResources(final String appName, final String groupName, final List<String> templateNameList) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_DELETE_GROUP_LEVEL_APP_RESOURCES_BY_APP_GROUP_NAME_TEMPLATE_NAME_LIST);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        return q.executeUpdate();
    }

    @Override
    public int deleteExternalProperties() {
        final Query q = em.createNamedQuery(JpaResourceConfigTemplate.QUERY_DELETE_RESOURCE_TEMPLATE_BY_ENTITY_TYPE);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_TYPE, EntityType.EXT_PROPERTIES);
        return q.executeUpdate();
    }

    @Override
    public JpaWebServerConfigTemplate getWebServerResource(final String resourceName, final String webServerName) {
        final Query q = em.createNamedQuery(JpaWebServerConfigTemplate.QUERY_GET_WEBSERVER_RESOURCE);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
        return (JpaWebServerConfigTemplate) q.getSingleResult();
    }

    @Override
    public JpaJvmConfigTemplate getJvmResource(final String resourceName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaJvmConfigTemplate.QUERY_GET_JVM_RESOURCE);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return (JpaJvmConfigTemplate) q.getSingleResult();
    }

    @Override
    public JpaApplicationConfigTemplate getAppResource(final String resourceName, final String appName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplicationConfigTemplate.QUERY_GET_APP_RESOURCE);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return (JpaApplicationConfigTemplate) q.getSingleResult();
    }

    @Override
    public JpaGroupWebServerConfigTemplate getGroupLevelWebServerResource(final String resourceName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupWebServerConfigTemplate.QUERY_GET_GROUP_LEVEL_WEBSERVER_RESOURCE);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return (JpaGroupWebServerConfigTemplate) q.getSingleResult();
    }

    @Override
    public JpaGroupJvmConfigTemplate getGroupLevelJvmResource(final String resourceName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupJvmConfigTemplate.QUERY_GET_GROUP_LEVEL_JVM_RESOURCE);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return (JpaGroupJvmConfigTemplate) q.getSingleResult();
    }

    @Override
    public JpaGroupAppConfigTemplate getGroupLevelAppResource(final String resourceName, final String appName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_GET_GROUP_LEVEL_APP_RESOURCE);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        return (JpaGroupAppConfigTemplate) q.getSingleResult();
    }

    @Override
    public List<String> getGroupLevelAppResourceNames(String groupName, String webAppName) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_APP_RESOURCE_NAMES);
        q.setParameter("grpName", groupName);
        q.setParameter("appName", webAppName);
        return q.getResultList();
    }

    @Override
    public JpaResourceConfigTemplate getExternalPropertiesResource(String resourceName) {
        // TODO make generic for all resources
        final Query q = em.createNamedQuery(JpaResourceConfigTemplate.GET_RESOURCE_TEMPLATE_CONTENT);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceName);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_APP_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_GRP_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_TYPE, EntityType.EXT_PROPERTIES);

        return (JpaResourceConfigTemplate) q.getSingleResult();
    }

    @Override
    public JpaResourceConfigTemplate createResource(Long entityId, Long groupId, Long appId, EntityType entityType, String resourceFileName, String templateContent, String metaData) {

        JpaResourceConfigTemplate resourceTemplate = new JpaResourceConfigTemplate();
        resourceTemplate.setEntityId(entityId);
        resourceTemplate.setGrpId(groupId);
        resourceTemplate.setAppId(appId);
        resourceTemplate.setTemplateContent(templateContent);
        resourceTemplate.setTemplateName(resourceFileName);
        resourceTemplate.setEntityType(entityType);
        resourceTemplate.setMetaData(metaData);

        em.persist(resourceTemplate);
        em.flush();

        return resourceTemplate;
    }

    @Override
    public void updateResource(ResourceIdentifier resourceIdentifier, EntityType entityType, String templateContent) {
        final Query q = em.createNamedQuery(JpaResourceConfigTemplate.UPDATE_RESOURCE_TEMPLATE_CONTENT);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_TYPE, entityType);
        // TODO make this more generic and actually use the resource identifier fields
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_GRP_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_APP_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceIdentifier.resourceName);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_TEMPLATE_CONTENT, templateContent);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(resourceIdentifier.toString(), resourceIdentifier.resourceName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(resourceIdentifier.toString(), resourceIdentifier.resourceName);
        }
    }

    @Override
    public List<String> getResourceNames(ResourceIdentifier identifier, EntityType entityType) {
        // TODO make generic for all resources
        final Query q = em.createNamedQuery(JpaResourceConfigTemplate.GET_RESOURCE_TEMPLATE_NAMES);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_TYPE, EntityType.EXT_PROPERTIES);

        return q.getResultList();
    }
}