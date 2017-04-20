package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.media.Media;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public interface BinaryDistributionService extends DistributionService {
    /**
     * Distribute jwala Apache http webserver to remote host
     * @param hostname
     */
    void distributeWebServer(final String hostname);

    /**
     * This method copies unzip.exe to remote host
     * @param hostname
     */
    void distributeUnzip(final String hostname);

    /**
     * Distribute media
     * @param serverName the name of the server that owns the media to be distributed
     * @param hostName the host where the media is to be distributed
     * @param groups the groups where the server is assigned to
     * @param media the media to be distributed
     */
    void distributeMedia(final String serverName, final String hostName, Group[] groups, final Media media);
}
