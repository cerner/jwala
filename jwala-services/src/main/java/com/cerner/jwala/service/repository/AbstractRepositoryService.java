package com.cerner.jwala.service.repository;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * Defines general repository related operations
 *
 * Created by Jedd Cuison on 12/16/2016
 */
public abstract class AbstractRepositoryService implements RepositoryService {

    private static final int BYTE_ARRAY_SIZE = 1024;

    protected abstract Path getRepositoryPath();

    @Override
    public String upload(final String filename, final InputStream resource) {
        try {
            final String absoluteFilename = getRepositoryPath().toAbsolutePath().normalize().toString() + "/" +
                    getResourceNameUniqueName(filename);
            final FileOutputStream out = new FileOutputStream(absoluteFilename);
            final byte [] bytes = new byte[BYTE_ARRAY_SIZE];
            int byteCount;
            while ((byteCount = resource.read(bytes)) != -1) {
                out.write(bytes, 0, byteCount);
            }
            return absoluteFilename;
        } catch (final IOException e) {
            throw new RepositoryServiceException("Resource upload failed!", e);
        } finally {
            IOUtils.closeQuietly(resource);
        }
    }

    @Override
    public void delete(final String filename) {
        final File file = new File(filename);
        if (file.delete()) {
            throw new RepositoryServiceException(MessageFormat.format("Failed to delete {0}!", filename));
        }
    }

    /**
     * Generate a unique file name for a resource e.g. hct.war -> hct-e60b9a77-9ac3-443a-85ee-5cc001b62d80.war
     * @param name the name of the resource
     * @return the resource name with a UUID for uniqueness
     */
    private String getResourceNameUniqueName(final String name) {
        int idx = name.lastIndexOf('.');
        String prefix, suffix;
        if(idx == -1) {
            prefix = name;
            suffix = "";
        } else {
            prefix = name.substring(0, idx);
            suffix = name.substring(idx);
        }
        return prefix + "-" + UUID.randomUUID().toString() + suffix;
    }

}
