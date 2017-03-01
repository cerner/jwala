package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.common.properties.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JschLogger implements com.jcraft.jsch.Logger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschLogger.class);

    private static final int DEBUG_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.DEBUG;// 0
    private static final int INFO_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.INFO;// 1
    private static final int WARN_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.WARN;// 2
    private static final int ERROR_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.ERROR;// 3
    private static final int FATAL_LEVEL_THRESHOLD = com.jcraft.jsch.Logger.FATAL;// 4

    @Override
    public boolean isEnabled(int level) {
        // Just let the log function decide.
        return ApplicationProperties.getAsBoolean("ssh.verbose");
    }

    @Override
    public void log(int level, String message) {
        if (level == DEBUG_LEVEL_THRESHOLD) {
            LOGGER.debug(message);
        } else if (level == INFO_LEVEL_THRESHOLD) {
            LOGGER.info(message);
        } else if (level == WARN_LEVEL_THRESHOLD) {
            LOGGER.warn(message);
        } else if (level == ERROR_LEVEL_THRESHOLD) {
            LOGGER.error(message);
        } else if (level == FATAL_LEVEL_THRESHOLD) {
            LOGGER.error("JSch Fatal) " + message);
        }
    }
}
