package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.exception.CommandFailureException;

/**
 * Created by Arvindo Kinny on 10/11/2016.
 */

public interface BinaryDistributionControlService {
    /**
     * This method copies a resource on a remote host from source to destination folder
     * @param hostname
     * @param source
     * @param destination
     * @return
     * @throws CommandFailureException
     */
    CommandOutput secureCopyFile(final String hostname, final String source, final String destination) throws CommandFailureException;

    /**
     *
     * @param hostname
     * @param destination
     * @return
     * @throws CommandFailureException
     */
    CommandOutput createDirectory(final String hostname, final String destination) throws CommandFailureException;

    /**
     *
     * @param hostname
     * @param destination
     * @return
     * @throws CommandFailureException
     */
    CommandOutput checkFileExists(final String hostname, final String destination) throws CommandFailureException;

    /**
     *
     * @param hostname
     * @param zipPath
     * @param destination
     * @param exclude
     * @return
     * @throws CommandFailureException
     */
    CommandOutput unzipBinary(final String hostname, final String zipPath, final String destination, final String exclude) throws CommandFailureException;

    /**
     *
     * @param hostname
     * @param destination
     * @return
     * @throws CommandFailureException
     */
    CommandOutput deleteBinary(final String hostname, final String destination) throws CommandFailureException;

    /**
     *
     * @param hostname
     * @param mode
     * @param targetDir
     * @param target
     * @return
     * @throws CommandFailureException
     */
    CommandOutput changeFileMode(final String hostname, final String mode, final String targetDir, final String target) throws CommandFailureException;


    /**
     *
     * @param hostname Name of the host
     * @return Uname of the host Linux, CYGWIN_NT-6.3, etc
     * @throws CommandFailureException
     */
    CommandOutput getUName(final String hostname) throws CommandFailureException;

    /**
     *
     * @param hostname Name of the host
     * @param remotePath remote file of directory
     * @throws CommandFailureException
     */
    CommandOutput backupFile(final String hostname, final String remotePath) throws CommandFailureException;
}
