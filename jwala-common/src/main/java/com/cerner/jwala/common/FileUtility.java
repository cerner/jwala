package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.ApplicationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

/**
 * A utility class for file related operations
 * <p>
 * Created by Jedd Anthony Cuison on 12/1/2016
 */
@Component
public class FileUtility {
    private static final String ZIP_FILE_EXT = ".zip";
    private static final String TAR_GZIP_FILE_EXT = ".gz";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtility.class);
    private ZipFile zipFile;

    /**
     * Unzips the file to the specified destination
     *
     * @param destination the destination e.g. c:/scratch
     */
    public void unzip(final File zipFile, final File destination) {
        if (!destination.exists() && !destination.mkdir()) {
            throw new FileUtilityException("Failed to create zip file destination directory \"" + destination.getAbsolutePath() + "\"!");
        }
        JarFile jarFile = null;
        final long startTime = System.currentTimeMillis();
        try {
            LOGGER.debug("Start unzip {}", zipFile.getAbsoluteFile());
            jarFile = new JarFile(zipFile);
            final Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = (JarEntry) entries.nextElement();
                final File f = new File(destination + File.separator + jarEntry.getName());
                if (jarEntry.isDirectory()) {
                    if (!f.mkdirs()) {
                        throw new ApplicationException("Failed to create directory " + f.getAbsoluteFile());
                    }
                    continue;
                }
                final InputStream in = jarFile.getInputStream(jarEntry);
                final FileOutputStream fos = new FileOutputStream(f);
                BufferedOutputStream bufout = new BufferedOutputStream(fos);

                while (in.available() > 0) {
                    bufout.write(in.read());
                }
                bufout.close();
                in.close();
            }
        } catch (final IOException e) {
            throw new FileUtilityException("Failed to unpack " + zipFile.getAbsolutePath() + "!", e);
        } finally {
            closeJarFile(jarFile);
            LOGGER.debug("End unzip {} in {} ms", zipFile.getAbsoluteFile(), (System.currentTimeMillis() - startTime));
        }
    }

    /**
     * Closes a {@link JarFile}
     *
     * @param jarFile the jar file to close
     */
    private void closeJarFile(final JarFile jarFile) {
        try {
            if (jarFile != null) {
                jarFile.close();
            }
        } catch (final IOException e) {
            LOGGER.error("Failed to close {}", jarFile.getName(), e);
        }
    }


    /**
     * Gets the root directories of a zip file
     *
     * @param zipFilename the zip filename
     * @return zip file root directories
     */
    public Set<String> getZipRootDirs(final String zipFilename) {
        final Set<String> zipRootDirs = new HashSet<>();
        try {
            if (zipFilename.indexOf(TAR_GZIP_FILE_EXT) > 0) {
                return getGZipRootDirs(zipFilename);
            }
            final ZipFile zipFile = new ZipFile(zipFilename);
            final Enumeration zipEntryEnumeration = zipFile.entries();
            while (zipEntryEnumeration.hasMoreElements()) {
                final String zipEntry = zipEntryEnumeration.nextElement().toString();
                final int slashIdx = zipEntry.indexOf('/');
                if (slashIdx > 0) {
                    zipRootDirs.add(zipEntry.substring(0, slashIdx));
                }
            }
            zipFile.close();
            return zipRootDirs;
        } catch (final IOException e) {
            throw new FileUtilityException(MessageFormat.format("Failed to get {0} parent path!", zipFilename), e);
        }
    }

    /**
     * Create archive in zip format
     *
     * @param archiveFile
     * @param filesToBeJared
     * @param parent
     */
    public void createJarArchive(File archiveFile, File[] filesToBeJared, String parent) {

        final int BUFFER_SIZE = 10240;
        try (
                FileOutputStream stream = new FileOutputStream(archiveFile);
                JarOutputStream out = new JarOutputStream(stream, new Manifest())) {

            byte buffer[] = new byte[BUFFER_SIZE];

            // Open archive file
            for (File aFileTobeJared : filesToBeJared) {
                if (aFileTobeJared == null || !aFileTobeJared.exists() || aFileTobeJared.isDirectory()) {
                    continue; // Just in case...
                }

                LOGGER.debug("Adding " + aFileTobeJared.getPath());

                File parentDir = new File(parent);

                String relPath = aFileTobeJared.getCanonicalPath()
                        .substring(parentDir.getParentFile().getCanonicalPath().length() + 1,
                                aFileTobeJared.getCanonicalPath().length());

                relPath = relPath.replace("\\", "/");

                // Add archive entry
                JarEntry jarAdd = new JarEntry(relPath);
                jarAdd.setTime(aFileTobeJared.lastModified());
                out.putNextEntry(jarAdd);


                // Write file to archive
                try (FileInputStream in = new FileInputStream(aFileTobeJared)) {
                    while (true) {
                        int nRead = in.read(buffer, 0, buffer.length);
                        if (nRead <= 0)
                            break;
                        out.write(buffer, 0, nRead);
                    }
                }
            }

            LOGGER.debug("Adding files to jar completed");
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Get the list of tar gzip
     *
     * @param tarGzipFile tar gzip filename
     * @return the list of extracted filenames
     * @throws IOException
     */
    public static Set<String> getGZipRootDirs(String tarGzipFile) {
        final Set<String> zipRootDirs = new HashSet<>();
        TarArchiveInputStream in = null;
        try {
            in = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(tarGzipFile)));
            TarArchiveEntry entry = in.getNextTarEntry();
            while (entry != null) {
                final int slashIdx = entry.getName().indexOf('/');
                if (slashIdx > 0) {
                    zipRootDirs.add(entry.getName().substring(0, slashIdx));
                }
                entry = in.getNextTarEntry();
            }
        } catch (Exception ex) {
            throw new ApplicationException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOGGER.error("Error closing gzip stream", e);
            }
        }

        return zipRootDirs;

    }

    public static String getCheckSum(final String fileAbsolutePath) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(fileAbsolutePath));
            return DigestUtils.sha256Hex(fileInputStream);
        } catch (FileNotFoundException e) {
            final String errMsg = MessageFormat.format("Failed to get the checksum for non-existent file: {0}", fileAbsolutePath);
            LOGGER.error(errMsg);
            throw new FileUtilityException(errMsg);
        } catch (IOException e) {
            final String errMsg = MessageFormat.format("Failed to generate the checksum while reading file: {0}", fileAbsolutePath);
            LOGGER.error(errMsg);
            throw new FileUtilityException(errMsg);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

}