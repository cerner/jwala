package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.AbstractH2ServerService;
import com.cerner.jwala.service.DbServerServiceException;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * H2 Web Server implementation of {@link AbstractH2ServerService}
 *
 * Created by Jedd Cuison on 8/30/2016
 */
public class H2WebServerServiceImpl extends AbstractH2ServerService {

    private final static Logger LOGGER = Logger.getLogger(H2WebServerServiceImpl.class.getName());
    private static final String DEFAULT_WEBSERVER_PARAM = "-webSSL,-webPort,8084";

    public H2WebServerServiceImpl(final String webServerParams) {
        super(webServerParams == null ? DEFAULT_WEBSERVER_PARAM : webServerParams);
        if (webServerParams == null) {
            LOGGER.warning("webServerParams is null, loading default webServerParams values \"" + DEFAULT_WEBSERVER_PARAM + "\"");
        }
    }

    @Override
    protected Server createServer(final String [] serverParams) throws DbServerServiceException {
        try {
            return Server.createWebServer(serverParams);
        } catch (final SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create H2 Web Server!", e);
            throw new DbServerServiceException(e);
        }
    }
}
