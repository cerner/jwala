package com.cerner.jwala.persistence.service.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.domain.builder.JvmBuilder;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.ApplicationCrudService;
import com.cerner.jwala.persistence.jpa.service.GroupJvmRelationshipService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class JpaJvmPersistenceServiceImpl implements JvmPersistenceService {

    @Autowired
    private MediaDao mediaDao;

    private final JvmCrudService jvmCrudService;
    private final ApplicationCrudService applicationCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @PersistenceContext(unitName = "jwala-unit")
    private EntityManager em; // Going forward we will be phasing out JvmCrudService thus the reason this is defined here

    public JpaJvmPersistenceServiceImpl(final JvmCrudService jvmCrudService,
                                        final ApplicationCrudService applicationCrudService,
                                        final GroupJvmRelationshipService groupJvmRelationshipService) {
        this.jvmCrudService = jvmCrudService;
        this.applicationCrudService = applicationCrudService;
        this.groupJvmRelationshipService = groupJvmRelationshipService;
    }

    @Override
    public Jvm createJvm(CreateJvmRequest createJvmRequest) {
        JpaMedia jdkMedia = null;
        if (createJvmRequest.getJdkMediaId() != null ) {
            jdkMedia = mediaDao.findById(createJvmRequest.getJdkMediaId().getId());
        }
//        Media tomcatMedia = mediaDaoImpl.find(createJvmRequest.getTomcatMediaId());
        final JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest, jdkMedia);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm updateJvm(UpdateJvmRequest updateJvmRequest, boolean updateJvmPassword) {
        JpaMedia jdkMedia = null;
        if (updateJvmRequest.getNewJdkMediaId() != null ) {
            jdkMedia = mediaDao.findById(updateJvmRequest.getNewJdkMediaId().getId());
        }
//        Media tomcatMedia = mediaDaoImpl.find(createJvmRequest.getTomcatMediaId());
        final JpaJvm jpaJvm = jvmCrudService.updateJvm(updateJvmRequest, updateJvmPassword, jdkMedia);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {
        final JpaJvm jpaJvm = jvmCrudService.getJvm(aJvmId);
        return jvmFrom(jpaJvm);
    }

    @Override
    public JpaJvm getJpaJvm(final Identifier<Jvm> aJvmId, final boolean fetchGroups) {
        final JpaJvm jvm = jvmCrudService.getJvm(aJvmId);
        if (fetchGroups) {
            // groups are lazy loaded so we need this.
            jvm.getGroups().size();
        }
        return jvm;
    }

    @Override
    public List<Jvm> getJvms() {
        return jvmsFrom(jvmCrudService.getJvms());
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        groupJvmRelationshipService.removeRelationshipsForJvm(aJvmId);
        jvmCrudService.removeJvm(aJvmId);
    }

    @Override
    public Jvm removeJvmFromGroups(final Identifier<Jvm> aJvmId) {
        groupJvmRelationshipService.removeRelationshipsForJvm(aJvmId);
        return getJvm(aJvmId);
    }

    @Override
    public JpaJvmConfigTemplate uploadJvmConfigTemplate(UploadJvmTemplateRequest uploadJvmTemplateRequest) {
        return jvmCrudService.uploadJvmConfigTemplate(uploadJvmTemplateRequest);
    }

    @Override
    public String getJvmTemplate(String templateName, Identifier<Jvm> jvmId) {
        return jvmCrudService.getJvmTemplate(templateName, jvmId);
    }

    @Override
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmCrudService.getResourceTemplateNames(jvmName);
    }

    @Override
    public String getResourceTemplate(final String jvmName, final String resourceTemplateName) {
        return jvmCrudService.getResourceTemplate(jvmName, resourceTemplateName);
    }

    @Override
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        jvmCrudService.updateResourceTemplate(jvmName, resourceTemplateName, template);
        return jvmCrudService.getResourceTemplate(jvmName, resourceTemplateName);
    }

    @Override
    public String updateResourceMetaData(final String jvmName, final String resourceTemplateName, final String metaData) {
        jvmCrudService.updateResourceMetaData(jvmName, resourceTemplateName, metaData);
        return jvmCrudService.getResourceTemplateMetaData(jvmName, resourceTemplateName);
    }

    @Override
    public Jvm findJvm(final String jvmName, final String groupName) {
        return jvmCrudService.findJvm(jvmName, groupName);
    }

    @Override
    public Jvm findJvmByExactName(String jvmName) {
        return jvmCrudService.findJvmByExactName(jvmName);
    }

    @Override
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        jvmCrudService.updateState(id, state);
    }

    @Override
    public void updateErrorStatus(final Identifier<Jvm> id, final String errorStatus) {
        jvmCrudService.updateErrorStatus(id, errorStatus);
    }

    @Override
    @Transactional
    public void updateState(final Identifier<Jvm> id, final JvmState state, final String errorStatus) {
        jvmCrudService.updateState(id, state, errorStatus);
    }

    @Override
    public List<Group> findGroupsByJvm(Identifier<Jvm> id) {
        return groupJvmRelationshipService.findGroupsByJvm(id);
    }

    @Override
    public Long getJvmStartedCount(final String groupName) {
        return jvmCrudService.getJvmStartedCount(groupName);
    }

    @Override
    public Long getJvmCount(final String groupName) {
        return jvmCrudService.getJvmCount(groupName);
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        return jvmCrudService.getJvmStoppedCount(groupName);
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        return jvmCrudService.getJvmForciblyStoppedCount(groupName);
    }

    protected Jvm jvmFrom(final JpaJvm aJpaJvm) {
        return new JvmBuilder(aJpaJvm).build();
    }

    protected List<Jvm> jvmsFrom(final List<JpaJvm> someJpaJvms) {
        final List<Jvm> jvms = new ArrayList<>();
        for (final JpaJvm jpaJvm : someJpaJvms) {
            jvms.add(jvmFrom(jpaJvm));
        }
        return jvms;
    }

    @Override
    public int removeTemplate(final String name) {
        return jvmCrudService.removeTemplate(name);
    }

    @Override
    public int removeTemplate(final String jvmName, final String templateName) {
        return jvmCrudService.removeTemplate(jvmName, templateName);
    }

    @Override
    public List<JpaJvmConfigTemplate> getConfigTemplates(final String jvmName) {
        return jvmCrudService.getConfigTemplates(jvmName);
    }

    @Override
    public List<Jvm> getJvmsByGroupName(final String groupName) {
        return jvmCrudService.getJvmsByGroupName(groupName);
    }

    @Override
    public List<Jvm> getJvmsAndWebAppsByGroupName(final String groupName) {
        final List<Jvm> jvms = jvmCrudService.getJvmsByGroupName(groupName);
        final List<Jvm> jvmsWithWebApps = new ArrayList<>();
        final com.cerner.jwala.common.domain.model.jvm.JvmBuilder jvmBuilder = new com.cerner.jwala.common.domain.model.jvm.JvmBuilder();
        for (Jvm jvm : jvms) {
            final List<Application> webApps = applicationCrudService.findApplicationsBelongingToJvm(jvm.getId());
            // TODO: Decide whether to use a builder or have a setter just to set the applications ?
            final Jvm jvmWithWebApps = jvmBuilder.setId(jvm.getId())
                                                 .setName(jvm.getJvmName())
                                                 .setHostName(jvm.getHostName())
                                                 .setStatusPath(jvm.getStatusPath())
                                                 .setGroups(jvm.getGroups())
                                                 .setHttpPort(jvm.getHttpPort())
                                                 .setHttpsPort(jvm.getHttpsPort())
                                                 .setRedirectPort(jvm.getRedirectPort())
                                                 .setShutdownPort(jvm.getShutdownPort())
                                                 .setAjpPort(jvm.getAjpPort())
                                                 .setSystemProperties(jvm.getSystemProperties())
                                                 .setState(jvm.getState())
                                                 .setErrorStatus(jvm.getErrorStatus())
                                                 .setUserName(jvm.getUserName())
                                                 .setEncryptedPassword(jvm.getEncryptedPassword())
                                                 .setWebApps(webApps)
                                                 .setJavaHome(jvm.getJavaHome())
                                                 .build();
            jvmsWithWebApps.add(jvmWithWebApps);
        }
        return jvmsWithWebApps;
    }

    @Override
    public boolean checkJvmResourceFileName(final String groupName, final String jvmName, final String fileName) {
        return jvmCrudService.checkJvmResourceFileName(groupName, jvmName, fileName);
    }

    @Override
    public Long getJvmId(final String name) {
        final Query q =  em.createNamedQuery(JpaJvm.QUERY_GET_JVM_ID);
        q.setParameter(JpaJvm.QUERY_PARAM_NAME, name);
        try {
            return (Long) q.getSingleResult();
        } catch (final NoResultException e) {
            logger.error("Failed to query the jvm id where name = {}", name, e);
            return null;
        }
    }
}
