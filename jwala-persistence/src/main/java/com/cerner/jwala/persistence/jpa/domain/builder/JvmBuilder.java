package com.cerner.jwala.persistence.jpa.domain.builder;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import org.modelmapper.ModelMapper;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link Jvm} builder from a {@link JpaJvm}.
 */
public class JvmBuilder {

    private JpaJvm jpaJvm;

    private ModelMapper modelMapper = new ModelMapper();

    public JvmBuilder() {
    }

    public JvmBuilder(final JpaJvm aJvm) {
        jpaJvm = aJvm;
    }

    public JvmBuilder setJpaJvm(final JpaJvm aJvm) {
        jpaJvm = aJvm;
        return this;
    }

    public Jvm build() {
        final com.cerner.jwala.common.domain.model.jvm.JvmBuilder builder =
                new com.cerner.jwala.common.domain.model.jvm.JvmBuilder();
        final com.cerner.jwala.persistence.jpa.domain.JpaMedia jdkMedia = jpaJvm.getJdkMedia();
        builder.setId(new Identifier<Jvm>(jpaJvm.getId()))
                .setName(jpaJvm.getName())
                .setHostName(jpaJvm.getHostName())
                .setStatusPath(new Path(jpaJvm.getStatusPath()))
                .setGroups(createGroups())
                .setHttpPort(jpaJvm.getHttpPort())
                .setHttpsPort(jpaJvm.getHttpsPort())
                .setRedirectPort(jpaJvm.getRedirectPort())
                .setShutdownPort(jpaJvm.getShutdownPort())
                .setAjpPort(jpaJvm.getAjpPort())
                .setSystemProperties(jpaJvm.getSystemProperties())
                .setState(jpaJvm.getState())
                .setErrorStatus(jpaJvm.getErrorStatus())
                .setLastUpdatedDate(jpaJvm.getLastUpdateDate())
                .setUserName(jpaJvm.getUserName())
                .setEncryptedPassword(jpaJvm.getEncryptedPassword())
                .setJdkMedia(jpaJvm.getJdkMedia() == null ? null : modelMapper.map(jpaJvm.getJdkMedia(), Media.class))
                .setJavaHome(createJavaHomeFromMedia(jdkMedia))
                .setTomcatMedia(jpaJvm.getTomcatMedia() == null ? null : modelMapper.map(jpaJvm.getTomcatMedia(), Media.class));
        return builder.build();
    }

    private String createJavaHomeFromMedia(JpaMedia jdkMedia) {
        if (jdkMedia == null) {
            return "";
        }

        if (jdkMedia.getRemoteDir() != null && jdkMedia.getRootDir() != null){
            return jdkMedia.getRemoteDir() + "/" + jdkMedia.getRootDir();
        } else {
            return "";
        }
    }

    protected Set<Group> createGroups() {
        final Set<Group> groups = new HashSet<>();

        if (jpaJvm.getGroups() != null) {
            for (final JpaGroup jpaGroup : jpaJvm.getGroups()) {
                groups.add(new Group(Identifier.<Group>id(jpaGroup.getId()), jpaGroup.getName()));
            }

        }
        return groups;
    }

}
