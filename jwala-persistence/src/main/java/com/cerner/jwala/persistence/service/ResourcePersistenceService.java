package com.cerner.jwala.persistence.service;

import java.util.List;

/**
 * Created by Eric Pinder on 3/25/2015.
 */
public interface ResourcePersistenceService {

    /**
     * Get's an application's resource names.
     * @param groupName the group where the application belongs to
     * @param appName the application name
     * @return list of resource names
     */
    List<String> getApplicationResourceNames(String groupName, String appName);

    /**
     * Gets an application's resource template.
     * @param groupName the group the application belongs to
     * @param appName the application name
     * @param templateName the template name
     * @return the template
     */
    String getAppTemplate(String groupName, String appName, String templateName);
}
