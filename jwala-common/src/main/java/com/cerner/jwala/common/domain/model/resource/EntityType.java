package com.cerner.jwala.common.domain.model.resource;

/**
 * Enumerates possible resource entity types.
 * <p>
 * Created by Jedd Cuison on 3/30/2016.
 */
public enum EntityType {
    JVM("JVM"),
    GROUPED_JVMS("GROUPED_JVMS"),
    WEB_SERVER("WEB_SERVER"),
    GROUPED_WEBSERVERS("GROUPED_WEBSERVERS"),
    APP("APPLICATION"),
    GROUPED_APPS("GROUPED_APPS"),
    EXT_PROPERTIES("EXT_PROPERTIES"),
    UNDEFINED(null);

    final private String entityTypeValue;

    EntityType(final String entityTypeValue) {
        this.entityTypeValue = entityTypeValue;
    }

    public static EntityType fromValue(final String entityTypeValue) {
        for (EntityType entityType : EntityType.values()) {
            if (entityType != UNDEFINED && entityType.entityTypeValue.equalsIgnoreCase(entityTypeValue)) {
                return entityType;
            }
        }
        return UNDEFINED;
    }
}
