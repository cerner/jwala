package com.cerner.jwala.common.domain.model.balancermanager;

import java.util.ArrayList;
import java.util.List;

public class BalancerManagerState {

    private List<GroupDrainStatus> groups = new ArrayList<>();

    public List<GroupDrainStatus> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDrainStatus> groups) {
        this.groups = groups;
    }

    public BalancerManagerState(List<BalancerManagerState.GroupDrainStatus> groups){
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "BalancerManagerState{" +
                "groups=" + groups +
                '}';
    }

    public static class GroupDrainStatus {

        public GroupDrainStatus(String groupName, List<GroupDrainStatus.WebServerDrainStatus> webServers) {
            this.groupName = groupName;
            this.webServers = webServers;
        }

        private String groupName;
        private List<WebServerDrainStatus> webServers = new ArrayList<>();

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<WebServerDrainStatus> getwebServers() {
            return webServers;
        }

        public void setwebServers(List<WebServerDrainStatus> webServers) {
            this.webServers = webServers;
        }

        @Override
        public String toString() {
            return "BalancerManagerState{" +
                    "groupName='" + groupName + '\'' +
                    ", webServers=" + webServers +
                    '}';
        }

        public static class WebServerDrainStatus {

            public WebServerDrainStatus(String webServerName, List<JvmDrainStatus> webServer) {
                this.webServerName = webServerName;
                this.webServer = webServer;
            }

            private String webServerName;
            private List<JvmDrainStatus> webServer = new ArrayList<>();

            public String getWebServerName() {
                return webServerName;
            }

            public void setWebServerName(String webServerName) {
                this.webServerName = webServerName;
            }

            public List<JvmDrainStatus> getjvms() {
                return webServer;
            }

            public void setjvms(List<JvmDrainStatus> webServer) {
                this.webServer = webServer;
            }

            @Override
            public String toString() {
                return "WebServerDrainStatus{" +
                        "webServerName='" + webServerName + '\'' +
                        ", webServer=" + webServer +
                        '}';
            }

            public static class JvmDrainStatus {
                private String jvmName;
                private String ignoreError;
                private String drainingMode;
                private String disabled;
                private String hotStandby;
                private String appName;
                private String workerUrl;

                public JvmDrainStatus(String workerUrl, String jvmName, String appName, String ignoreError, String drainingMode, String disabled, String hotStandby) {
                    this.workerUrl = workerUrl;
                    this.jvmName = jvmName;
                    this.appName = appName;
                    this.ignoreError = ignoreError;
                    this.drainingMode = drainingMode;
                    this.disabled = disabled;
                    this.hotStandby = hotStandby;
                }

                public String getJvmName() {
                    return jvmName;
                }

                public void setJvmName(String jvmName) {
                    this.jvmName = jvmName;
                }

                public String getIgnoreError() {
                    return ignoreError;
                }

                public void setIgnoreError(String ignoreError) {
                    this.ignoreError = ignoreError;
                }

                public String getDrainingMode() {
                    return drainingMode;
                }

                public void setDrainingMode(String drainingMode) {
                    this.drainingMode = drainingMode;
                }

                public String getDisabled() {
                    return disabled;
                }

                public void setDisabled(String disabled) {
                    this.disabled = disabled;
                }

                public String getHotStandby() {
                    return hotStandby;
                }

                public void setHotStandby(String hotStandy) {
                    this.hotStandby = hotStandy;
                }

                public String getAppName() {
                    return appName;
                }

                public void setAppName(String appName) {
                    this.appName = appName;
                }

                public String getWorkerUrl() {
                    return workerUrl;
                }

                public void setWorkerUrl(String workerUrl) {
                    this.workerUrl = workerUrl;
                }

                @Override
                public String toString() {
                    return "JvmDrainStatus{" +
                            "jvmName='" + jvmName + '\'' +
                            ", ignoreError='" + ignoreError + '\'' +
                            ", drainingMode='" + drainingMode + '\'' +
                            ", disabled='" + disabled + '\'' +
                            ", hotStandby='" + hotStandby + '\'' +
                            ", appName='" + appName + '\'' +
                            ", workerUrl='" + workerUrl + '\'' +
                            '}';
                }
            }
        }
    }


}