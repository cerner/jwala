package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.common.domain.model.media.MediaType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Jlkwison on 6/1/2017
 */
@Entity
@NamedQueries({@NamedQuery(name = JpaSpringBootApp.QUERY_FIND_BY_NAME, query = "SELECT m FROM media m WHERE lower(m.name) = lower(:name)")})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class JpaSpringBootApp  extends AbstractEntity<JpaSpringBootApp> {

    public static final String QUERY_FIND_BY_NAME = "QUERY_FIND_BY_NAME";
    public static final String QUERY_FIND_BY_NAME_TYPE = "QUERY_FIND_BY_NAME_TYPE";
    public static final String PARAM_NAME = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Size(min = 2, max = 200, message = "{app.name.length.msg}")
    private String name;

    private List<String> hostName;

    private String archiveFilename;

    private MediaType jdkMedia;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getHostName() {
        return hostName;
    }

    public String getArchiveFilename() {
        return archiveFilename;
    }

    public MediaType getJdkMedia() {
        return jdkMedia;
    }

}
