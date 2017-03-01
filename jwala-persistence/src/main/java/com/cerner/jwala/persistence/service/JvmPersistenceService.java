package com.cerner.jwala.persistence.service;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;

import java.util.List;

// TODO: Get rid of this...stick with the DAO
public interface JvmPersistenceService {

    Jvm createJvm(CreateJvmRequest createJvmRequest);

    Jvm updateJvm(UpdateJvmRequest updateJvmRequest, boolean updateJvmPassword);

    Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException;

    JpaJvm getJpaJvm(Identifier<Jvm> aJvmId, boolean fetchGroups);

    List<Jvm> getJvms();

    void removeJvm(final Identifier<Jvm> aJvmId);

    Jvm removeJvmFromGroups(final Identifier<Jvm> aJvmId);

    JpaJvmConfigTemplate uploadJvmConfigTemplate(UploadJvmTemplateRequest uploadJvmTemplateRequest);

    String getJvmTemplate(String templateName, Identifier<Jvm> jvmId);

    // Note: Do we really need a persistence service and a CRUD service ? Can we just have a DAO to make
    //       things simple ? TODO: Discuss this with the team in the future.
    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    Jvm findJvm(String jvmName, String groupName);

    Jvm findJvmByExactName(String jvmName);

    void updateState(Identifier<Jvm> id, JvmState state);

    void updateErrorStatus(Identifier<Jvm> id, String errorSatus);

    void updateState(Identifier<Jvm> id, JvmState state, String errorStatus);

    List<Group> findGroupsByJvm(Identifier<Jvm> id);

    Long getJvmStartedCount(String groupName);

    Long getJvmCount(String groupName);

    Long getJvmStoppedCount(String groupName);

    Long getJvmForciblyStoppedCount(String groupName);

    int removeTemplate(String name);

    int removeTemplate(String jvmName, String templateName);

    List<JpaJvmConfigTemplate> getConfigTemplates(String jvmName);

    /**
     * This service returns a list of JpaJvm objects which belong to a particular group. This method uses the group name to lookup the JpaJvms.
     * @param groupName
     * @return a list of JpaJvm objects
     */
    List<Jvm> getJvmsByGroupName(String groupName);

    /**
     * Get JVMs and applications under them.
     * @param groupName the group name.
     * @return List of {@link Jvm}
     */
    List<Jvm> getJvmsAndWebAppsByGroupName(String groupName);

    /**
     * This method checks if a resource file exists for a jvm.
     * @param groupName This is the of the group under which the jvm should exist
     * @param jvmName This is the name of the jvm for which we check if the resource file exists
     * @param filename This is the name of the resource file that needs to be checked
     * @return true if the file exists, else returns false
     */
    boolean checkJvmResourceFileName(String groupName, String jvmName, String filename);

    /**
     * Get the JVM id
     * @param name the name of the JVM
     * @return the JVM id
     */
    Long getJvmId(String name);

    String updateResourceMetaData(String jvmName, String resourceName, String metaData);
}
