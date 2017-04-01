package com.cerner.jwala.common.properties;

/**
 * Created by Steven Ger on 12/21/16
 */
public enum PropertyKeys {

    REMOTE_JAWALA_DATA_DIR("remote.jwala.data.dir"),
    APACHE_HTTPD_FILE_NAME("jwala.apache.httpd.zip.name"),
    REMOTE_PATHS_APACHE_HTTPD("remote.paths.apache.httpd"),
    REMOTE_PATHS_APACHE_HTTPD_CONF("remote.paths.httpd.conf"),
    REMOTE_PATHS_HTTPD_ROOT_DIR_NAME("remote.paths.httpd.root.dir.name"),
    SCRIPTS_PATH("commands.scripts-path"),
    REMOTE_TOMCAT_DIR_NAME("remote.tomcat.dir.name"),
    TOMCAT_BINARY_FILE_NAME("jwala.tomcat.zip.name"),
    REMOTE_PATH_INSTANCES_DIR("remote.paths.instances"),
    REMOTE_PATHS_DEPLOY_DIR("remote.paths.deploy.dir"),
    REMOTE_SCRIPT_DIR("remote.commands.user-scripts"),
    REMOTE_JAVA_HOME("remote.jwala.java.home"),
    JDK_BINARY_FILE_NAME("jwala.default.jdk.zip"),
    REMOTE_PATHS_TOMCAT_ROOT_CORE("remote.paths.tomcat.root.core"),
    LOCAL_JWALA_BINARY_DIR("jwala.binary.dir"),
    JMAP_DUMP_LIVE_ENABLED("jmap.dump.live.enabled"),
    TOMCAT_MANAGER_XML_SSL_PATH("tomcat.manager.xml.ssl.path"),
    PATHS_RESOURCE_TEMPLATES("paths.resource-templates"),
    JSCH_CHANNEL_SHELL_READ_INPUT_SLEEP_DURATION("jsch.channel.shell.read.input.sleep.duration"),
    JSCH_EXEC_READ_REMOTE_OUTPUT_LOOP_SLEEP_TIME("jsch.exec.read.remote.output.loop.sleep.time"),
    JSCH_SHELL_READ_REMOTE_OUTPUT_TIMEOUT("jsch.shell.read.remote.output.timeout"),
    JSCH_EXEC_READ_REMOTE_OUTPUT_TIMEOUT("jsch.exec.read.remote.output.timeout"),
    USER_NAME("ssh.userName"),
    PORT("ssh.port"),
    PRIVATE_KEY_FILE("ssh.privateKeyFile"),
    KNOWN_HOSTS_FILE("ssh.knownHostsFile"),
    ENCRYPTED_PASSWORD("ssh.encrypted.password");


    private String propertyName;

    PropertyKeys(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
