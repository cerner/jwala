package com.cerner.jwala.service.binarydistribution;

/**
 * Created by Arvindo Kinny on 1/17/2017.
 */
public interface DistributionService {
    /**
     * Change remote file permissions on remote host
     * @param hostname
     * @param mode
     * @param targetDir
     * @param target
     */
    public void changeFileMode(final String hostname, final String mode, final String targetDir, final String target);

    /**
     * Delete file on remote host
     * @param hostname
     * @param destination
     */
    public void remoteDeleteBinary(final String hostname, final String destination);

    /**
     * Unpzip file
     * @param hostname
     * @param zipFileName
     * @param destination
     * @param exclude
     */
    public void remoteUnzipBinary(final String hostname, final String zipFileName, final String destination,
                                  final String exclude) ;

    /**
     * scp file to remote host
     * @param hostname
     * @param source
     * @param destination
     */
    public void remoteSecureCopyFile(final String hostname, final String source, final String destination);

    /**
     * Create remote directory, idempotent
     * @param hostname
     * @param remoteDir
     */
    public void remoteCreateDirectory(final String hostname, final String remoteDir);

    /**
     * Check if remote file exists
     *
     * @param hostname
     * @param remoteFilePath
     * @return
     */
    public boolean remoteFileCheck(final String hostname, final String remoteFilePath) ;

    /**
     * Back file with post fix UTC time stamp
     * @param hostname
     * @param remoteFilePath
     */
    public void backupFile(final String hostname, final String remoteFilePath) ;
}
