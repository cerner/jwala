package com.cerner.jwala.ws.rest.v1.service.resource;

/**
 * Wrapper that contains parameters for create resource REST service.
 *
 * Created by Jedd Cuison on 6/6/2016.
 */
public class CreateResourceParam {

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
        return "CreateResourceParam{" +
                "group='" + group + '\'' +
                ", webServer='" + webServer + '\'' +
                ", jvm='" + jvm + '\'' +
                ", webApp='" + webApp + '\'' +
                '}';
    }
}
