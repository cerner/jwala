package com.cerner.jwala.persistence.service.resource;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.group.CreateGroupRequest;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.ResourcePersistenceService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static junit.framework.Assert.assertTrue;

@Transactional
public abstract class AbstractResourcePersistenceServiceTest {

    public static final String GROUP_TEST_RESOURCE_PERSISTENCE = "group-testResourcePersistence";
    public static final String APP_TEST_RESOURCE_PERSISTENCE = "app-testResourcePersistence";
    public static final String APP_CONTEXT_XML = "app-context.xml";
    @Autowired
    private ResourcePersistenceService resourcePersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    @Test
    public void testGetApplicationResourceNames() {
        List<String> result = resourcePersistenceService.getApplicationResourceNames(GROUP_TEST_RESOURCE_PERSISTENCE, APP_TEST_RESOURCE_PERSISTENCE);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAppTemplate() {
        // create the group
        CreateGroupRequest createGroupRequest = new CreateGroupRequest(GROUP_TEST_RESOURCE_PERSISTENCE);
        Group group = groupPersistenceService.createGroup(createGroupRequest);

        // create the app
        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest(group.getId(), APP_TEST_RESOURCE_PERSISTENCE, "/app-context", true, true, false);
        Application app = applicationPersistenceService.createApplication(createApplicationRequest);

        // add the template to the app at the group level
        groupPersistenceService.populateGroupAppTemplate(GROUP_TEST_RESOURCE_PERSISTENCE, APP_TEST_RESOURCE_PERSISTENCE, APP_CONTEXT_XML, "{}", "<root/>");

        String result = resourcePersistenceService.getAppTemplate(GROUP_TEST_RESOURCE_PERSISTENCE, APP_TEST_RESOURCE_PERSISTENCE, APP_CONTEXT_XML);
        assertTrue(!result.isEmpty());
    }
    
}
