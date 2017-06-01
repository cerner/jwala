package com.cerner.jwala.common.domain.model.springboot;

import com.cerner.jwala.common.domain.model.media.Media;

import java.util.List;

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

    public List<String> getHostnames() {
        return hostnames;
    }

    public String getArchiveFileName() {
        return archiveFileName;
    }

    public Media getJdkMedia() {
        return jdkMedia;
    }

    private final Long id;
    private final String name;
    private final List<String> hostnames;
    private final String archiveFileName;
    private final Media jdkMedia;

    public SpringBootApp(Long id, String name, List<String> hostnames, String archiveFileName, Media jdkMedia) {

        this.id = id;
        this.name = name;
        this.hostnames = hostnames;
        this.archiveFileName = archiveFileName;
        this.jdkMedia = jdkMedia;
    }

}
