package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.common.domain.model.PathToStringSerializer;
import com.cerner.jwala.common.domain.model.StringToPathDeserializer;
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.persistence.jpa.domain.constraint.ValidPath;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * POJO that defines a media such as jdk, tomcat application server,  web srve* Creted by Jedd Cuison on 12/6/2016
 */
@Entity(name = "media")
@NamedQueries({@NamedQuery(name = JpaMedia.QUERY_FIND_BY_NAME, query = "SELECT m FROM media m WHERE lower(m.name) = lower(:name)"),
        @NamedQuery(name = JpaMedia.QUERY_FIND_BY_NAME_TYPE, query = "SELECT m FROM media m WHERE lower(m.name) = lower(:name) AND m.type = (:type)")})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class JpaMedia extends AbstractEntity<JpaMedia> {

    public static final String QUERY_FIND_BY_NAME = "QUERY_FIND_BY_NAME";
    public static final String QUERY_FIND_BY_NAME_TYPE = "QUERY_FIND_BY_NAME_TYPE";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_TYPE = "type";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(min = 2, max = 200, message = "{media.name.length.msg}")
    private String name;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private MediaType type;

    @ValidPath(allowableFileExtensions = {"zip", "gz"})
    private String localPath;

    @ValidPath
    private String remoteDir; // e.g. c:/ctp

    private String mediaDir;  // e.g. tomcat-7.0

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(final MediaType type) {
        this.type = type;
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getLocalPath() {
        return StringUtils.isEmpty(localPath) ? null : Paths.get(localPath);
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setLocalPath(final Path localPath) {
        this.localPath = localPath.toString();
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getRemoteDir() {
        return Paths.get(remoteDir);
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setRemoteDir(final Path remoteDir) {
        this.remoteDir = remoteDir.toString();
    }

    @JsonSerialize(using = PathToStringSerializer.class)
    public Path getMediaDir() {
        return StringUtils.isEmpty(mediaDir) ? null : Paths.get(mediaDir);
    }

    @JsonDeserialize(using = StringToPathDeserializer.class)
    public void setMediaDir(final Path mediaDir) {
        this.mediaDir = mediaDir.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpaMedia jpaMedia = (JpaMedia) o;

        if (!name.equals(jpaMedia.name)) return false;
        return type == jpaMedia.type;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Media{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", localPath='" + localPath + '\'' +
                ", remoteDir='" + remoteDir + '\'' +
                ", mediaDir='" + mediaDir + '\'' +
                '}';
    }

}
