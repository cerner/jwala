package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.service.repository.AbstractRepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Jedd Anthony Cuison on 11/30/2016
 */
@Service("resourceRepositoryService")
public class ResourceRepositoryServiceImpl extends AbstractRepositoryService {

    @Value("${paths.web-archive}")
    private String archivePath;

    @Override
    protected Path getRepositoryPath() {
        return Paths.get(archivePath);
    }

}
