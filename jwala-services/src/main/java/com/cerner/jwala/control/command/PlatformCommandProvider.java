package com.cerner.jwala.control.command;

public interface PlatformCommandProvider<T> {

    ServiceCommandBuilder getServiceCommandBuilderFor(final T anOperation);
}
