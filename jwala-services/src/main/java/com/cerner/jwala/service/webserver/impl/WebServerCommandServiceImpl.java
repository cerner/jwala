package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.control.webserver.command.WebServerCommandFactory;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.webserver.WebServerCommandService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Encapsulates non-state altering commands to a web server.
 *
 * Created by Jedd Cuison on 8/25/14.
 */
public class WebServerCommandServiceImpl implements WebServerCommandService {

    @Autowired
    WebServerCommandFactory webServerCommandFactory;

    private final WebServerService webServerService;
    private final SshConfiguration sshConfig;

    private final RemoteCommandExecutorService remoteCommandExecutorService;

    public WebServerCommandServiceImpl(final WebServerService webServerService, final SshConfiguration sshConfig,
                                       final RemoteCommandExecutorService remoteCommandExecutorService) {
        this.webServerService = webServerService;
        this.sshConfig = sshConfig;
        this.remoteCommandExecutorService = remoteCommandExecutorService;
    }


    @Override
    public CommandOutput getHttpdConf(final Identifier<WebServer> webServerId) throws CommandFailureException {
        final WebServer webServer = webServerService.getWebServer(webServerId);
        RemoteCommandReturnInfo remoteCommandReturnInfo = webServerCommandFactory.executeCommand(webServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE);
        return new CommandOutput(new ExecReturnCode(remoteCommandReturnInfo.retCode), remoteCommandReturnInfo.standardOuput,
                remoteCommandReturnInfo.errorOupout);
    }

}
