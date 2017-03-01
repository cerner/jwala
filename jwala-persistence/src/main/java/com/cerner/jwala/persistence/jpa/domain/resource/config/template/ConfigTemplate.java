package com.cerner.jwala.persistence.jpa.domain.resource.config.template;

import javax.persistence.*;

import com.cerner.jwala.persistence.jpa.domain.AbstractEntity;

/**
 * Base POJO for resource configuration template data.
 *
 * Created by Jedd Cuison on 4/4/2016.
 */
@MappedSuperclass
public class ConfigTemplate extends AbstractEntity<ConfigTemplate> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name = "TEMPLATE_CONTENT", nullable = false, length = 2147483647)
    private String templateContent;

    @Column(nullable = false, length = 2147483647)
    private String metaData;

    private boolean locked;

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
