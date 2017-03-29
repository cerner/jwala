package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.ControlGroupJvmRequest;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.service.group.GroupJvmControlService;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmControlService;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupJvmControlServiceImpl implements GroupJvmControlService {

    private final GroupService groupService;
    private final JvmControlService jvmControlService;
    private final ExecutorService executorService;
    private final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupJvmControlServiceImpl.class);

    public GroupJvmControlServiceImpl(final GroupService theGroupService, final JvmControlService theJvmControlService) {
        groupService = theGroupService;
        jvmControlService = theJvmControlService;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("thread-task-executor.group-control.pool.size", "25")));
    }

    @Transactional
    @Override
    public void controlGroup(final ControlGroupJvmRequest controlGroupJvmRequest, final User aUser) {

        controlGroupJvmRequest.validate();

        Group group = groupService.getGroup(controlGroupJvmRequest.getGroupId());

        final Set<Jvm> jvms = group.getJvms();
        if (jvms != null) {
            controlJvms(controlGroupJvmRequest, aUser, jvms);
        }
    }

    @Override
    public void controlAllJvms(final ControlGroupJvmRequest controlGroupJvmRequest, final User user) {
        Set<Jvm> jvms = new TreeSet<>(new JvmTreeSet());
        for (Group group : groupService.getGroups()) {
            Set<Jvm> groupsJvms = group.getJvms();
            if (groupsJvms != null && !groupsJvms.isEmpty()) {
                jvms.addAll(groupsJvms);
            }
        }
        LOGGER.info("jvm size to perform ControlJvms: " + jvms.size());
        controlJvms(controlGroupJvmRequest, user, jvms);
    }

    private void controlJvms(final ControlGroupJvmRequest controlGroupJvmRequest, final User user, Set<Jvm> jvms) {
        for (final Jvm jvm : jvms) {
            if (!checkSameState(controlGroupJvmRequest.getControlOperation(), jvm.getState())) {
                executorService.submit(new Callable<CommandOutput>() {
                    @Override
                    public CommandOutput call() throws Exception {
                        ControlJvmRequest controlJvmRequest = new ControlJvmRequest(jvm.getId(), controlGroupJvmRequest.getControlOperation());
                        return jvmControlService.controlJvm(controlJvmRequest, user);
                    }
                });
            }
        }
    }

    public boolean checkSameState(final JvmControlOperation jvmControlOperation, final JvmState jvmState) {
        return (jvmControlOperation.START.toString().equals(jvmControlOperation.name()) && jvmState.isStartedState()) ||
                (jvmControlOperation.STOP.toString().equals(jvmControlOperation.name()) && !jvmState.isStartedState()) ? true : false;
    }
}

class JvmTreeSet implements Comparator<Jvm> {
    @Override
    public int compare(Jvm jvm1, Jvm jvm2) {
        return jvm1.getJvmName().compareToIgnoreCase(jvm2.getJvmName());
    }
}
