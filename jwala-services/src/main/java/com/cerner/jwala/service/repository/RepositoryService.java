package com.cerner.jwala.service.repository;

import java.io.InputStream;

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

}
