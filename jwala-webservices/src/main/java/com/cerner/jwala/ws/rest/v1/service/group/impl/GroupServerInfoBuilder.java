package com.cerner.jwala.ws.rest.v1.service.group.impl;

/**
 * Builder for {@link GroupServerInfo}.
 */
class GroupServerInfoBuilder {
    private String groupName;
    private Long jvmCount;
    private Long jvmStartedCount;
    private Long jvmStoppedCount;
    private Long jvmForciblyStoppedCount;
    private Long webServerCount;
    private Long webServerStartedCount;
    private Long webServerStoppedCount;

    public GroupServerInfoBuilder setGroupName(final String groupName) {
        this.groupName = groupName;
        return this;
    }

    public GroupServerInfoBuilder setJvmCount(final Long jvmCount) {
        this.jvmCount = jvmCount;
        return this;
    }

    public GroupServerInfoBuilder setJvmStartedCount(final Long jvmStartedCount) {
        this.jvmStartedCount = jvmStartedCount;
        return this;
    }

    public GroupServerInfoBuilder setJvmStoppedCount(final Long jvmStoppedCount) {
        this.jvmStoppedCount = jvmStoppedCount;
        return this;
    }

    public GroupServerInfoBuilder setJvmForciblyStoppedCount(final Long jvmForciblyStoppedCount) {
        this.jvmForciblyStoppedCount = jvmForciblyStoppedCount;
        return this;
    }

    public GroupServerInfoBuilder setWebServerCount(final Long webServerCount) {
        this.webServerCount = webServerCount;
        return this;
    }

    public GroupServerInfoBuilder setWebServerStartedCount(final Long webServerStartedCount) {
        this.webServerStartedCount = webServerStartedCount;
        return this;
    }

    public GroupServerInfoBuilder setWebServerStoppedCount(final Long webServerStoppedCount) {
        this.webServerStoppedCount = webServerStoppedCount;
        return this;
    }

    public GroupServerInfo build() {
        return new GroupServerInfo(groupName, jvmCount, jvmStartedCount, jvmStoppedCount, jvmForciblyStoppedCount,
                webServerCount, webServerStartedCount, webServerStoppedCount);
    }
}