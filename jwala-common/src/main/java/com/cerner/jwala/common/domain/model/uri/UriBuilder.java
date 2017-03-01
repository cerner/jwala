package com.cerner.jwala.common.domain.model.uri;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.exception.ApplicationException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cerner.jwala.common.domain.model.path.Path;

import java.net.URI;
import java.net.URISyntaxException;

public class UriBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(UriBuilder.class); 

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    private static final String NO_USER_INFO = null;
    private static final String NO_QUERY = null;
    private static final String NO_FRAGMENT = null;

    private String scheme;
    private String user;
    private String host;
    private Integer port;
    private Integer httpsPort;
    private Path path;
    private String query;
    private String fragment;

    public UriBuilder() {
        scheme = HTTP;
        user = NO_USER_INFO;
        query = NO_QUERY;
        fragment = NO_FRAGMENT;
    }

    public UriBuilder setScheme(final String aScheme) {
        scheme = aScheme;
        return this;
    }

    public UriBuilder setUser(final String aUser) {
        user = aUser;
        return this;
    }

    public UriBuilder setHost(final String aHost) {
        host = aHost;
        return this;
    }

    public UriBuilder setPort(final Integer aPort) {
        port = aPort;
        return this;
    }

    public UriBuilder setPath(final Path aPath) {
        setScheme(aPath.getFeature("scheme", scheme));
        path = aPath;
        return this;
    }

    public UriBuilder setQuery(final String aQuery) {
        query = aQuery;
        return this;
    }

    public UriBuilder setFragment(final String aFragment) {
        this.fragment = aFragment;
        return this;
    }

    public URI buildUnchecked() {
        try {
            final URI uri = build();
            LOGGER.trace("Constructed URI: " + uri.toString());
            return uri;
        } catch (final URISyntaxException urise) {
            throw new ApplicationException("Unable to construct the URI for : " + this.toString(), urise);
        }
    }

    public URI build() throws URISyntaxException {
        final URI uri = new URI(scheme,
                                user,
                                host,
                                httpsPort != null && scheme.equals(HTTPS) ? httpsPort : port,
                                path.getUriPath(),
                                query,
                                fragment);
        return uri;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("scheme", scheme)
                .append("user", user)
                .append("host", host)
                .append("port", port)
                .append("httpsPort", httpsPort)
                .append("path", path)
                .append("query", query)
                .append("fragment", fragment)
                .toString();
    }

    public UriBuilder setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
        return this;
    }
}
