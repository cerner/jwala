package com.cerner.jwala.common.request.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidTemplateNameRule;
import com.cerner.jwala.common.rule.app.ApplicationIdRule;
import com.cerner.jwala.common.rule.jvm.JvmNameRule;

import java.io.Serializable;

/**
 * Request wrapper for uploading an application resource template.
 */
public class UploadAppTemplateRequest implements Serializable, Request {
    private final Application application;
    private final String fileName;
    private final String jvmName;
    private final String templateContent;
    private final String confFileName;
    private final String medataData;

    public UploadAppTemplateRequest(final Application application, final String name, final String confFileName,
                                    final String jvmName, final String templateContent) {

        this.application = application;
        this.fileName = name;
        this.jvmName = jvmName;
        this.templateContent = templateContent;
        this.confFileName = confFileName;
        this.medataData = null;
    }

    public UploadAppTemplateRequest(final Application application, final String name, final String confFileName,
                                    final String jvmName, final String metaData, final String templateContent) {

        this.application = application;
        this.fileName = name;
        this.jvmName = jvmName;
        this.templateContent = templateContent;
        this.confFileName = confFileName;
        this.medataData = metaData;
    }

    public void validate() {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new ApplicationIdRule(this.application.getId()),
                new JvmNameRule(this.jvmName)
        ).validate();
    }

    public Application getApp() {
        return application;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public String getConfFileName() {
        return confFileName;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getMedataData() {
        return medataData;
    }

    @Override
    public String toString() {
        return "UploadAppTemplateRequest{" +
                "application=" + application +
                ", fileName='" + fileName + '\'' +
                ", jvmName='" + jvmName + '\'' +
                ", templateContent='" + templateContent + '\'' +
                ", confFileName='" + confFileName + '\'' +
                ", medataData='" + medataData + '\'' +
                '}';
    }
}
