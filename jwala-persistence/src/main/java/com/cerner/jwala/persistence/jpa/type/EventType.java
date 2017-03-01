package com.cerner.jwala.persistence.jpa.type;

import org.apache.commons.lang3.StringUtils;

/**
 * This enum lists events classified by source and severity level
 *
 * Note: Initially the event type would only identify an event by source but later on it became evident that the event
 *       should also be classified by severity level but since adding severity level to
 *       {@link com.cerner.jwala.persistence.jpa.domain.JpaHistory} will require existing Cerner installations to update
 *       their db, a decision was made to have the event type describe both instead. Anyhow this can change in the near
 *       future on subsequent team design/code review.
 *
 * Created by Jedd Cuison on 12/9/2015
 */
public enum EventType {

    USER_ACTION_INFO("UI"), SYSTEM_ERROR("SE"), SYSTEM_INFO("SI"),
    @Deprecated USER_ACTION("A") /* Kept for backward compatibility */,
    @Deprecated APPLICATION_EVENT("E")  /* Kept for backward compatibility */,
    UNKNOWN(StringUtils.EMPTY);

    private final String abbrev;

    EventType(final String abbrev) {
        this.abbrev = abbrev;
    }

    public static EventType fromValue(final String val) {
        for (EventType eventType: values()) {
            if (eventType.abbrev.equalsIgnoreCase(val)) {
                return eventType;
            }
        }
        return UNKNOWN;
    }

    public String toValue() {
        return abbrev;
    }

}
