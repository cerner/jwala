package com.cerner.jwala.common.domain.model.media;

import java.io.Serializable;

/**
 * Created by RS045609 on 12/2/2016.
 */
public class Media implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String path;
    private String type;
    private String remoteHostPath;
    private String mediaDir;

    public Media(Integer id, String name, String path, String type, String remoteHostPath, String mediaDir) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.type = type;
        this.remoteHostPath = remoteHostPath;
        this.mediaDir = mediaDir;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemoteHostPath() {
        return remoteHostPath;
    }

    public void setRemoteHostPath(String remoteHostPath) {
        this.remoteHostPath = remoteHostPath;
    }

    public String getMediaDir() {
        return mediaDir;
    }

    public void setMediaDir(String mediaDir) {
        this.mediaDir = mediaDir;
    }
}
