package com.cerner.jwala.common.domain.model.path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local path or remote URI
 * Linux or Windows separators
 * ;separated key value pair (features) can be appended
 * 
 */
public class Path implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Path.class);

    public static final String HTTP = "http";
    private static final Pattern ABSOLUTE_REG_EX = Pattern.compile("^(([a-zA-Z]:)|([\\/])).*");
    private static final Pattern FEATURE_REG_EX = Pattern.compile(";([^=;]*)?=?([0-9A-Za-z]*)?");
    
    private final String path;

    public Path(final String thePath) {
        path = thePath;
    }

    public boolean isAbsolute() {
        return ABSOLUTE_REG_EX.matcher(path).matches();
    }

    /**
     * Suitable for constructing URIs
     * @return the path unadorned with key value pairs
     */
    public String getUriPath() {
        return removeFeatures(path);
    }

    /**
     * Suitable for persisting
     * @return the path including key value pairs
     */
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Path rhs = (Path) obj;
        return new EqualsBuilder()
                .append(this.path, rhs.path)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(path)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("path", path)
                .toString();
    }
    
    private static String removeFeatures(String path) { 
        Matcher featureMatcher = FEATURE_REG_EX.matcher(path);
        return featureMatcher.replaceAll("");
    }
    
    public String getFeature(String featureName, String defaultValue) {
        Matcher featureMatcher = FEATURE_REG_EX.matcher(path);
        while(featureMatcher.find()) {
            if(featureMatcher.group(1).equalsIgnoreCase(featureName)) {
                return featureMatcher.group(2);
            }
        }
        return defaultValue;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(path);
    }

    public boolean startsWithHttp() {
        return path.toLowerCase().startsWith(HTTP);
    }

    public boolean isValidUrl() {
        try {
            new URL(path);
            return true;
        } catch (final MalformedURLException e) {
            LOGGER.error("{} is an invalid URL!", path,e);
            return false;
        }
    }

    public boolean isValidUri(final String protocol, final String hostName, final int port) {
        try {
            new URI(protocol, null, hostName, port, path, StringUtils.EMPTY, StringUtils.EMPTY);
            return true;
        } catch (final URISyntaxException e) {
            LOGGER.error("Failed to create a valid URI using the parameters: {}, {}, {} and {}!",
                         protocol, hostName, port, path, e);
            return false;
        }
    }

    public URI toUri() {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw new PathToUriException(path);
        }
    }

}
