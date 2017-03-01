package com.cerner.jwala.tomcat.listener.messaging.jgroups;

import com.cerner.jwala.tomcat.listener.messaging.ServerInfoFields;
import org.apache.catalina.LifecycleState;
import org.jgroups.Address;
import org.jgroups.Message;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * A message content builder
 *
 * Created by Jedd Cuison on 8/15/2016
 */
public class JGroupsServerInfoMessageBuilder {

    private String serverId;
    private String serverName;
    private LifecycleState state;
    private Address srcAddress;
    private Address destAddress;

    public JGroupsServerInfoMessageBuilder setServerId(final String serverId) {
        this.serverId = serverId;
        return this;
    }

    public JGroupsServerInfoMessageBuilder setServerName(final String serverName) {
        this.serverName = serverName;
        return this;
    }

    public JGroupsServerInfoMessageBuilder setState(final LifecycleState state) {
        this.state = state;
        return this;
    }

    public JGroupsServerInfoMessageBuilder setSrcAddress(final Address srcAddress) {
        this.srcAddress = srcAddress;
        return this;
    }

    public JGroupsServerInfoMessageBuilder setDestAddress(final Address destAddress) {
        this.destAddress = destAddress;
        return this;
    }

    public Message build() {
        final Map<String, Object> serverInfoMap = new HashMap<>();
        serverInfoMap.put(ServerInfoFields.ID.name(), serverId);
        serverInfoMap.put(ServerInfoFields.NAME.name(), serverName);
        serverInfoMap.put(ServerInfoFields.AS_OF.name(), DateTime.now());
        serverInfoMap.put(ServerInfoFields.STATE.name(), state);
        return new Message(destAddress, srcAddress, serverInfoMap);
    }
}
