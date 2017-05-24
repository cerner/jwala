package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A utility class for miscellaneous jwala specific operations
 * <p>
 * Created by Arvindo Kinny on 12/1/2016
 */
public class JwalaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwalaUtils.class);

    /**
     * Method to get IP from hostname
     * @param hostname host name to look for IP
     * @return IPv4 address
     */
    public static String getHostAddress(String hostname){
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        }catch(UnknownHostException ex){
            String message = "Invalid Hostname " + hostname;
            LOGGER.error(message, ex);
            throw new ApplicationException(message, ex);
        }
    }
}
