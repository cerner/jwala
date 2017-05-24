package com.cerner.jwala.service.binarydistribution.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionLockManager;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.cerner.jwala.control.AemControl.Properties.UNZIP_SCRIPT_NAME;

/**
 * Created by Arvindo Kinny on 10/11/2016
 */
public class BinaryDistributionServiceImpl implements BinaryDistributionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinaryDistributionServiceImpl.class);

    @Autowired
    protected SshConfiguration sshConfig;

    private static final String UNZIPEXE = "unzip.exe";

    public static final String EXCLUDED_FILES = "ReadMe.txt *--";

    @Autowired
    private BinaryDistributionControlService binaryDistributionControlService;

    @Autowired
    private BinaryDistributionLockManager binaryDistributionLockManager;

    @Autowired
    private HistoryFacadeService historyFacadeService;

    @Override
    public void distributeMedia(final String jvmOrWebServerName, final String hostName, Group[] groups, final Media media) {
        LOGGER.info("Deploying {}'s {} to {}", jvmOrWebServerName,  media.getName(), hostName);

        final String installPath = media.getRemoteDir().toString();
        if (StringUtils.isEmpty(installPath)) {
            throw new BinaryDistributionServiceException(media.getName() + " installation path cannot be blank!");
        }

        try {
            binaryDistributionLockManager.writeLock(hostName);
            if (!checkIfMediaDirExists(media.getRootDir().toString().split(","), hostName, installPath)) {
                historyFacadeService.write(hostName, Arrays.asList(groups), "Distribute " + media.getName(), EventType.SYSTEM_INFO,
                        getUserNameFromSecurityContext());
                distributeBinary(hostName, media.getLocalPath().toString(), installPath, EXCLUDED_FILES);
            } else {
                LOGGER.warn("{} already exists. Skipping {} installation.", installPath, media.getName());
            }
            LOGGER.info("{}'s {} successfully deployed to {}", jvmOrWebServerName, media.getName(), hostName);
        } finally {
            binaryDistributionLockManager.writeUnlock(hostName);
        }
    }

    private void distributeBinary(final String hostname, final String zipFileName, final String jwalaRemoteHome, final String exclude) {
        remoteCreateDirectory(hostname, jwalaRemoteHome);
        remoteSecureCopyFile(hostname, zipFileName, jwalaRemoteHome);
        remoteUnzipBinary(hostname, jwalaRemoteHome + "/" + getFileName(zipFileName), jwalaRemoteHome, exclude);
    }

    private String getFileName(String fullPath){
        return fullPath.substring(fullPath.lastIndexOf(File.separator) + 1, fullPath.length());
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
            if (binaryDistributionControlService.unzipBinary(hostname, Paths.get(zipFileName).normalize().toString(),
                    destination, exclude).getReturnCode().wasSuccessful()) {
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
        binaryDistributionControlService.backupFileWithMove(hostname, remoteFilePath);
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
            if (!remoteFileCheck(hostName, Paths.get(binaryDeployDir + "/" + mediaDir).normalize().toString())) {
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