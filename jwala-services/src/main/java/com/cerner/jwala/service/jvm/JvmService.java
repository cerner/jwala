package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.common.request.jvm.CreateJvmAndAddToGroupsRequest;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.service.jvm.impl.JvmHttpRequestResult;

import java.util.List;

public interface JvmService {

	Jvm createJvm(CreateJvmAndAddToGroupsRequest createJvmAndAddToGroupsRequest, User user);

	Jvm getJvm(final Identifier<Jvm> aJvmId);

	Jvm getJvm(final String jvmName);

	List<Jvm> getJvms();

	Jvm updateJvm(final UpdateJvmRequest updateJvmRequest, boolean updateJvmPassword);

	void deleteJvm(Identifier<Jvm> id, boolean hardDelete, User user);

	Jvm generateAndDeployJvm(String jvmName, User user);

	Jvm generateAndDeployFile(String jvmName, String fileName, User user);

	void performDiagnosis(Identifier<Jvm> aJvmId, User user);

	List<String> getResourceTemplateNames(final String jvmName);

	String getResourceTemplate(final String jvmName, final String resourceTemplateName, final boolean tokensReplaced);

	String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template);

	String previewResourceTemplate(String fileName, String jvmName, String groupName, String template);

	/**
	 * Update state of JVM
	 * 
	 * @param id    JVM id
	 * @param state JVM State
	 */
	public void updateState(Identifier<Jvm> id, JvmState state);

	/**
	 * Ping's the JVM and updates its state.
	 *
	 * @param jvm the JVM
	 */
	JvmHttpRequestResult pingAndUpdateJvmState(Jvm jvm);

	/**
	 * Deploy application context xml for JVM's
	 * 
	 * @param jvm  JVM
	 * @param user User
	 */
	void deployApplicationContextXMLs(Jvm jvm, User user);

	/**
	 * get Get the count of JVM's which are started
	 * 
	 * @param groupName
	 * @return
	 */
	public Long getJvmStartedCount(String groupName);

	/**
	 * Get the jvm count for a group
	 * 
	 * @param groupName
	 * @return
	 */
	public Long getJvmCount(String groupName);

	/**
	 * Get the count of stopped JVM's
	 * 
	 * @param groupName
	 * @return
	 */
	public Long getJvmStoppedCount(String groupName);

	/**
	 * Get the count of forcibly stopped JVM's
	 * 
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
	 * Delete a JVM
	 * 
	 * @param name     the name of the JVM to delete
	 * @param userName
	 */
	void deleteJvm(String name, String userName);

	/**
	 * Return the JVMs belonging to a group
	 * 
	 * @param name the name of the group of the JVMs
	 * @return a collection of JVMs belonging to the group
	 */
	List<Jvm> getJvmsByGroupName(String name);

	/**
	 * Upgrades the JDK of the mentioned JVM
	 * 
	 * @param jvmName
	 * @param user
	 * @return
	 */
	Jvm upgradeJDK(String jvmName, User user);
}
