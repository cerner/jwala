package com.cerner.jwala.control.webserver.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;

/**
 * The WebServer Command functional interface.<br/>
 */
@FunctionalInterface
public interface WebServerCommand<T> {
    public RemoteCommandReturnInfo apply(WebServer webserver);
}
