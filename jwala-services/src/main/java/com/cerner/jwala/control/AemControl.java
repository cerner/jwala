package com.cerner.jwala.control;

import com.cerner.jwala.common.properties.ApplicationProperties;

/**
 * Constants specific to this module
 * <code>AemControl.Properties.SCRIPT_PATH.getValue()</code>
 *
 * @author horspe00
 */
public class AemControl {

    private static final String NET_STOP_SLEEP_TIME_SECONDS_DEFAULT = "60";

    private AemControl() {
    }

    public enum Properties {
        CYGPATH("commands.cygwin.cygpath", "/usr/bin/cygpath"),
        START_SCRIPT_NAME("commands.cygwin.start-service", "start-service.sh"),
        HEAP_DUMP_SCRIPT_NAME("commands.cygwin.heap-dump", "heap-dump.sh"),
        THREAD_DUMP_SCRIPT_NAME("commands.cygwin.thread-dump", "thread-dump.sh"),
        STOP_SCRIPT_NAME("commands.cygwin.stop-service", "stop-service.sh"),
        SCP_SCRIPT_NAME("commands.cygwin.scp", "secure-copy.sh"),
        DEPLOY_CONFIG_ARCHIVE_SCRIPT_NAME("commands.cygwin.deploy-config-tar", "unjar-jvm.sh"),
        DELETE_SERVICE_SCRIPT_NAME("commands.cygwin.delete-service", "delete-service.sh"),
        INSTALL_SERVICE_SCRIPT_NAME("commands.cygwin.install-service", "install-service.sh"),
        SERVICE_STATUS_SCRIPT_NAME("commands.cygwin.service-status", "service-status.sh"),
        INSTALL_SERVICE_WS_SERVICE_SCRIPT_NAME("commands.cygwin.install-ws-service", "install-ws-service.sh"),
        UNPACK_BINARY_SCRIPT_NAME("commands.cygwin.unpack.sh", "unpack.sh"),
        UNZIP_SCRIPT_NAME("commands.unzip.sh", "unzip.sh"),
        SLEEP_TIME("net.stop.sleep.time.seconds", NET_STOP_SLEEP_TIME_SECONDS_DEFAULT);

        private final String propertyName;
        private final String defaultValue;

        Properties(final String thePropertyName, final String theDefaultValue) {
            propertyName = thePropertyName;
            defaultValue = theDefaultValue;
        }

        public String getName() {
            return propertyName;
        }

        public String getDefault() {
            return defaultValue;
        }

        public String getValue() {
            return ApplicationProperties.get(propertyName, defaultValue);
        }

        @Override
        public String toString() {
            return getValue();
        }
    }
}