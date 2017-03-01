package com.cerner.jwala.control.jvm.command;

/**
 * Created by Arvindo Kinny on 12/22/2016.
 */

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;

/**
 * The JvmCommand functional interface.<br/>
 */
@FunctionalInterface
public interface JvmCommand<T> {
    public RemoteCommandReturnInfo execute(Jvm jvm);
}
