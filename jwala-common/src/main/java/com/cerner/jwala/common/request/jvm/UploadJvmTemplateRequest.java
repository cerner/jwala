package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidTemplateNameRule;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;

import java.io.Serializable;

/**
 * Request wrapper to upload JVM resource template.
 *
 * Created by Jeffery Mahmood on 8/25/2015.
 */
public abstract class UploadJvmTemplateRequest implements Serializable, Request {
    private final Jvm jvm;
    private final String fileName;
    private final String templateContent;
    private final String metaData;

    public UploadJvmTemplateRequest(final Jvm jvm, final String fileName, final String templateContent) {
        this.jvm = jvm;
        this.fileName = fileName;
        this.templateContent = templateContent;
        this.metaData = null;
    }

    public UploadJvmTemplateRequest(final Jvm jvm, final String fileName, final String templateContent, final String metaData) {
        this.jvm = jvm;
        this.fileName = fileName;
        this.templateContent = templateContent;
        this.metaData = metaData;
    }

    @Override
    public void validate() {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new JvmIdRule(this.jvm.getId())
        ).validate();

    }

    public Jvm getJvm() {
        return jvm;
    }

    public String getTemplateContent(){
        return templateContent;
    }

    public abstract String getConfFileName();

    public String getMetaData() {
        return metaData;
    }

    @Override
    public String toString() {
        return "UploadJvmTemplateRequest{" +
                "jvm=" + jvm +
                ", fileName='" + fileName + '\'' +
                ", templateContent='" + templateContent + '\'' +
                ", metaData='" + metaData + '\'' +
                '}';
    }
}
