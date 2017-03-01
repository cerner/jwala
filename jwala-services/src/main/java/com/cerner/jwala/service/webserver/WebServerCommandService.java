package com.cerner.jwala.service.webserver;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

/**
 * Defines non-state altering commands to a web server.
 * <p/>
 * Created by Jedd Cuison on 8/25/14.
 */
public interface WebServerCommandService {

    CommandOutput getHttpdConf(Identifier<WebServer> webServerId) throws CommandFailureException;

}
