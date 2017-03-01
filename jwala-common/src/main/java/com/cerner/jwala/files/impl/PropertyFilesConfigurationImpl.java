package com.cerner.jwala.files.impl;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.files.FilesConfiguration;
import com.cerner.jwala.files.JwalaPath;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Paths specified in JwalaFiles section as paths.*
 * Properties defined in {@link JwalaPath}
 * @author horspe00
 *
 */
public class PropertyFilesConfigurationImpl implements FilesConfiguration {

    private final Map<JwalaPath, Path> paths = new HashMap<>();
    private final FileSystem defaultFs = FileSystems.getDefault();

    public PropertyFilesConfigurationImpl(Properties fmProperties) {
        load(fmProperties);
    }

    public void reload() {
        paths.clear();
        load(ApplicationProperties.getProperties());
    }

    public void load(Properties fmProperties) {         
        for(JwalaPath path : JwalaPath.values()) {
            paths.put(path, path.getDefaultPath());
        }

        for(Map.Entry<Object, Object> e : fmProperties.entrySet()) {
            if(e.getKey().toString().startsWith("paths.")) {
                for(Map.Entry<JwalaPath, Path> entry : paths.entrySet()) {
                    if(entry.getKey().getProperty().equalsIgnoreCase(e.getKey().toString())) {
                        entry.setValue(defaultFs.getPath(e.getValue().toString()));
                    }
                }
            }
        }
    }
}
