package com.cerner.jwala.service.media.impl;

import com.cerner.jwala.service.repository.AbstractRepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@link com.cerner.jwala.service.repository.RepositoryService} implementation for media
 *
 * Created by Jedd Cuison on 12/16/2016
 */
@Service("mediaRepositoryService")
public class MediaRepositoryServiceImpl extends AbstractRepositoryService {

    @Value("${jwala.binary.dir}")
    private String binPath;

    @Override
    protected Path getRepositoryPath() {
        return Paths.get(binPath);
    }

}
