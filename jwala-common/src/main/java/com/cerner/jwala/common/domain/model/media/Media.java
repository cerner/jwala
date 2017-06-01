package com.cerner.jwala.common.domain.model.media;

import com.cerner.jwala.common.domain.model.PathToStringSerializer;
import com.cerner.jwala.common.domain.model.StringToPathDeserializer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    private Path rootDir;

    public Media() {
    }

    public Media(final Long id, final String name, final MediaType type, final Path path, final Path remoteDir,
                 final Path rootDir) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.localPath = path;
        this.remoteDir = remoteDir;
        this.rootDir = rootDir;
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
    public Path getRootDir() {
        return rootDir;
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(type)
                .append(localPath)
                .append(remoteDir)
                .append(rootDir)
                .toHashCode();    
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        return new EqualsBuilder()
                .append(id, media.id)
                .append(name, media.name)
                .append(type, media.type)
                .append(localPath, media.localPath)
                .append(remoteDir, media.remoteDir)
                .append(rootDir, media.rootDir)
                .isEquals();
    }
}