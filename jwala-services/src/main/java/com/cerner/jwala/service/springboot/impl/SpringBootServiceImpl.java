package com.cerner.jwala.service.springboot.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;
import com.cerner.jwala.service.repository.RepositoryService;
import com.cerner.jwala.service.springboot.SpringBootService;
import com.cerner.jwala.service.springboot.SpringBootServiceException;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.io.BufferedInputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static com.cerner.jwala.common.JwalaUtils.getPathForExistingBinary;

/**
 * Created on 6/1/2017.
 */
@Service
public class SpringBootServiceImpl implements SpringBootService {

    @Autowired
    private SpringBootAppDao springBootAppDao;

    @Autowired
    @Qualifier("mediaRepositoryService")
    private RepositoryService repositoryService;

    @Autowired
    private FileUtility fileUtility;


    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootServiceImpl.class);

    @Override
    public JpaSpringBootApp controlSpringBoot(String name, String command) {
        return null;
    }

    @Override
    public JpaSpringBootApp generateAndDeploy(String name) {
        return null;
    }

    @Override
    public JpaSpringBootApp createSpringBoot(Map<String, String> springBootDataMap, Map<String, Object> springBootFileDataMap) {
        LOGGER.info("Create Spring Boot service create spring boot data map {} and file data map {}", springBootDataMap, springBootFileDataMap);

        final ObjectMapper objectMapper = new ObjectMapper();
        final JpaSpringBootApp springBootApp = objectMapper.convertValue(springBootDataMap, JpaSpringBootApp.class);

        // filename can be the full path or just the name that is why we need to convert it to Paths
        // to extract the base name e.g. c:/jdk.zip -> jdk.zip or jdk.zip -> jdk.zip
        final String filename = Paths.get((String) springBootFileDataMap.get("filename")).getFileName().toString();

        try {
            springBootAppDao.findByName(springBootApp.getName());
            final String msg = MessageFormat.format("Spring Boot already exists with name {0}", springBootApp.getName());
            LOGGER.error(msg);
            throw new SpringBootServiceException(msg);
        } catch (NoResultException e) {
            LOGGER.debug("No Spring Boot name conflict, ignoring not found exception for creating Spring Boot app ", e);
        }

        final String uploadedFilePath = repositoryService.upload(filename, (BufferedInputStream) springBootFileDataMap.get("content"));
        final List<String> binariesByBasename = repositoryService.getBinariesByBasename(FilenameUtils.removeExtension(filename));
        final String dest = getPathForExistingBinary(uploadedFilePath, binariesByBasename);

        return springBootAppDao.create(springBootApp);
    }

    @Override
    public JpaSpringBootApp update(JpaSpringBootApp springBootApp) {
        LOGGER.info("Update Spring Boot service {}", springBootApp);
        return springBootAppDao.update(springBootApp);
    }

    @Override
    public JpaSpringBootApp remove(String name) {
        LOGGER.info("Spring Boot service remove {}", name);
        return springBootAppDao.remove(name);
    }

    @Override
    public JpaSpringBootApp find(String name) {
        LOGGER.info("Spring Boot find service {}", name);
        return springBootAppDao.find(name);
    }
}
