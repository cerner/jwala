package com.cerner.jwala.persistence.jpa.service;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;

import java.util.List;

public interface JvmCrudService extends CrudService<JpaJvm> {

    JpaJvm createJvm(CreateJvmRequest createJvmRequest, JpaMedia jdkMedia);

    JpaJvm updateJvm(UpdateJvmRequest updateJvmRequest, boolean updateJvmPassword, JpaMedia jdkMedia);

    JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    List<JpaJvm> getJvms();

    void removeJvm(final Identifier<Jvm> aGroupId);

    JpaJvmConfigTemplate uploadJvmConfigTemplate(UploadJvmTemplateRequest uploadJvmTemplateRequest);

    String getJvmTemplate(String templateName, Identifier<Jvm> jvmId);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName) throws NonRetrievableResourceTemplateContentException;

    void updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    Jvm findJvm(String jvmName, String groupName);

    int updateState(Identifier<Jvm> id, JvmState state);

    int updateErrorStatus(Identifier<Jvm> id, String errorStatus);

    int updateState(Identifier<Jvm> id, JvmState state, String errorStatus);

    Jvm findJvmByExactName(String jvmName);

    Long getJvmStartedCount(String groupName);

    Long getJvmCount(String groupName);

    Long getJvmStoppedCount(String groupName);

    Long getJvmForciblyStoppedCount(String groupName);

    int removeTemplate(String name);

    @Deprecated
    int removeTemplate(String jvmName, String templateName);

    List<JpaJvmConfigTemplate> getConfigTemplates(String jvmName);

    List<Jvm> getJvmsByGroupName(String groupName);

    String getResourceTemplateMetaData(String jvmName, String fileName);

    /**
     * This method checks if the jvm template contains a file name/template name.
     * @param groupName name of the group in which the jvm needs to exist in
     * @param jvmName name of the jvm in which the file needs to be searched in
     * @param fileName name of the file to be searched
     * @return true if the file exists else false
     */
    boolean checkJvmResourceFileName(String groupName, String jvmName, String fileName);

    void updateResourceMetaData(String jvmName, String resourceTemplateName, String metaData);
}
