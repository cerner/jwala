package com.cerner.jwala.common.domain.model.media;

import com.cerner.jwala.common.domain.model.PathToStringSerializer;
import com.cerner.jwala.common.domain.model.StringToPathDeserializer;
import org.apache.tika.mime.MediaType;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.nio.file.Path;

/**
 * Created by Rahul Sayini on 12/2/2016
 */
public class Media {

    private Long id;
    private String name;
    private MediaType type;
    private Path path;
    private Path remoteHostPath;
    private Path mediaDir;

    public Media() {
    }

    public Media(final Long id, final String name, final MediaType type, final Path path, final Path remoteHostPath,
                 final Path mediaDir) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.path = path;
        this.remoteHostPath = remoteHostPath;
        this.mediaDir = mediaDir;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getPath() {
        return path;
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setPath(Path path) {
        this.path = path;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getRemoteHostPath() {
        return remoteHostPath;
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setRemoteHostPath(Path remoteHostPath) {
        this.remoteHostPath = remoteHostPath;
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getMediaDir() {
        return mediaDir;
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setMediaDir(Path mediaDir) {
        this.mediaDir = mediaDir;
    }

}
