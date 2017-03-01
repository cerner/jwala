package com.cerner.jwala.common.jsch;

import com.cerner.jwala.common.exec.RemoteSystemConnection;

/**
 * Defines a rudimentary JSCH service
 *
 * Created by Jedd Cuison on 12/23/2016
 */
public interface JschService {

    /**
     * Executes a command by opening an ssh shell in the remote server
     * @param remoteSystemConnection target server connection details
     * @param command the command to execute
     * @param timeout length of time to wait before timing out
     * @return {@link RemoteCommandReturnInfo}
     */
    RemoteCommandReturnInfo runShellCommand(RemoteSystemConnection remoteSystemConnection, String command, long timeout);

    /**
     * Executes a command through an EXEC type JSch channel
     * @param remoteSystemConnection target server connection details
     * @param command the command to execute
     * @param timeout length of time to wait before timing out
     * @return {@link RemoteCommandReturnInfo}
     */
    RemoteCommandReturnInfo runExecCommand(RemoteSystemConnection remoteSystemConnection, String command, long timeout);

}
