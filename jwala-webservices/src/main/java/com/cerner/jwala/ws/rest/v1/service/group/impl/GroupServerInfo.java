package com.cerner.jwala.ws.rest.v1.service.group.impl;

/**
 * Wrapper to pass jvm and web server count information to the UI.
 */
class GroupServerInfo {
    private final String groupName;
    private final Long jvmCount;
    private final Long jvmStartedCount;
    private final Long jvmStoppedCount;
    private final Long jvmForciblyStoppedCount;
    private final Long webServerCount;
    private final Long webServerStartedCount;
    private final Long webServerStoppedCount;

    public GroupServerInfo(final String groupName, final Long jvmCount, final Long jvmStartedCount, final Long jvmStoppedCount,
                           final Long jvmForciblyStoppedCount, final Long webServerCount, final Long webServerStartedCount,
                           final Long webServerStoppedCount) {
        this.groupName = groupName;
        this.jvmCount = jvmCount;
        this.jvmStartedCount = jvmStartedCount;
        this.jvmStoppedCount = jvmStoppedCount;
        this.jvmForciblyStoppedCount = jvmForciblyStoppedCount;
        this.webServerCount = webServerCount;
        this.webServerStartedCount = webServerStartedCount;
        this.webServerStoppedCount = webServerStoppedCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getJvmCount() {
        return jvmCount;
    }

    public Long getJvmStartedCount() {
        return jvmStartedCount;
    }

    public Long getJvmStoppedCount() {
        return jvmStoppedCount;
    }

    public Long getJvmForciblyStoppedCount() {
        return jvmForciblyStoppedCount;
    }

    public Long getWebServerCount() {
        return webServerCount;
    }

    public Long getWebServerStartedCount() {
        return webServerStartedCount;
    }

    public Long getWebServerStoppedCount() {
        return webServerStoppedCount;
    }
}
