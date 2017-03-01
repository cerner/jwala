package com.cerner.jwala.service.impl;

import com.cerner.jwala.service.AbstractH2ServerService;
import com.cerner.jwala.service.DbServerServiceException;
import org.h2.tools.Server;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * H2 TCP Server implementation of {@link AbstractH2ServerService}
 *
 * Created by Jedd Cuison on 8/25/2016
 */
public class H2TcpServerServiceImpl extends AbstractH2ServerService {

    private static final Logger LOGGER = Logger.getLogger(H2TcpServerServiceImpl.class.getName());

    private static final String DEFAULT_TCP_SERVER_PARAMS = "-tcpPort,9094,-tcpAllowOthers";

    public H2TcpServerServiceImpl(final String tcpServerParams) {
        super(tcpServerParams == null ? DEFAULT_TCP_SERVER_PARAMS : tcpServerParams);
        if (tcpServerParams == null) {
            LOGGER.warning(MessageFormat.format("tcpServerParams is null, loading default tcpServerParams values \"{0}\"",
                                                DEFAULT_TCP_SERVER_PARAMS));
        }
    }

    @Override
    protected Server createServer(final String [] serverParams) throws DbServerServiceException {
        try {
            return Server.createTcpServer(serverParams);
        } catch (final SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create H2 TCP Server!", e);
            throw new DbServerServiceException(e);
        }
    }

}
