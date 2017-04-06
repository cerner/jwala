package com.cerner.jwala.service.group.impl.spring.component;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.OperationalState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaWebServer;
import com.cerner.jwala.persistence.jpa.service.GroupCrudService;
import com.cerner.jwala.persistence.jpa.service.JvmCrudService;
import com.cerner.jwala.persistence.jpa.service.WebServerCrudService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.exception.GroupStateNotificationServiceException;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link GroupStateNotificationService} implementation.
 *
 * Created by Jedd Cuison on 3/14/2016.
 */
@Service
public class GroupStateNotificationServiceImpl implements GroupStateNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupStateNotificationServiceImpl.class);

    private final GroupCrudService groupCrudService;

    private final JvmCrudService jvmCrudService;

    private final WebServerCrudService webServerCrudService;

    private final MessagingService messagingService;

    private Object lockObject = new Object();

    private Map<String, CurrentState<Group, OperationalState>> groupStateMap = new HashMap<>();

    public GroupStateNotificationServiceImpl(final GroupCrudService groupCrudService, JvmCrudService jvmCrudService,
                                             final WebServerCrudService webServerCrudService, final MessagingService messagingService) {
        this.groupCrudService = groupCrudService;
        this.jvmCrudService = jvmCrudService;
        this.webServerCrudService = webServerCrudService;
        this.messagingService = messagingService;
        fetchStates(groupCrudService.getGroups(), false);
    }

    @Override
    @Async
    @SuppressWarnings("unchecked")
    public void retrieveStateAndSend(final Identifier id, final Class aClass) {
        LOGGER.debug("Synchronizing on {} and {}...", id, aClass);
        synchronized (lockObject) {
            LOGGER.debug("Thread locked on {} and {}...!", id, aClass);
            final List<JpaGroup> groups;
            if (Jvm.class.getName().equals(aClass.getName())) {
                final JpaJvm jvm = jvmCrudService.getJvm(id);
                groups = jvm.getGroups();
            } else if (WebServer.class.getName().equals(aClass.getName())) {
                final JpaWebServer webServer = webServerCrudService.getWebServerAndItsGroups(id.getId());
                groups = webServer.getGroups();
            } else {
                final String errMsg = "Invalid class parameter: " + aClass.getName() + "!";
                LOGGER.error(errMsg);
                throw new GroupStateNotificationServiceException(errMsg);
            }
            fetchStates(groups, true);
        }
        LOGGER.debug("Thread locked on {} and {} released!", id, aClass);
    }

    /**
     * Fetch the jvm and web server states for a list of groups and save them in a map
     * @param groups the groups whose jvm and web server states are to be retrieved
     * @param send if true, the map is sent to a topic so a client process it
     */
    private void fetchStates(final List<JpaGroup> groups, boolean send) {
        for (final JpaGroup group: groups) {
            final Long jvmStartedCount = jvmCrudService.getJvmStartedCount(group.getName());
            final Long jvmStoppedCount = jvmCrudService.getJvmStoppedCount(group.getName());
            final Long jvmForciblyStoppedCount = jvmCrudService.getJvmForciblyStoppedCount(group.getName());
            final Long jvmCount = jvmCrudService.getJvmCount(group.getName());
            final Long webServerStartedCount = webServerCrudService.getWebServerStartedCount(group.getName());
            final Long webServerStoppedCount = webServerCrudService.getWebServerStoppedCount(group.getName());
            final Long webServerCount = webServerCrudService.getWebServerCount(group.getName());
            final CurrentState<Group, OperationalState> groupState = new CurrentState<>(new Identifier<>(group.getId()),
                    null, DateTime.now(), StateType.GROUP, webServerCount, webServerStartedCount,
                    webServerStoppedCount, jvmCount, jvmStartedCount, jvmStoppedCount, jvmForciblyStoppedCount);

            if (send) {
                messagingService.send(groupState);
            }

            groupStateMap.put(group.getName(), groupState);
        }
    }

    @Override
    public CurrentState<Group, OperationalState> getGroupState(final String groupName) {
        return groupStateMap.get(groupName);
    }

    @Override
    public Map<String, CurrentState<Group, OperationalState>> getGroupStates() {
        return groupStateMap;
    }

}
