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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        if (id != null ? !id.equals(media.id) : media.id != null) return false;
        if (name != null ? !name.equals(media.name) : media.name != null) return false;
        if (path != null ? !path.equals(media.path) : media.path != null) return false;
        if (type != null ? !type.equals(media.type) : media.type != null) return false;
        if (remoteHostPath != null ? !remoteHostPath.equals(media.remoteHostPath) : media.remoteHostPath != null)
            return false;
        return mediaDir != null ? mediaDir.equals(media.mediaDir) : media.mediaDir == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (remoteHostPath != null ? remoteHostPath.hashCode() : 0);
        result = 31 * result + (mediaDir != null ? mediaDir.hashCode() : 0);
        return result;
    }
}
