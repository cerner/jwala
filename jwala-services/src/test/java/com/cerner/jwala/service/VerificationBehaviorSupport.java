package com.cerner.jwala.service;

import java.util.HashSet;
import java.util.Set;

import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.request.group.AddJvmToGroupRequest;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

public class VerificationBehaviorSupport {

    protected Set<AddJvmToGroupRequest> createMockedAddRequests(final int aNumberToCreate) {

        final Set<AddJvmToGroupRequest> commands = new HashSet<>(aNumberToCreate);

        for (int i = 0; i < aNumberToCreate; i++) {
            commands.add(mock(AddJvmToGroupRequest.class));
        }

        return commands;
    }

    protected <T extends Request> T matchCommand(final T aCommand) {
        return argThat(new CommandMatcher<T>(aCommand));
    }
}
