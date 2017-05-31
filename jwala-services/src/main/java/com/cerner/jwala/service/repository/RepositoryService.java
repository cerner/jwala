package com.cerner.jwala.service.repository;

import java.io.InputStream;
import java.util.List;

/**
 * Defines repository related operations on resources
 *
 * Created by Jedd Anthony Cuison on 11/30/2016
 */
public interface RepositoryService {

    /**
     * uploads a file to the resource repository
     * @param baseFilename the file name without the path
     * @param resource an inputstream that is the source of data to be uploaded
     * @return absolute location of the file in the repository
     */
    String upload(String baseFilename, InputStream resource);

    /**
     * removes a file from the resource repository
     * @param filename
     */
    void delete(String filename);

    /**
     * Get a list of absolute paths of all the existing binaries that contain the given file name
     * @param filename the base name to search for
     * @return the list of matching binaries as absolute paths
     */
    List<String> getBinariesByBasename(String filename);
}
