package com.cerner.jwala.service;

import org.mockito.ArgumentMatcher;

import com.cerner.jwala.common.request.Request;

public class CommandMatcher<T> extends ArgumentMatcher<T> {

    private final Request expectedRequest;

    public CommandMatcher(final Request theExpectedRequest) {
        expectedRequest = theExpectedRequest;
    }

    @Override
    public boolean matches(final Object argument) {
        return expectedRequest.equals(argument);
    }
}
