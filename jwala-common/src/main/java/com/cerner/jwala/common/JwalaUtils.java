package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.ApplicationException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static com.cerner.jwala.common.FileUtility.getCheckSum;

/**
 * A utility class for miscellaneous jwala specific operations
 * <p>
 * Created by Arvindo Kinny on 12/1/2016
 */
public class JwalaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwalaUtils.class);

    /**
     * Method to get IP from hostname
     *
     * @param hostname host name to look for IP
     * @return IPv4 address
     */
    public static String getHostAddress(String hostname) {
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException ex) {
            String message = "Invalid Hostname " + hostname;
            LOGGER.error(message, ex);
            throw new ApplicationException(message, ex);
        }
    }

    /**
     * Given the path to a file, check the existing files in the jwala data/binaries directory to see if there is an
     * already existing file that matches based on the SHA-256 checksum
     * @param uploadedFilePath the absolute path of the file to compare
     * @param binariesAbsPath the list of existing files in data/binaries by their absolute paths
     * @return either the uploadFilePath if no matches were found or the absolute path of the existing binary
     */
    public static String getPathForExistingBinary(final String uploadedFilePath, List<String> binariesAbsPath) {
        if (!binariesAbsPath.isEmpty()) {
            String initialDestCheckSum = getCheckSum(uploadedFilePath);
            File uploadedFile = new File(uploadedFilePath);
            for (String existingBinaryAbsPath : binariesAbsPath) {
                File existingBinaryFile = new File(existingBinaryAbsPath);
                if (!existingBinaryFile.equals(uploadedFile) && getCheckSum(existingBinaryAbsPath).equals(initialDestCheckSum)) {
                    LOGGER.warn("Uploading {}, but found existing binary {} so using that one instead.", uploadedFilePath, existingBinaryAbsPath);
                    LOGGER.warn("Deleting uploaded file {}", uploadedFilePath);
                    try {
                        FileUtils.forceDelete(uploadedFile);
                    } catch (IOException e) {
                        LOGGER.warn("Failed to delete uploaded file {}", uploadedFilePath, e);
                    }
                    return existingBinaryAbsPath;
                }
            }
        }
        return uploadedFilePath;
    }


}
