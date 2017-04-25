package com.cerner.jwala.service.media;

import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;

import java.util.List;
import java.util.Map;

/**
 * The media service contract
 *
 * Created by Jedd Cuison on 12/7/2016
 */
public interface MediaService {

    /**
     * Find a media by id
     * @param id id of the media
     * @return the {@link JpaMedia}
     */
    JpaMedia find(Long id);

    /**
     * Find a media
     * @param name name of the media
     * @return the {@link JpaMedia}
     */
    JpaMedia find(String name);

    /**
     * Find all media
     * @return List of {@link JpaMedia}
     */
    List<JpaMedia> findAll();

    /**
     * Create a media
     * @param mediaDataMap contains media data
     * @param mediaFileDataMap contains media archive file data
     */
    JpaMedia create(Map<String, String> mediaDataMap, Map<String, Object> mediaFileDataMap);

    /**
     * Remove media
     * @param name the name if the media to remove
     */
    void remove(String name);

    /**
     * Returns a list of {@link MediaType}
     * @return List of {@link MediaType}
     */
    MediaType [] getMediaTypes();

    /**
     * Update a media
     * @param media the media
     * @return updated media
     */
    JpaMedia update(JpaMedia media);
}
