package com.cerner.jwala.common.domain.model.resource;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Resource entity that wraps type, group and target
 *
 * Created by Jedd Cuison on 3/30/2016
 */
public class Entity {
    private final String type;
    private final String group;
    private final String target;
    private final String parentName;
    private final boolean deployToJvms;

    @JsonCreator
    public Entity(@JsonProperty("type") final String type,
                  @JsonProperty("group") final String group,
                  @JsonProperty("target") final String target,
                  @JsonProperty("parentName") final String parentName,
                  @JsonProperty("deployToJvms") final Boolean deployToJvms) {
        this.type = type;
        this.group = group;
        this.target = target;
        this.parentName = parentName;
        this.deployToJvms = deployToJvms == null ? true : deployToJvms;
    }

    public String getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public String getTarget() {
        return target;
    }

    public String getParentName() {
        return parentName;
    }

    public boolean getDeployToJvms() {
        return deployToJvms;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "type='" + type + '\'' +
                ", group='" + group + '\'' +
                ", target='" + target + '\'' +
                ", parentName='" + parentName + '\'' +
                ", deployToJvms=" + deployToJvms +
                '}';
    }
}
