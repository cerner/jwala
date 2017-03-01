package com.cerner.jwala.control.command.common;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */

import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;

/**
 * The ApplicationCommand functional interface.<br/>
 */
@FunctionalInterface
public interface RemoteShellCommand<T> {
    public RemoteCommandReturnInfo apply(String host, String... params);
}
