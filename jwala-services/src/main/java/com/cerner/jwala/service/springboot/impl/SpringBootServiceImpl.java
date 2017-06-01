package com.cerner.jwala.service.springboot.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.springboot.SpringBootApp;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.dao.SpringBootAppDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.domain.JpaSpringBootApp;
import com.cerner.jwala.service.repository.RepositoryService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import com.cerner.jwala.service.springboot.SpringBootService;
import com.cerner.jwala.service.springboot.SpringBootServiceException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.*;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Scanner;

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
    ResourceContentGeneratorService resourceContentGeneratorService;

    @Autowired
    private FileUtility fileUtility;


    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootServiceImpl.class);

    @Override
    @Transactional
    public JpaSpringBootApp controlSpringBoot(String name, String command) {
        return null;
    }

    @Override
    @Transactional
    public JpaSpringBootApp generateAndDeploy(String name) throws FileNotFoundException {
        final JpaSpringBootApp springBootApp = springBootAppDao.find(name);
        InputStream templateData = new FileInputStream(new File(ApplicationProperties.getRequired(PropertyKeys.ROGUE_WINDOWS_XML_TEMPLATE)));
        Scanner scanner = new Scanner(templateData).useDelimiter("\\A");
        String springBootXmlTemplateContent = scanner.hasNext() ? scanner.next() : "";

        String templateContent = resourceContentGeneratorService.generateContent("spring-boot.xml.tpl", springBootXmlTemplateContent, null, new ModelMapper().map(springBootApp, SpringBootApp.class), ResourceGeneratorType.TEMPLATE);

        return springBootApp;
    }

    @Override
    @Transactional
    public JpaSpringBootApp createSpringBoot(Map<String, Object> springBootDataMap, Map<String, Object> springBootFileDataMap) {
        LOGGER.info("Create Spring Boot service create spring boot data map {} and file data map {}", springBootDataMap, springBootFileDataMap);

        final JpaSpringBootApp springBootApp = new JpaSpringBootApp();
        springBootApp.setName((String) springBootDataMap.get("name"));
        springBootApp.setJdkMedia((JpaMedia) springBootDataMap.get("jdkMedia"));

        // filename can be the full path or just the name that is why we need to convert it to Paths
        // to extract the base name e.g. c:/jdk.zip -> jdk.zip or jdk.zip -> jdk.zip
        final String filename = Paths.get((String) springBootFileDataMap.get("filename")).getFileName().toString();

        try {
            springBootAppDao.find(springBootApp.getName());
            final String msg = MessageFormat.format("Spring Boot already exists with name {0}", springBootApp.getName());
            LOGGER.error(msg);
            throw new SpringBootServiceException(msg);
        } catch (NoResultException e) {
            LOGGER.debug("No Spring Boot name conflict, ignoring not found exception for creating Spring Boot app ", e);
        }

        final String uploadedFilePath = repositoryService.upload(filename, (BufferedInputStream) springBootFileDataMap.get("content"));
        springBootApp.setArchiveFilename(uploadedFilePath);

        return springBootAppDao.create(springBootApp);
    }

    @Override
    @Transactional
    public JpaSpringBootApp update(JpaSpringBootApp springBootApp) {
        LOGGER.info("Update Spring Boot service {}", springBootApp);
        return springBootAppDao.update(springBootApp);
    }

    @Override
    public void remove(String name) {
        LOGGER.info("Spring Boot service remove {}", name);
        springBootAppDao.remove(springBootAppDao.find(name));
    }

    @Override
    @Transactional
    public JpaSpringBootApp find(String name) {
        LOGGER.info("Spring Boot find service {}", name);
        return springBootAppDao.find(name);
    }
}
