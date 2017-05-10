package com.cerner.jwala.common.properties;

/**
 * Created by Steven Ger on 12/21/16
 */
public enum PropertyKeys {

    ENCRYPTED_PASSWORD("ssh.encrypted.password"),
    JDK_BINARY_FILE_NAME("jwala.default.jdk.zip"),
    JMAP_DUMP_LIVE_ENABLED("jmap.dump.live.enabled"),
    JSCH_CHANNEL_SHELL_READ_INPUT_SLEEP_DURATION("jsch.channel.shell.read.input.sleep.duration"),
    JSCH_EXEC_READ_REMOTE_OUTPUT_LOOP_SLEEP_TIME("jsch.exec.read.remote.output.loop.sleep.time"),
    JSCH_EXEC_READ_REMOTE_OUTPUT_TIMEOUT("jsch.exec.read.remote.output.timeout"),
    JSCH_SHELL_READ_REMOTE_OUTPUT_TIMEOUT("jsch.shell.read.remote.output.timeout"),
    JWALA_HISTORY_MAX_REC_DEAFULT("jwala.history.max.rec.default"),
    JWALA_HISTORY_RESULT_FETCH_COUNT("jwala.history.result.fetch.count"),
    JWALA_JTA_TRANSACTION_TIMEOUT("jwala.jta.transaction.timeout"),
    KNOWN_HOSTS_FILE("ssh.knownHostsFile"),
    LOCAL_JWALA_BINARY_DIR("jwala.binary.dir"),
    PATHS_GENERATED_RESOURCE_DIR("paths.generated.resource.dir"),
    PATHS_RESOURCE_TEMPLATES("paths.resource-templates"),
    PORT("ssh.port"),
    PRIVATE_KEY_FILE("ssh.privateKeyFile"),
    REMOTE_JAWALA_DATA_DIR("remote.jwala.data.dir"),
    REMOTE_PATHS_INSTANCES_DIR("remote.paths.instances"),
    REMOTE_PATHS_TOMCAT_ROOT_CORE("remote.paths.tomcat.root.core"),
    REMOTE_SCRIPT_DIR("remote.commands.user-scripts"),
    SCRIPTS_PATH("commands.scripts-path"),
    TOMCAT_MANAGER_XML_SSL_PATH("tomcat.manager.xml.ssl.path"),
    USER_NAME("ssh.userName"),
    JVM_ROUTE_MAX_LENGTH("jvm.route.max.length");

    private String propertyName;

    PropertyKeys(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
