package com.cerner.jwala.common.domain.model.springboot;

import com.cerner.jwala.common.domain.model.media.Media;

/**
 * Created on 6/1/2017.
 */
public class SpringBootApp {

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHostnames() {
        return hostnames;
    }

    public String getArchiveFileName() {
        return archiveFileName;
    }

    public Media getJdkMedia() {
        return jdkMedia;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHostnames(String hostnames) {
        this.hostnames = hostnames;
    }

    public void setArchiveFileName(String archiveFileName) {
        this.archiveFileName = archiveFileName;
    }

    public void setJdkMedia(Media jdkMedia) {
        this.jdkMedia = jdkMedia;
    }

    private Long id;
    private String name;
    private String hostnames;
    private String archiveFileName;
    private Media jdkMedia;

    public SpringBootApp() {

    }

    public SpringBootApp(Long id, String name, String hostnames, String archiveFileName, Media jdkMedia) {
        this.id = id;
        this.name = name;
        this.hostnames = hostnames;
        this.archiveFileName = archiveFileName;
        this.jdkMedia = jdkMedia;
    }

}
