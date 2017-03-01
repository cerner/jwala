package com.cerner.jwala.control.configuration;

public enum AemSshProperty {

    USER_NAME("AemSsh.userName"),
    PORT("AemSsh.port"),
    PRIVATE_KEY_FILE("AemSsh.privateKeyFile"),
    KNOWN_HOSTS_FILE("AemSsh.knownHostsFile"),
    ENCRYPTED_PASSWORD("AemSsh.encrypted.password");

    private final String propertyName;

    private AemSshProperty(final String thePropertyName) {
        propertyName = thePropertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
