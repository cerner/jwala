package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.jvm.Jvm;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */
public interface BinaryDistributionService extends DistributionService {
    /**
     * Distribute jwala JDK to remote host
     * @param jvm
     */
    void distributeJdk(final Jvm jvm);

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
}
