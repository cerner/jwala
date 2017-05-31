package com.cerner.jwala.service.media.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.media.MediaService;
import com.cerner.jwala.service.media.MediaServiceException;
import com.cerner.jwala.service.repository.RepositoryService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.BufferedInputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cerner.jwala.common.JwalaUtils.getPathForExistingBinary;

/**
 * Implements {@link MediaService}
 * <p>
 * Created by Jedd Cuison on 12/7/2016
 */
@Service
public class MediaServiceImpl implements MediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaServiceImpl.class);

    @Autowired
    private MediaDao mediaDao;

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private WebServerPersistenceService webServerPersistenceService;

    @Autowired
    private FileUtility fileUtility;

    @Autowired
    @Qualifier("mediaRepositoryService")
    private RepositoryService repositoryService;

    @Override
    public JpaMedia find(final Long id) {
        return mediaDao.findById(id);
    }

    @Override
    @Transactional
    public JpaMedia find(final String name) {
        return mediaDao.find(name);
    }

    @Override
    @Transactional
    public List<JpaMedia> findAll() {
        return mediaDao.findAll();
    }

    @Override
    @Transactional
    public JpaMedia create(final Map<String, String> mediaDataMap, final Map<String, Object> mediaFileDataMap) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JpaMedia media = objectMapper.convertValue(mediaDataMap, JpaMedia.class);

        // filename can be the full path or just the name that is why we need to convert it to Paths
        // to extract the base name e.g. c:/jdk.zip -> jdk.zip or jdk.zip -> jdk.zip
        final String filename = Paths.get((String) mediaFileDataMap.get("filename")).getFileName().toString();

        try {
            mediaDao.findByNameAndType(media.getName(), media.getType());
            final String msg = MessageFormat.format("Media already exists with name {0} and type {1}", media.getName(), media.getType());
            LOGGER.error(msg);
            throw new MediaServiceException(msg);
        } catch (NoResultException e) {
            LOGGER.debug("No Media name conflict, ignoring not found exception for creating media ", e);
        }

        final String uploadedFilePath = repositoryService.upload(filename, (BufferedInputStream) mediaFileDataMap.get("content"));
        final List<String> binariesByBasename = repositoryService.getBinariesByBasename(FilenameUtils.removeExtension(filename));
        final String dest = getPathForExistingBinary(uploadedFilePath, binariesByBasename);

        final Set<String> zipRootDirSet = fileUtility.getZipRootDirs(dest);
        if (!zipRootDirSet.isEmpty()) {
            media.setRootDir(Paths.get(StringUtils.join(zipRootDirSet, ",")));
            media.setLocalPath(Paths.get(dest));
            return mediaDao.create(media);
        }
        repositoryService.delete(dest);
        throw new MediaServiceException(MessageFormat.
                format("{0} does not have any root directories! It may not be a valid media file.", filename));
    }

    @Override
    @Transactional
    public void remove(final String name, final MediaType type) {
        final JpaMedia jpaMedia = mediaDao.findByNameAndType(name, type);
        checkForAssociation(name, type);
        mediaDao.remove(jpaMedia);
        repositoryService.delete(jpaMedia.getLocalPath().getFileName().toString());
    }

    /**
     * This method will check for the existing jvm associations for the media
     *
     * @param name media name
     */
    private void checkForAssociation(String name, MediaType type) {
        List<Jvm> jvmList = jvmPersistenceService.getJvms();
        List<WebServer> webServerList = webServerPersistenceService.getWebServers();
        Set<String> existingAssosiations = new HashSet<>();
        for (Jvm jvm : jvmList) {
            if (jvm.getJdkMedia() != null && jvm.getJdkMedia().getType() == type && name.equalsIgnoreCase(jvm.getJdkMedia().getName())) {
                existingAssosiations.add(jvm.getJvmName());
            }
            if (jvm.getTomcatMedia() != null && jvm.getTomcatMedia().getType() == type && name.equalsIgnoreCase(jvm.getTomcatMedia().getName())) {
                existingAssosiations.add(jvm.getJvmName());
            }
        }
        for (WebServer webServer : webServerList) {
            if (webServer.getApacheHttpdMedia() != null && name.equalsIgnoreCase(webServer.getApacheHttpdMedia().getName()) &&
                    webServer.getApacheHttpdMedia().getType() == type) {
                existingAssosiations.add(webServer.getName());
            }
        }
        if (!existingAssosiations.isEmpty()) {
            final String msg = MessageFormat.format("The media {0} cannot be deleted because it is still associated with following {1}", name, existingAssosiations);
            LOGGER.error(msg);
            throw new MediaServiceException(msg);
        }
    }

    @Override
    public MediaType[] getMediaTypes() {
        return MediaType.values();
    }

    @Override
    @Transactional
    public JpaMedia update(final JpaMedia media) {
        JpaMedia originalMedia = mediaDao.findById(media.getId());
        try {
            if (!originalMedia.getName().equalsIgnoreCase(media.getName()) && null != mediaDao.findByNameAndType(media.getName(), media.getType())) {
                final String msg = MessageFormat.format("Media already exists with name {0} and type {1}", media.getName(), media.getType());
                LOGGER.error(msg);
                throw new MediaServiceException(msg);
            }
        } catch (NoResultException e) {
            LOGGER.debug("No media name conflict, ignore no result exception for creating media", e);
        }
        return mediaDao.update(media);
    }

}
