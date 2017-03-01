package com.cerner.jwala.ws.rest.v1.service.group;

import java.io.Serializable;
import java.util.List;

/**
 * A generic representation of membership details.
 *
 * Created by Jedd Cuison on 10/29/14.
 */
public class MembershipDetails implements Serializable {

    private final String name;
    private final GroupChildType type;
    private final List<String> groupNames;

    /**
     * Membership details constructor
     * @param name the name
     * @param type the entity type {@link GroupChildType} e.g. JVM or WEB_SERVER
     * @param groupNames
     */
    public MembershipDetails(final String name, final GroupChildType type, final List<String> groupNames) {
        this.name  = name;
        this.type = type;
        this.groupNames = groupNames;
    }

    public String getName() {
        return name;
    }

    public GroupChildType getType() {
        return type;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

}