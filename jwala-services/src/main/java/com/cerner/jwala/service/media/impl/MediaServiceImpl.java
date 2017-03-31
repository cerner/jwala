package com.cerner.jwala.service.media.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.media.MediaService;
import com.cerner.jwala.service.media.MediaServiceException;
import com.cerner.jwala.service.repository.RepositoryService;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        final String filename = (String) mediaFileDataMap.get("filename");
        final String dest = repositoryService.upload(filename, (BufferedInputStream) mediaFileDataMap.get("content"));

        final Set<String> zipRootDirSet = fileUtility.getZipRootDirs(dest);
        if (!zipRootDirSet.isEmpty()) {
            media.setMediaDir(Paths.get(StringUtils.join(zipRootDirSet, ",")));
            media.setLocalPath(Paths.get(dest));
            return mediaDao.create(media);
        }

        repositoryService.delete(dest);
        throw new MediaServiceException(MessageFormat
                .format("{0} does not have any root directories! It may not be a valid media file.", filename));
    }

    @Override
    @Transactional
    public void remove(final String name) {
        final JpaMedia jpaMedia = mediaDao.find(name);
        checkForExistingAssociationsBeforeRemove(name);
        mediaDao.remove(jpaMedia);
        repositoryService.delete(jpaMedia.getLocalPath().getFileName().toString());
    }

    private void checkForExistingAssociationsBeforeRemove(String name) {
        List<Jvm> jvmList = jvmPersistenceService.getJvms();
        for (Jvm jvm : jvmList) {
            if (jvm.getJdkMedia().getName().equals(name)) {
                throw new MediaServiceException("Cannot delete media check for jvm dependencies");
            }
        }
    }

    @Override
    public MediaType[] getMediaTypes() {
        return MediaType.values();
    }

    @Override
    @Transactional
    public JpaMedia update(final JpaMedia media) {
        return mediaDao.update(media);
    }

}
