package com.siemens.cto.aem.domain.model.path;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Local path or remote URI
 * Linux or Windows separators
 * ;separated key value pair (features) can be appended
 * 
 */
public class Path implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern absoluteRegEx = Pattern.compile("^(([a-zA-Z]:)|([\\/])).*");
    private static final Pattern featureRegEx = Pattern.compile(";([^=;]*)?=?([0-9A-Za-z]*)?");
    
    private final String path;

    public Path(final String thePath) {
        path = thePath;
    }

    public boolean isAbsolute() {
        return absoluteRegEx.matcher(path).matches();
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
        Matcher featureMatcher = featureRegEx.matcher(path);
        return featureMatcher.replaceAll("");
    }
    
    public String getFeature(String featureName, String defaultValue) {
        Matcher featureMatcher = featureRegEx.matcher(path);
        while(featureMatcher.find()) {
            if(featureMatcher.group(1).equalsIgnoreCase(featureName)) {
                return featureMatcher.group(2);
            }
        }
        return defaultValue;
    }
}
