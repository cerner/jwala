package com.cerner.jwala.control.command;

import com.cerner.jwala.common.exec.ExecCommand;

public class DefaultExecCommandBuilderImpl<T> {
    private String entityName;
    private T controlOperation;
    private String[] params;

    public DefaultExecCommandBuilderImpl setEntityName(final String entityName) {
        this.entityName = entityName;
        return this;
    }

    public DefaultExecCommandBuilderImpl setOperation(final T anOperation) {
        controlOperation = anOperation;
        return this;
    }

    public DefaultExecCommandBuilderImpl setParameter(String... aParams) {
        params = aParams;
        return this;
    }

    public ExecCommand build(PlatformCommandProvider<T> provider) {
        //TODO The platform must come from the Web Server in the future (i.e. once it's ready and available)
        final ServiceCommandBuilder builder = provider.getServiceCommandBuilderFor(controlOperation);
        return builder.buildCommandForService(entityName, params);
    }
}