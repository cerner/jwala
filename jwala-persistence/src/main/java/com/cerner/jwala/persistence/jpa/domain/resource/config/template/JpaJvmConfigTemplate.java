package com.cerner.jwala.persistence.jpa.domain.resource.config.template;

import com.cerner.jwala.persistence.jpa.domain.JpaJvm;

import javax.persistence.*;

/**
 * POJO that describes a db table that holds data about JVM related resource configuration templates.
 * <p/>
 * Created by Jeffery Mahmood on 8/18/2015.
 */
@Entity
@Table(name = "JVM_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"JVM_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaJvmConfigTemplate.GET_JVM_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT t.templateName FROM JpaJvmConfigTemplate t WHERE LOWER(t.jvm.name) = LOWER(:jvmName)"),
        @NamedQuery(name = JpaJvmConfigTemplate.GET_JVM_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaJvmConfigTemplate t where LOWER(t.jvm.name) = LOWER" +
                        "(:jvmName) and t.templateName = :templateName"),
        @NamedQuery(name = JpaJvmConfigTemplate.GET_JVM_TEMPLATE_META_DATA,
                query = "SELECT t.metaData FROM JpaJvmConfigTemplate t where LOWER(t.jvm.name) = LOWER(:jvmName) and " +
                        "t.templateName = :templateName"),
        @NamedQuery(name = JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_META_DATA,
                query = "UPDATE JpaJvmConfigTemplate t SET t.metaData= :metaData WHERE LOWER(t.jvm.name) = " +
                        "LOWER(:jvmName) AND " +
                        "t.templateName = :templateName"),
        @NamedQuery(name = JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_CONTENT,
                query = "UPDATE JpaJvmConfigTemplate t SET t.templateContent = :templateContent WHERE LOWER(t.jvm" +
                        ".name) = LOWER(:jvmName) AND t.templateName = :templateName"),
        @NamedQuery(name = JpaJvmConfigTemplate.QUERY_DELETE_JVM_TEMPLATE, query = "DELETE FROM JpaJvmConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCE_BY_TEMPLATE_JVM_NAME, query = "DELETE FROM " +
                "JpaJvmConfigTemplate t WHERE t.templateName = :templateName AND LOWER(t.jvm.name) = LOWER(:jvmName)"),
        @NamedQuery(name = JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_JVM_NAME, query =
                "DELETE FROM JpaJvmConfigTemplate t WHERE t.templateName IN :templateNameList AND LOWER(t.jvm.name) =" +
                        " LOWER(:jvmName)"),
        @NamedQuery(name = JpaJvmConfigTemplate.QUERY_GET_JVM_RESOURCE_TEMPLATES,
                query = "SELECT t FROM JpaJvmConfigTemplate t WHERE LOWER(t.jvm.name) = LOWER(:jvmName)"),
        @NamedQuery(name = JpaJvmConfigTemplate.GET_JVM_TEMPLATE_RESOURCE_NAME,
                query = "SELECT t.templateName FROM JpaJvmConfigTemplate t WHERE LOWER(t.jvm.name) = LOWER(:jvmName) " +
                        "AND t.templateName = :templateName"),
        @NamedQuery(name = JpaJvmConfigTemplate.QUERY_GET_JVM_RESOURCE,
                query = "SELECT t FROM JpaJvmConfigTemplate t WHERE LOWER(t.jvm.name) = LOWER(:jvmName) AND t" +
                        ".templateName =" +
                        " :templateName")
})
public class JpaJvmConfigTemplate extends ConfigTemplate {
    public static final String GET_JVM_RESOURCE_TEMPLATE_NAMES = "getJvmResourceTemplateNames";
    public static final String GET_JVM_TEMPLATE_CONTENT = "getJvmTemplateContent";
    public static final String GET_JVM_TEMPLATE_META_DATA = "getJvmTemplateMetaData";
    public static final String UPDATE_JVM_TEMPLATE_CONTENT = "updateJvmTemplateContent";
    public static final String UPDATE_JVM_TEMPLATE_META_DATA = "updateJvmTemplateMetaData";
    public static final String QUERY_DELETE_JVM_TEMPLATE = "deleteJvmTemplate";
    public static final String QUERY_DELETE_JVM_RESOURCE_BY_TEMPLATE_JVM_NAME = "deleteJvmResourceByTemplateJvmName";
    public static final String QUERY_DELETE_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_JVM_NAME = "deleteJvmResourcesByTemplateNameListAndJvmName";
    public static final String QUERY_GET_JVM_RESOURCE_TEMPLATES = "getJvmResourceTemplates";
    public static final String QUERY_GET_JVM_RESOURCE = "getJvmResource";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_TEMPLATE_NAME_LIST = "templateNameList";
    public static final String QUERY_PARAM_JVM_NAME = "jvmName";

    public static final String GET_JVM_TEMPLATE_RESOURCE_NAME = "getJvmTemplateResourceName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaJvm jvm;

    public JpaJvm getJvm() {
        return jvm;
    }

    public void setJvm(final JpaJvm jvm) {
        this.jvm = jvm;
    }
}
