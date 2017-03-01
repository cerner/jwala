package com.cerner.jwala.persistence.jpa.domain.resource.config.template;

import javax.persistence.*;

import com.cerner.jwala.common.domain.model.resource.EntityType;

@Entity
@Table(name = "RESOURCE_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"ENTITY_ID", "APP_ID", "GRP_ID", "ENTITY_TYPE", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaResourceConfigTemplate.GET_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT t.templateName FROM JpaResourceConfigTemplate t WHERE t.entityType = :entityType"),
        @NamedQuery(name = JpaResourceConfigTemplate.GET_RESOURCE_TEMPLATE_CONTENT,
                query = "SELECT t FROM JpaResourceConfigTemplate t where t.entityId = :entityId and t.appId = :appId and t.grpId = :grpId and t.entityType = :entityType and t.templateName = :templateName"),
        @NamedQuery(name = JpaResourceConfigTemplate.GET_RESOURCE_TEMPLATE_META_DATA,
                query = "SELECT t.metaData FROM JpaResourceConfigTemplate t where t.jvm.name = :jvmName and t.templateName = :templateName"),
        @NamedQuery(name = JpaResourceConfigTemplate.UPDATE_RESOURCE_TEMPLATE_CONTENT,
                query = "UPDATE JpaResourceConfigTemplate t SET t.templateContent = :templateContent WHERE t.entityId = :entityId and t.appId = :appId and t.grpId = :grpId and t.entityType = :entityType and t.templateName = :templateName"),
        @NamedQuery(name = JpaResourceConfigTemplate.QUERY_DELETE_RESOURCE_TEMPLATE_BY_ENTITY_TYPE, query = "DELETE FROM JpaResourceConfigTemplate t WHERE t.entityType = :entityType"),
        @NamedQuery(name = JpaResourceConfigTemplate.QUERY_DELETE_RESOURCE_BY_TEMPLATE_NAME, query = "DELETE FROM JpaResourceConfigTemplate t WHERE t.templateName = :templateName AND t.jvm.name = :jvmName"),
        @NamedQuery(name = JpaResourceConfigTemplate.QUERY_DELETE_RESOURCES_BY_TEMPLATE_NAME_LIST_ENTITY_NAME, query = "DELETE FROM JpaResourceConfigTemplate t WHERE t.templateName IN :templateNameList AND t.jvm.name = :jvmName"),
        @NamedQuery(name = JpaResourceConfigTemplate.QUERY_GET_RESOURCE_TEMPLATES,
                query = "SELECT t FROM JpaResourceConfigTemplate t WHERE t.jvm.name = :jvmName"),
        @NamedQuery(name = JpaResourceConfigTemplate.GET_TEMPLATE_RESOURCE_NAME,
                query = "SELECT t.templateName FROM JpaResourceConfigTemplate t WHERE t.jvm.name = :jvmName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaResourceConfigTemplate.QUERY_GET_RESOURCE,
                query = "SELECT t FROM JpaResourceConfigTemplate t WHERE t.jvm.name = :jvmName AND t.templateName = :templateName")
})
public class JpaResourceConfigTemplate extends ConfigTemplate{
    public static final String GET_RESOURCE_TEMPLATE_NAMES = "getResourceTemplateNames";
    public static final String GET_RESOURCE_TEMPLATE_CONTENT = "getResourceTemplateContent";
    public static final String GET_RESOURCE_TEMPLATE_META_DATA = "getResourceTemplateMetaData";
    public static final String UPDATE_RESOURCE_TEMPLATE_CONTENT = "updateResourceTemplateContent";
    public static final String QUERY_DELETE_RESOURCE_TEMPLATE_BY_ENTITY_TYPE = "queryDeleteResourceTemple";
    public static final String QUERY_DELETE_RESOURCE_BY_TEMPLATE_NAME = "queryDeleteResourceByTemplateName";
    public static final String QUERY_DELETE_RESOURCES_BY_TEMPLATE_NAME_LIST_ENTITY_NAME = "queryDeleteResourcesByTemplateNameListEntityName";
    public static final String QUERY_GET_RESOURCE_TEMPLATES = "queryGetResourceTemplates";
    public static final String GET_TEMPLATE_RESOURCE_NAME = "getTemplateResourceName";
    public static final String QUERY_GET_RESOURCE = "queryGetResource";

    public static final String QUERY_PARAM_ENTITY_ID = "entityId";
    public static final String QUERY_PARAM_APP_ID = "appId";
    public static final String QUERY_PARAM_GRP_ID = "grpId";
    public static final String QUERY_PARAM_ENTITY_TYPE = "entityType";
    public static final String QUERY_PARAM_TEMPLATE_NAME= "templateName";
    public static final String QUERY_PARAM_TEMPLATE_CONTENT= "templateContent";

    @Column(name = "ENTITY_ID", nullable = true)
    private Long entityId;

    @Column(name = "APP_ID", nullable = true)
    private Long appId;

    @Column(name = "GRP_ID", nullable = true)
    private Long grpId;

    @Column(name = "ENTITY_TYPE", nullable = true)
    private EntityType entityType;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getGroupId() {
        return grpId;
    }

    public void setGrpId(Long grpId) {
        this.grpId = grpId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
}
