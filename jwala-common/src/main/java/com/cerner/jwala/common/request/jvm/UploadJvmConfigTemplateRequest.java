package com.cerner.jwala.common.request.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;

/**
 * Created by Jeffery Mahmood on 9/15/2015.
 */
public class UploadJvmConfigTemplateRequest extends UploadJvmTemplateRequest {
    private String confFileName;

    public UploadJvmConfigTemplateRequest(final Jvm jvm, final String fileName, final String templateContent, final String metaData) {
        super(jvm, fileName, templateContent, metaData);
    }

    public void setConfFileName(String fileName){
        confFileName = fileName;
    }

    @Override
    public String getConfFileName() {
        return confFileName;
    }
}
