package com.cerner.jwala.persistence.jpa.domain.resource.config.template;

import com.cerner.jwala.persistence.jpa.domain.JpaGroup;

import javax.persistence.*;

/**
 * POJO that describes a db table that holds data about a group of JVM related resource configuration templates.
 */
@Entity
@Table(name = "GRP_JVM_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupJvmConfigTemplate t WHERE LOWER(t.grp.name) = " +
                        "LOWER(:grpName)"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupJvmConfigTemplate t where LOWER(t.grp.name) = " +
                        "LOWER(:grpName) and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_META_DATA,
                query = "SELECT t.metaData FROM JpaGroupJvmConfigTemplate t where LOWER(t.grp.name) = LOWER(:grpName)" +
                        " and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.UPDATE_GROUP_JVM_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupJvmConfigTemplate t SET t.templateContent = :templateContent WHERE LOWER(t" +
                        ".grp.name) = LOWER(:grpName) AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.UPDATE_GROUP_JVM_TEMPLATE_META_DATA,
                query = "UPDATE JpaGroupJvmConfigTemplate t SET t.metaData = :metaData WHERE LOWER(t.grp.name) = " +
                        "LOWER(:grpName) AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.QUERY_DELETE_GRP_JVM_TEMPLATE, query = "DELETE FROM JpaGroupJvmConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_RESOURCE_NAME,
                query = "SELECT t.templateName FROM JpaGroupJvmConfigTemplate t WHERE LOWER(t.grp.name) = " +
                        "LOWER(:grpName) AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCE_BY_TEMPLATE_GROUP_NAME,
                query = "DELETE FROM JpaGroupJvmConfigTemplate t WHERE t.templateName = :templateName AND LOWER(t.grp" +
                        ".name) = LOWER(:grpName)"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_GROUP_NAME,
                    query = "DELETE FROM JpaGroupJvmConfigTemplate t WHERE t.templateName IN :templateNameList AND " +
                            "LOWER(t.grp.name) = LOWER(:grpName)"),
        @NamedQuery(name = JpaGroupJvmConfigTemplate.QUERY_GET_GROUP_LEVEL_JVM_RESOURCE,
                query = "SELECT t FROM JpaGroupJvmConfigTemplate t where LOWER(t.grp.name) = LOWER(:grpName) and t" +
                        ".templateName = :templateName")
})
public class JpaGroupJvmConfigTemplate extends ConfigTemplate {
    public static final String GET_GROUP_JVM_TEMPLATE_RESOURCE_NAMES = "getGroupJvmTemplateResourceNames";
    public static final String GET_GROUP_JVM_TEMPLATE_CONTENT = "getGroupJvmTemplateContent";
    public static final String UPDATE_GROUP_JVM_TEMPLATE_CONTENT = "updateGroupJvmTemplateContent";
    public static final String UPDATE_GROUP_JVM_TEMPLATE_META_DATA = "updateGroupJvmTemplateMetaData";
    public static final String GET_GROUP_JVM_TEMPLATE_META_DATA = "getGroupJvmMetaData";
    public static final String QUERY_DELETE_GRP_JVM_TEMPLATE = "deleteGrpJvmTemplate";
    public static final String QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCE_BY_TEMPLATE_GROUP_NAME = "deleteGroupLevelJvmResourceByTemplateGroupName";
    public static final String QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_GROUP_NAME = "deleteGroupLevelJvmResourcesByTemplateNameListAndGroupName";
    public static final String QUERY_GET_GROUP_LEVEL_JVM_RESOURCE = "getGroupLevelJvmResource";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_TEMPLATE_NAME_LIST = "templateNameList";
    public static final String QUERY_PARAM_GROUP_NAME = "grpName";

    public static final String GET_GROUP_JVM_TEMPLATE_RESOURCE_NAME = "getGroupJvmTemplateResourceName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaGroup grp;

    public JpaGroup getJpaGroup() {
        return grp;
    }

    public void setJpaGroup(JpaGroup jpaGroup) {
        this.grp = jpaGroup;
    }
}
