package com.cerner.jwala.common.domain.model.media;

import com.cerner.jwala.common.domain.model.PathToStringSerializer;
import com.cerner.jwala.common.domain.model.StringToPathDeserializer;
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
    private Path localPath;
    private Path remoteDir;
    private Path mediaDir;

    public Media() {
    }

    public Media(final Long id, final String name, final MediaType type, final Path path, final Path remoteDir,
                 final Path mediaDir) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.localPath = path;
        this.remoteDir = remoteDir;
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
    public Path getLocalPath() {
        return localPath;
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setLocalPath(Path localPath) {
        this.localPath = localPath;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getRemoteDir() {
        return remoteDir;
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setRemoteDir(Path remoteDir) {
        this.remoteDir = remoteDir;
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
