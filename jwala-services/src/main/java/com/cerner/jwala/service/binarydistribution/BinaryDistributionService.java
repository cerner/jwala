package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.media.Media;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public interface BinaryDistributionService extends DistributionService {

    /**
     * This method copies unzip.exe to remote host
     * @param hostname
     */
    void distributeUnzip(final String hostname);

    /**
     * Distribute media
     * @param jvmOrWebServerName the name of the server that owns the media to be distributed
     * @param hostName the host where the media is to be distributed
     * @param groups the groups where the server is assigned to
     * @param media the media to be distributed
     */
    <T> void distributeMedia(final T jvmOrWebServer, final String hostName, Group[] groups, final Media media);
}
