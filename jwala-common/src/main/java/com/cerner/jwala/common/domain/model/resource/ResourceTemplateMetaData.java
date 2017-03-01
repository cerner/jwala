package com.cerner.jwala.common.domain.model.resource;

import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Resource template meta data.
 * <p>
 * Created by Jedd Cuison on 3/30/2016.
 */
public class ResourceTemplateMetaData {
    private final String templateName;

    @JsonDeserialize(using = MediaTypeToStrDeserializer.class)
    private final MediaType contentType;

    private final String deployFileName;
    private final String deployPath;
    private final Entity entity;
    private boolean unpack;
    private boolean overwrite;

    @JsonIgnore
    private String jsonData;

    @JsonCreator
    public ResourceTemplateMetaData(@JsonProperty("templateName") final String templateName,
                                    @JsonProperty("contentType") final MediaType contentType,
                                    @JsonProperty("deployFileName") final String deployFileName,
                                    @JsonProperty("deployPath") final String deployPath,
                                    @JsonProperty("entity") final Entity entity,
                                    @JsonProperty("unpack") final Boolean unpack,
                                    @JsonProperty("overwrite") Boolean overwrite) {
        this.templateName = templateName;
        this.contentType = contentType;
        this.deployFileName = deployFileName;
        this.deployPath = deployPath;
        this.entity = entity;
        this.unpack = unpack == null ? false : unpack;
        this.overwrite = overwrite == null ? true : overwrite;
    }

    public String getTemplateName() {
        return templateName;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getDeployFileName() {
        return deployFileName;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public boolean isUnpack() {
        return unpack;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    @Override
    public String toString() {
        return "ResourceTemplateMetaData{" +
                "templateName='" + templateName + '\'' +
                ", contentType=" + contentType +
                ", deployFileName='" + deployFileName + '\'' +
                ", deployPath='" + deployPath + '\'' +
                ", entity=" + entity +
                ", unpack=" + unpack +
                ", overwrite=" + overwrite +
                '}';
    }
}
