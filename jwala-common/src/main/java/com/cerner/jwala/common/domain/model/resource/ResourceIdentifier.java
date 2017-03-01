package com.cerner.jwala.common.domain.model.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Contains parameters that identify a resource
 *
 * Created by Jedd Cuison on 7/18/2016
 */
public class ResourceIdentifier {

    public final String resourceName;
    public final String groupName;
    public final String webServerName;
    public final String jvmName;
    public final String webAppName;

    private ResourceIdentifier(final Builder builder) {
        this.resourceName = builder.resourceName;
        this.groupName = builder.groupName;
        this.webServerName = builder.webServerName;
        this.jvmName = builder.jvmName;
        this.webAppName = builder.webAppName;
    }

    public static class Builder {
        private String resourceName;
        private String groupName;
        private String webServerName;
        private String jvmName;
        private String webAppName;

        public Builder setResourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public Builder setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder setWebServerName(String webServerName) {
            this.webServerName = webServerName;
            return this;
        }

        public Builder setJvmName(String jvmName) {
            this.jvmName = jvmName;
            return this;
        }

        public Builder setWebAppName(String webAppName) {
            this.webAppName = webAppName;
            return this;
        }

        public ResourceIdentifier build() {
            return new ResourceIdentifier(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || ResourceIdentifier.class != o.getClass()) return false;

        ResourceIdentifier that = (ResourceIdentifier) o;

        return new EqualsBuilder()
                .append(resourceName, that.resourceName)
                .append(groupName, that.groupName)
                .append(webServerName, that.webServerName)
                .append(jvmName, that.jvmName)
                .append(webAppName, that.webAppName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(resourceName)
                .append(groupName)
                .append(webServerName)
                .append(jvmName)
                .append(webAppName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ResourceIdentifier{" +
                "resourceName='" + resourceName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", webServerName='" + webServerName + '\'' +
                ", jvmName='" + jvmName + '\'' +
                ", webAppName='" + webAppName + '\'' +
                '}';
    }
}
