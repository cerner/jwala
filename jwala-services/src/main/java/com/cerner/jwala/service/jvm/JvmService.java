package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;

import java.util.List;


public interface JvmService {

    Jvm createJvm(CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, User user);

    Jvm getJvm(final Identifier<Jvm> aJvmId);

    Jvm getJvm(final String jvmName);

    List<Jvm> getJvms();

    Jvm updateJvm(final UpdateJvmRequest updateJvmRequest, boolean updateJvmPassword);

    void removeJvm(final Identifier<Jvm> aJvmId, User user);

    void deleteJvmService(ControlJvmRequest controlJvmRequest, Jvm jvm, User user);

    Jvm generateAndDeployJvm(String jvmName, User user);

    Jvm generateAndDeployFile(String jvmName, String fileName, User user);

    void performDiagnosis(Identifier<Jvm> aJvmId, User user);

    List<String> getResourceTemplateNames(final String jvmName);

    String getResourceTemplate(final String jvmName, final String resourceTemplateName, final boolean tokensReplaced);

    String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

    String previewResourceTemplate(String fileName, String jvmName, String groupName, String template);

    /**
     * Update state of JVM
     * @param id JVM id
     * @param state JVM State
     */
    public void updateState(Identifier<Jvm> id, JvmState state);

    /**
     * Ping's the JVM and updates its state.
     *
     * @param jvm the JVM
     */
    void pingAndUpdateJvmState(Jvm jvm);

    /**
     * Deploy application context xml for JVM's
     * @param jvm JVM
     * @param user User
     */
    void deployApplicationContextXMLs(Jvm jvm, User user);

    /**
     * get Get the count of JVM's which are started
     * @param groupName
     * @return
     */
    public Long getJvmStartedCount(String groupName);

    /**
     * Get the jvm count for a group
     * @param groupName
     * @return
     */
    public Long getJvmCount(String groupName);

    /**
     * Get the count of stopped JVM's
     * @param groupName
     * @return
     */
    public Long getJvmStoppedCount(String groupName);

    /**
     * Get the count of forcibly stopped JVM's
     * @param groupName
     * @return
     */
    public Long getJvmForciblyStoppedCount(String groupName);

    /**
     * Create JVM default templates.
     *
     * @param jvmName
     * @param parentGroup
     */
    void createDefaultTemplates(String jvmName, Group parentGroup);

    /**
     * Check for setEnv Script
     * @param jvmName
     */
    void checkForSetenvScript(String jvmName);

    /**
     * Delete a JVM
     * @param name the name of the JVM to delete
     * @param userName
     */
    void deleteJvm(String name, String userName);

}
