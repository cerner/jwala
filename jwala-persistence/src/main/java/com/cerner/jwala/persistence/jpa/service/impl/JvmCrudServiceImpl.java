package com.cerner.jwala.persistence.jpa.service.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.domain.builder.JvmBuilder;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateMetaDataUpdateException;
import com.cerner.jwala.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JvmCrudServiceImpl extends AbstractCrudServiceImpl<JpaJvm> implements JvmCrudService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmCrudServiceImpl.class);

    @PersistenceContext(unitName = "jwala-unit")
    private EntityManager entityManager;

    @Override
    public JpaJvm createJvm(CreateJvmRequest createJvmRequest, JpaMedia jdkMedia) {

        try {
            final JpaJvm jpaJvm = new JpaJvm();

            jpaJvm.setName(createJvmRequest.getJvmName());
            jpaJvm.setHostName(createJvmRequest.getHostName());
            jpaJvm.setHttpPort(createJvmRequest.getHttpPort());
            jpaJvm.setHttpsPort(createJvmRequest.getHttpsPort());
            jpaJvm.setRedirectPort(createJvmRequest.getRedirectPort());
            jpaJvm.setShutdownPort(createJvmRequest.getShutdownPort());
            jpaJvm.setAjpPort(createJvmRequest.getAjpPort());
            jpaJvm.setStatusPath(createJvmRequest.getStatusPath().getPath());
            jpaJvm.setSystemProperties(createJvmRequest.getSystemProperties());
            jpaJvm.setUserName(createJvmRequest.getUserName());
            jpaJvm.setEncryptedPassword(createJvmRequest.getEncryptedPassword());
            jpaJvm.setJdkMedia(jdkMedia);
//            jpaJvm.setTomcatMedia(createJvmRequest.getTomcatMediaId());

            return create(jpaJvm);
        } catch (final EntityExistsException eee) {
            LOGGER.error("Error creating JVM for request {}", createJvmRequest, eee);
            throw new EntityExistsException("JVM with name already exists: " + createJvmRequest, eee);
        }
    }

    @Override
    public JpaJvm updateJvm(final UpdateJvmRequest updateJvmRequest, final boolean updateJvmPassword, final JpaMedia jdkMedia) {

        try {
            final Identifier<Jvm> jvmId = updateJvmRequest.getId();
            final JpaJvm jpaJvm = getJvm(jvmId);

            jpaJvm.setName(updateJvmRequest.getNewJvmName());
            jpaJvm.setHostName(updateJvmRequest.getNewHostName());
            jpaJvm.setHttpPort(updateJvmRequest.getNewHttpPort());
            jpaJvm.setHttpsPort(updateJvmRequest.getNewHttpsPort());
            jpaJvm.setRedirectPort(updateJvmRequest.getNewRedirectPort());
            jpaJvm.setShutdownPort(updateJvmRequest.getNewShutdownPort());
            jpaJvm.setAjpPort(updateJvmRequest.getNewAjpPort());
            jpaJvm.setStatusPath(updateJvmRequest.getNewStatusPath().getPath());
            jpaJvm.setSystemProperties(updateJvmRequest.getNewSystemProperties());
            jpaJvm.setUserName(updateJvmRequest.getNewUserName());

            if (updateJvmPassword) {
                jpaJvm.setEncryptedPassword(updateJvmRequest.getNewEncryptedPassword());
            }

            jpaJvm.setJdkMedia(jdkMedia);
//            jpaJvm.setTomcatMedia(updateJvmRequest.getNewTomcatMediaId());

            return update(jpaJvm);
        } catch (final EntityExistsException eee) {
            LOGGER.error("Error updating JVM for request {}", updateJvmRequest, eee);
            throw new EntityExistsException("JVM with name already exists: " + updateJvmRequest, eee);
        }
    }

    @Override
    public JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {
        final JpaJvm jvm = findById(aJvmId.getId());

        if (jvm == null) {
            LOGGER.error("Error getting JVM for ID {}", aJvmId);
            throw new NotFoundException(FaultType.JVM_NOT_FOUND,
                    "Jvm not found: " + aJvmId);
        }

        return jvm;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaJvm> getJvms() {
        return findAll();
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        remove(getJvm(aJvmId));
    }

    @Override
    public JpaJvmConfigTemplate uploadJvmConfigTemplate(UploadJvmTemplateRequest uploadJvmTemplateRequest) {

        final Jvm jvm = uploadJvmTemplateRequest.getJvm();
        Identifier<Jvm> id = jvm.getId();
        final JpaJvm jpaJvm = getJvm(id);

        String templateContent = uploadJvmTemplateRequest.getTemplateContent();

        // get an instance and then do a create or update
        Query query = entityManager.createQuery("SELECT t FROM JpaJvmConfigTemplate t where t.templateName = :tempName and t.jvm = :jpaJvm");
        query.setParameter("jpaJvm", jpaJvm);
        query.setParameter("tempName", uploadJvmTemplateRequest.getConfFileName());
        List<JpaJvmConfigTemplate> templates = query.getResultList();
        JpaJvmConfigTemplate jpaConfigTemplate;
        final String metaData = uploadJvmTemplateRequest.getMetaData();
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            //create
            jpaConfigTemplate = new JpaJvmConfigTemplate();
            jpaConfigTemplate.setJvm(jpaJvm);
            jpaConfigTemplate.setTemplateName(uploadJvmTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            LOGGER.error("Error uploading JVM template for request {}", uploadJvmTemplateRequest);
            throw new BadRequestException(FaultType.JVM_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for JVM [" + uploadJvmTemplateRequest+ "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;
    }

    @Override
    public String getJvmTemplate(String templateName, Identifier<Jvm> jvmId) {
        JpaJvm jpaJvm = getJvm(jvmId);
        Query query = entityManager.createQuery("SELECT t FROM JpaJvmConfigTemplate t where t.templateName = :tempName and t.jvm = :jpaJvm");
        query.setParameter("jpaJvm", jpaJvm);
        query.setParameter("tempName", templateName);
        List<JpaJvmConfigTemplate> templates = query.getResultList();
        if (templates.size() == 1) {
            return templates.get(0).getTemplateContent();
        } else if (templates.isEmpty()) {
            return "";
        } else {
            LOGGER.error("Error getting JVM template {} for JVM ID {}", templateName, jvmId);
            throw new BadRequestException(FaultType.JVM_TEMPLATE_NOT_FOUND,
                    "Only expecting one " + templateName + " template to be returned for JVM [" + jvmId + "] but returned " + templates.size() + " templates");
        }
    }

    @Override
    public List<String> getResourceTemplateNames(String jvmName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_RESOURCE_TEMPLATE_NAMES);
        q.setParameter("jvmName", jvmName);
        return q.getResultList();
    }

    @Override
    public String getResourceTemplate(final String jvmName, final String resourceTemplateName) throws NonRetrievableResourceTemplateContentException {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_TEMPLATE_CONTENT);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting resource template {} for JVM {}", resourceTemplateName, jvmName, re);
            throw new NonRetrievableResourceTemplateContentException(jvmName, resourceTemplateName, re);
        }
    }

    @Override
    public String getResourceTemplateMetaData(String jvmName, String fileName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_TEMPLATE_META_DATA);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", fileName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.error("Error getting resource meta data {} for JVM {}", fileName, jvmName, re);
            throw new NonRetrievableResourceTemplateContentException(jvmName, fileName, re);
        }
    }

    @Override
    public void updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_CONTENT);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating resource template {} for JVM {}", resourceTemplateName, jvmName, re);
            throw new ResourceTemplateUpdateException(jvmName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating resource template numEntities=0 {} for JVM {}", resourceTemplateName, jvmName);
            throw new ResourceTemplateUpdateException(jvmName, resourceTemplateName);
        }
    }

    @Override
    public void updateResourceMetaData(final String jvmName, final String resourceTemplateName, final String metaData) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_META_DATA);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("metaData", metaData);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.error("Error updating resource meta data {} for JVM {}", resourceTemplateName, jvmName, re);
            throw new ResourceTemplateMetaDataUpdateException(jvmName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            LOGGER.error("Error updating resource meta data {} for JVM {}", resourceTemplateName, jvmName);
            throw new ResourceTemplateMetaDataUpdateException(jvmName, resourceTemplateName);
        }
    }

    /**
     * @param jvmName
     * @param groupName
     * @return
     */
    @Override
    public Jvm findJvm(final String jvmName, final String groupName) {
        Jvm jvm = null;
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME);
        q.setParameter("jvmName", jvmName);
        q.setParameter("groupName", groupName);
        try {
            JpaJvm jpaJvm = (JpaJvm) q.getSingleResult();
            jvm = new JvmBuilder(jpaJvm).build();
        } catch (NoResultException e) {
            LOGGER.error("error with getting result for jvmName: {} and groupName {}, error: {}", jvmName, groupName, e);
        }
        return jvm;
    }

    @Override
    public Jvm findJvmByExactName(String jvmName) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j WHERE j.name=:jvmName ORDER BY j.name");
        query.setParameter("jvmName", jvmName);
        return new JvmBuilder((JpaJvm) query.getSingleResult()).build();
    }

    @Override
    public Long getJvmStartedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, JvmState.JVM_STARTED);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, JvmState.JVM_STOPPED);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getJvmCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public int updateState(final Identifier<Jvm> id, final JvmState state) {
        // Normally we would load the JpaJvm then set the states but I reckon running an UPDATE query would be faster since
        // it's only one transaction vs 2 (find and update).
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_UPDATE_STATE_BY_ID);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, state);
        query.setParameter(JpaJvm.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public int updateErrorStatus(final Identifier<Jvm> id, final String errorStatus) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_UPDATE_ERROR_STATUS_BY_ID);
        query.setParameter(JpaJvm.QUERY_PARAM_ERROR_STATUS, errorStatus);
        query.setParameter(JpaJvm.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public int updateState(final Identifier<Jvm> id, final JvmState state, final String errorStatus) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, state);
        query.setParameter(JpaJvm.QUERY_PARAM_ERROR_STATUS, errorStatus);
        query.setParameter(JpaJvm.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, JvmState.FORCED_STOPPED);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public int removeTemplate(final String name) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_TEMPLATE);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, name);
        return q.executeUpdate();
    }

    @Override
    public int removeTemplate(final String jvmName, final String templateName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCE_BY_TEMPLATE_JVM_NAME);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return q.executeUpdate();
    }

    @Override
    public List<JpaJvmConfigTemplate> getConfigTemplates(final String jvmName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.QUERY_GET_JVM_RESOURCE_TEMPLATES);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return q.getResultList();
    }

    @Override
    public List<Jvm> getJvmsByGroupName(String groupName) {
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVMS_BY_GROUP_NAME);
        q.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        List<Jvm> listOfJvms=buildJvms(q.getResultList());
        listOfJvms.sort(new Comparator<Jvm>() {
            @Override
            public int compare(Jvm jvm1, Jvm jvm2) {
                return jvm1.getJvmName().compareTo(jvm2.getJvmName());
            }
        });
        return listOfJvms;
    }

    /**
     * Build the JVM list.
     *
     * @param jpaJvms {@link JpaJvm}
     * @return The JVM list. Returns an empty list if there are no JVMs.
     */
    private List<Jvm> buildJvms(List<JpaJvm> jpaJvms) {
        final List<Jvm> jvms = new ArrayList<>(jpaJvms.size());
        for (final JpaJvm jpaJvm : jpaJvms) {
            jvms.add(new JvmBuilder(jpaJvm).build());
        }
        return jvms;
    }

    @Override
    public boolean checkJvmResourceFileName(final String groupName, final String jvmName, final String fileName) {
        final Jvm jvm = findJvm(jvmName, groupName);
        if (jvm != null) {
            final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_TEMPLATE_RESOURCE_NAME);
            q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
            q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, fileName);
            List<String> result = q.getResultList();
            if (result != null && result.size() == 1) {
                return true;
            }
        }
        return false;
    }
}
