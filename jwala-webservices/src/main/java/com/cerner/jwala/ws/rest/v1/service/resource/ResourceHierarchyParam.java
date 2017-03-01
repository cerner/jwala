package com.cerner.jwala.ws.rest.v1.service.resource;

/**
 * POJO that contains resource related hierarchy parameters.
 * <p/>
 * Created by Jedd Cuison on 6/2/2016.
 */
public class ResourceHierarchyParam {

    private String group;
    private String webServer;
    private String jvm;
    private String webApp;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getWebServer() {
        return webServer;
    }

    public void setWebServer(String webServer) {
        this.webServer = webServer;
    }

    public String getJvm() {
        return jvm;
    }

    public void setJvm(String jvm) {
        this.jvm = jvm;
    }

    public String getWebApp() {
        return webApp;
    }

    public void setWebApp(String webApp) {
        this.webApp = webApp;
    }

    @Override
    public String toString() {
        return "ResourceHierarchyParam{" +
                "group='" + group + '\'' +
                ", webServer='" + webServer + '\'' +
                ", jvm='" + jvm + '\'' +
                ", webApp='" + webApp + '\'' +
                '}';
    }
}
