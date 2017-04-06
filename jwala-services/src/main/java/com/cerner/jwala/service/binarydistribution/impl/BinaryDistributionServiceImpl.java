package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.text.MessageFormat;

import static com.cerner.jwala.control.AemControl.Properties.UNZIP_SCRIPT_NAME;

/**
 * Created by Arvindo Kinny on 10/11/2016
 */
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);

    @Autowired
    protected SshConfiguration sshConfig;

    private static final String UNZIPEXE = "unzip.exe";
    private static final String APACHE_EXCLUDE = "ReadMe.txt *--";

    @Autowired
    private BinaryDistributionControlService binaryDistributionControlService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    private HistoryFacadeService historyFacadeService;


    @Override
    public void distributeWebServer(final String hostname) {
        String writeLockResourceName = hostname;
        try {
            binaryDistributionLockManager.writeLock(writeLockResourceName);
            String apacheDirName = ApplicationProperties.get(PropertyKeys.REMOTE_PATHS_HTTPD_ROOT_DIR_NAME);
            String remoteDeployDir = ApplicationProperties.getRequired(PropertyKeys.REMOTE_PATHS_DEPLOY_DIR);
            String httpdZipFile = ApplicationProperties.getRequired(PropertyKeys.APACHE_HTTPD_FILE_NAME);
            String jwalaBinaryDir = ApplicationProperties.getRequired(PropertyKeys.LOCAL_JWALA_BINARY_DIR);
            if (!binaryDistributionControlService.checkFileExists(hostname, remoteDeployDir+"/"+apacheDirName).getReturnCode().wasSuccessful()) {
                distributeBinary(hostname, jwalaBinaryDir + File.separator + httpdZipFile, remoteDeployDir, APACHE_EXCLUDE);
            } else {
                LOGGER.warn("Webserver directories already exists, installation of {} skipped!", httpdZipFile);
            }
        } finally {
            binaryDistributionLockManager.writeUnlock(writeLockResourceName);
        }
    }

    @Override
    public void distributeJdk(final Jvm jvm) {
        LOGGER.info("Start deploy jdk for {}", jvm.getHostName());
        final Media jdkMedia = jvm.getJdkMedia();
        final String binaryDeployDir = jdkMedia.getRemoteHostPath().replaceAll("\\\\", "/");
        if (binaryDeployDir != null && !binaryDeployDir.isEmpty()) {
            historyFacadeService.write(jvm.getHostName(), jvm.getGroups(), "DISTRIBUTE_JDK " + jdkMedia.getName(),
                    EventType.APPLICATION_EVENT, getUserNameFromSecurityContext());
            if (!checkIfMediaDirExists(jvm.getJdkMedia().getMediaDir().split(","), jvm.getHostName(), binaryDeployDir)) {
                distributeBinary(jvm.getHostName(), jdkMedia.getPath(), jdkMedia.getRemoteHostPath(), "");
            } else {
                LOGGER.warn("Jdk directories already exists, installation of {} skipped!", jvm.getJdkMedia().getName());
            }
        } else {
            final String errMsg = MessageFormat.format("JDK dir location is null or empty for JVM {0}. Not deploying JDK.", jvm.getJvmName());
            throw new ApplicationException(errMsg);
        }
        LOGGER.info("End deploy jdk for {}", jvm.getHostName());
    }

    private void distributeBinary(final String hostname, final String zipFileName, final String jwalaRemoteHome, final String exclude) {
        remoteCreateDirectory(hostname, jwalaRemoteHome);
        remoteSecureCopyFile(hostname, zipFileName, jwalaRemoteHome);
        remoteUnzipBinary(hostname, jwalaRemoteHome + "/" + getFileName(zipFileName), jwalaRemoteHome + "/", exclude);
        //remoteDeleteBinary(hostname, jwalaRemoteHome + "/" + getFileName(zipFileName));
    }

    private String getFileName(String fullPath){
        return fullPath.substring(fullPath.lastIndexOf(File.separator), fullPath.length());
    }

    @Override
    public void changeFileMode(final String hostname, final String mode, final String targetDir, final String target) {
        try {
            if (binaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target).getReturnCode().wasSuccessful()) {
                LOGGER.info("change file mode " + mode + " at targetDir " + targetDir);
            } else {
                String message = "Failed to change the file permissions in " + targetDir + "/" + target;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in change file mode at host: " + hostname + " mode: " + mode + " target: " + target;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteDeleteBinary(final String hostname, final String destination) {
        try {
            if (binaryDistributionControlService.deleteBinary(hostname, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully delete the binary {}", destination);
            } else {
                final String message = "error in deleting file " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {

            final String message = "Error in delete remote binary at host: " + hostname + " destination: " + destination;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteUnzipBinary(final String hostname, final String zipFileName, final String destination, final String exclude) {
        try {
            if (binaryDistributionControlService.unzipBinary(hostname, zipFileName, destination, exclude).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully unzipped the binary {}", zipFileName);
            } else {
                final String message = "cannot unzip from " + zipFileName + " to " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = String.format("Error in remote unzip binary at host: %s binaryLocation: %s destination: %s", hostname, zipFileName, destination);
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteSecureCopyFile(final String hostname, final String source, final String destination) {
        try {
            if (binaryDistributionControlService.secureCopyFile(hostname, source, destination).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully copied the binary {} over to {}", source, destination);
            } else {
                final String message = "error with scp of binary " + source + " to destination " + destination;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error issuing SCP to host " + hostname + " using source " + source +
                    " and destination " + destination + ". Exception is " + e.getMessage();
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public void remoteCreateDirectory(final String hostname, final String remoteDir) {
        LOGGER.debug("Attempting to create directory {} on host {}", remoteDir, hostname);
        try {
            if (binaryDistributionControlService.createDirectory(hostname, remoteDir).getReturnCode().wasSuccessful()) {
                LOGGER.info("successfully created directories {}", remoteDir);
            } else {
                final String message = "User does not have permission to create the directory " + remoteDir;
                LOGGER.error(message);
                throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message);
            }
        } catch (CommandFailureException e) {
            final String message = "Error in create remote directory at host: " + hostname + " destination: " + remoteDir;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
    }

    @Override
    public boolean remoteFileCheck(final String hostname, final String remoteFilePath) {
        LOGGER.info("Looking for the remote file {} on host {}", remoteFilePath, hostname);
        boolean result;
        try {
            result = binaryDistributionControlService.checkFileExists(hostname, remoteFilePath).getReturnCode().wasSuccessful();
        } catch (CommandFailureException e) {
            final String message = "Error in check remote File at host: " + hostname + " destination: " + remoteFilePath;
            LOGGER.error(message, e);
            throw new InternalErrorException(FaultType.REMOTE_COMMAND_FAILURE, message, e);
        }
        LOGGER.info("Remote file {} {}", remoteFilePath, result ? "found" : "not found");
        return result;
    }

    /**
     * Distrubute unzip.exe for windows and unzip.sh for other OS
     * @param hostname host name
     */
    @Override
    public void distributeUnzip(String hostname) {
        LOGGER.info("Start deploy unzip for {}", hostname);
        final String jwalaScriptsPath = ApplicationProperties.getRequired(PropertyKeys.REMOTE_SCRIPT_DIR);
        if (!remoteFileCheck(hostname, jwalaScriptsPath)) {
            remoteCreateDirectory(hostname, jwalaScriptsPath);
        }
        final String unzipFile = ApplicationProperties.get(PropertyKeys.LOCAL_JWALA_BINARY_DIR) + "/" + UNZIPEXE;
        File unzipExe = new File(unzipFile);
        if (unzipExe.isFile() && !remoteFileCheck(hostname, jwalaScriptsPath + "/" + UNZIPEXE)) {
            LOGGER.info("SCP {} ", unzipFile);
            remoteSecureCopyFile(hostname, unzipFile,  jwalaScriptsPath + "/" + UNZIPEXE);
            changeFileMode(hostname, "a+x", jwalaScriptsPath, UNZIPEXE);
        }
        final String remoteUnzipScriptPath = jwalaScriptsPath + "/" + UNZIP_SCRIPT_NAME;
        final String unzipScriptFile = ApplicationProperties.get(PropertyKeys.SCRIPTS_PATH)+ "/" + UNZIP_SCRIPT_NAME;
        if (!remoteFileCheck(hostname, remoteUnzipScriptPath)) {
            LOGGER.info("SCP {} " + unzipScriptFile);
            remoteSecureCopyFile(hostname, unzipScriptFile,  remoteUnzipScriptPath);
            changeFileMode(hostname, "a+x", jwalaScriptsPath, UNZIP_SCRIPT_NAME.getValue());
        }
        LOGGER.info("End deploy unzip for {}", hostname);
    }

    @Override
    public void backupFile(final String hostname, final String remoteFilePath) {
        binaryDistributionControlService.backupFile(hostname, remoteFilePath);
    }

    /**
     * Checks if the binary media directories already exists
     *
     * @param mediaDirs       the binary media directories to check
     * @param hostName        the host name where to check the binary media directories
     * @param binaryDeployDir the location where the binary media directories are in
     * @return true if all the binary media root directories already exists, otherwise false
     */
    private boolean checkIfMediaDirExists(final String[] mediaDirs, final String hostName, final String binaryDeployDir) {
        for (final String mediaDir : mediaDirs) {
            if (!remoteFileCheck(hostName, binaryDeployDir + "/" + mediaDir)) {
                return false;
            }
        }
        return true;
    }

    private String getUserNameFromSecurityContext() {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            LOGGER.error("No context found getting user name from SecurityContextHolder");
            throw new SecurityException("No context found getting user name from SecurityContextHolder");
        }

        final Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            LOGGER.debug("No authentication found getting user name from SecuriyContextHolder");
            return "";
        }

        return authentication.getName();
    }

}