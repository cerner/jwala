package com.cerner.jwala.persistence.jpa.domain;

import com.cerner.jwala.common.domain.model.group.History;
import com.cerner.jwala.persistence.jpa.type.EventType;

import javax.persistence.*;

@Entity
@Table(name = "history", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@NamedQueries({
    @NamedQuery(name = JpaHistory.QRY_GET_HISTORY_BY_GROUP_NAME,
                query = "SELECT h FROM JpaHistory h WHERE h.group.name = :groupName ORDER BY h.id DESC"),
    @NamedQuery(name = JpaHistory.QRY_GET_HISTORY_BY_GROUP_NAME_AND_SERVER_NAME,
                query = "SELECT h FROM JpaHistory h WHERE h.group.name = :groupName AND h.serverName = :serverName ORDER BY h.id DESC")
})
public class JpaHistory extends AbstractEntity<JpaHistory> {

    public static final String QRY_GET_HISTORY_BY_GROUP_NAME = "getHistoryByGroupName";
    public static final String QRY_GET_HISTORY_BY_GROUP_NAME_AND_SERVER_NAME = "getHistoryByGroupNameAndServerName";
    private static final int MAX_EVENT_LEN = 100000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    public String serverName;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private JpaGroup group;

    @Column(length = MAX_EVENT_LEN)
    private String event;

    @Column(name = "EVENTTYPE", length = 2)
    private String eventTypeValue;

    public JpaHistory() {}

    public JpaHistory(final String serverName, final JpaGroup group, final String event, final EventType eventType,
                      final String user) {
        this.serverName = serverName;
        this.group = group;
        this.event = event;
        this.eventTypeValue = eventType.toValue();
        this.createBy = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public JpaGroup getGroup() {
        return group;
    }

    public void setGroup(final JpaGroup group) {
        this.group = group;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(final String event) {
        this.event = event;
    }

    public EventType getEventType() {
        return EventType.fromValue(eventTypeValue);
    }

    public void setEventType(final EventType eventType) {
        this.eventTypeValue = eventType.toValue();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final JpaHistory jpaHistory = (JpaHistory) o;

        return id != null ? id.equals(jpaHistory.id) : jpaHistory.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static int getMaxEventLen() {
        return MAX_EVENT_LEN;
    }
}
