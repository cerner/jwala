package com.cerner.jwala.common.request.webserver;

import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidTemplateNameRule;
import com.cerner.jwala.common.rule.webserver.WebServerIdRule;

import java.io.Serializable;

/**
 * Request wrapper for uploading web server resource templates.
 *
 * Created by Jeffery Mahmood on 8/26/2015.
 */
@Deprecated
public abstract class UploadWebServerTemplateRequest implements Serializable, Request {
    private final WebServer webServer;
    private final String fileName;
    private final String templateContent;
    private final String metaData;

    public UploadWebServerTemplateRequest(final WebServer webServer, final String fileName, final String templateContent) {
        this.webServer = webServer;
        this.fileName = fileName;
        this.templateContent = templateContent;
        this.metaData = null;
    }

    public UploadWebServerTemplateRequest(final WebServer webServer, final String fileName, final String metaData,
                                          final String templateContent) {
        this.webServer = webServer;
        this.fileName = fileName;
        this.templateContent = templateContent;
        this.metaData = metaData;
    }

    @Override
    public void validate() {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new WebServerIdRule(this.webServer.getId())
        ).validate();

    }

    public WebServer getWebServer() {
        return webServer;
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
        return "UploadWebServerTemplateRequest{" +
                "webServer=" + webServer +
                ", fileName='" + fileName + '\'' +
                ", templateContent='" + templateContent + '\'' +
                ", metaData='" + metaData + '\'' +
                '}';
    }
}
