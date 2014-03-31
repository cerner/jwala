package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class WebServer implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Identifier<WebServer> id;
	private final Group group;
	private final String host;
	private final String name;
	private final Integer port;

	public WebServer(final Identifier<WebServer> theId, final Group theGroup,
			final String theName, final String theHost, final Integer thePort) {
		id = theId;
		host = theHost;
		port = thePort;
		name = theName;
		group = theGroup;
	}

	public Identifier<WebServer> getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebServer other = (WebServer) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WebServer {id=" + id + ", group=" + group + ", host=" + host
				+ ", name=" + name + ", port=" + port + "}";
	}

}
