package com.cerner.jwala.common.exception;

import com.cerner.jwala.common.exception.MessageResponseStatus;

final class TestMessageResponseStatus implements MessageResponseStatus {
    @Override
    public String getMessageCode() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}