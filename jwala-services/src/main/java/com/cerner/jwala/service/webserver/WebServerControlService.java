package com.cerner.jwala.service.webserver;

import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.exception.CommandFailureException;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser);

    void secureCopyFile(final String aWebServerName, final String sourcePath, final String destPath, String userId) throws CommandFailureException;

    void createDirectory(WebServer webServer, String dirAbsolutePath) throws CommandFailureException;

    void changeFileMode(WebServer webServer, String fileMode, String targetDirPath, String targetFile) throws CommandFailureException;

    boolean waitForState(final ControlWebServerRequest controlWebServerRequest, final Long waitTimeout);
}
