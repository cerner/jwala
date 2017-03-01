package com.cerner.jwala.ws.rest.v1.provider;

import javax.ws.rs.QueryParam;

public class NameSearchParameterProvider {

    @QueryParam("name")
    private String name;

    public NameSearchParameterProvider() {
    }

    public NameSearchParameterProvider(final String aName) {
        name = aName;
    }

    public boolean isNamePresent() {
        return name != null && !"".equals(name.trim());
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NameSearchParameterProvider{" +
               "name='" + name + '\'' +
               '}';
    }
}
