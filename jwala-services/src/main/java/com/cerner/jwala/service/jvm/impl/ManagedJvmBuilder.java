package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.FileUtility;
import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.properties.PropertyKeys;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.service.jvm.exception.JvmServiceException;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static com.cerner.jwala.common.properties.PropertyKeys.TOMCAT_MANAGER_XML_SSL_PATH;

/**
 * Created by Steven Ger on 12/16/16.
 */
public class ManagedJvmBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedJvmBuilder.class);

    private static final String PATHS_RESOURCE_TEMPLATES = "paths.resource-templates";
    private static final String INSTALL_SERVICE_TEMPLATE = "install-service-jvm.bat.tpl";
    private static final String SERVER_XML_TEMPLATE = "server.xml.tpl";
    public static final String INSTALL_SERVICE_BAT = "install-service.bat";
    public static final String SERVER_XML = "server.xml";

    private Jvm jvm;
    private FileUtility fileUtility;
    private ResourceService resourceService;

    public ManagedJvmBuilder() {
    }

    public ManagedJvmBuilder jvm(Jvm jvm) {
        this.jvm = jvm;
        return this;
    }

    public ManagedJvmBuilder fileUtility(FileUtility fileUtility) {
        this.fileUtility = fileUtility;
        return this;
    }

    public ManagedJvmBuilder resourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
        return this;
    }

    public ManagedJvmBuilder overwriteServerXml() {
        createServerXml();
        return this;
    }

    public File getStagingDir() {
        return new File(getWorkDir() + "/" + jvm.getJvmName());
    }

    public ManagedJvmBuilder build() {
        return prepare().
                stageTomcat().
                addScripts().
                overwriteServerXml().
                addSSLManagerXml().
                addLibs().
                createDirInJvmTomcat("/logs").
                jar();
    }

    private ManagedJvmBuilder addSSLManagerXml() {
        final String destManagerXmlPath = ApplicationProperties.get(TOMCAT_MANAGER_XML_SSL_PATH);
        final String templatesPath = ApplicationProperties.getRequired("paths.resource-templates");
        final String managerXmlFileName = "/manager.xml";
        final File srcManagerXmlFile = new File(templatesPath + managerXmlFileName);
        if (null == destManagerXmlPath || destManagerXmlPath.isEmpty() || !srcManagerXmlFile.exists()) {
            LOGGER.info("No source manager.xml file template at {} or destination path specified at {}", srcManagerXmlFile.getAbsolutePath(), TOMCAT_MANAGER_XML_SSL_PATH.getPropertyName());
            return this;
        }

        String generatedDestDir = getTomcatStagingDir().getAbsolutePath() + destManagerXmlPath + managerXmlFileName;
        LOGGER.info("Copying {} to generated JVM destination {}", srcManagerXmlFile.getAbsolutePath(), generatedDestDir);
        try {
            FileUtils.copyFile(srcManagerXmlFile, new File(generatedDestDir));
        } catch (IOException e) {
            LOGGER.error("Error adding the manager.xml to the generated staging directory. Could not copy {} to {}", srcManagerXmlFile.getAbsolutePath(), generatedDestDir, e);
            throw new JvmServiceException(e);
        }
        return this;
    }

    protected ManagedJvmBuilder createDirInJvmTomcat(final String createDirName) {

        File createDir = new File(getTomcatStagingDir().getAbsoluteFile() + createDirName);
        if (!createDir.exists() && !createDir.mkdirs()) {
            LOGGER.error("Failed to create JVM tomcat directory " + createDir);
            throw new InternalErrorException(FaultType.BAD_STREAM, "Failed to create directory" + createDir.getAbsolutePath());
        }

        return this;
    }

    protected ManagedJvmBuilder addLibs() {
        final File stagingLibDir = getTomcatStagingLibDir();

        LOGGER.debug("Copy agent dir: {} to instance template {}", getAgentDir(), stagingLibDir.getAbsolutePath());
        File jwalaAgentDir = new File(getAgentDir());
        if (jwalaAgentDir.exists() && jwalaAgentDir.isDirectory()) {
            File[] files = jwalaAgentDir.listFiles();

            if (files == null || files.length == 0) {
                throw new JvmServiceException("Agent files not found in directory " + jwalaAgentDir.getAbsolutePath());
            }

            for (File file : files) {
                try {
                    FileUtils.copyFileToDirectory(file, stagingLibDir);
                } catch (IOException e) {
                    throw new JvmServiceException(e);
                }
            }
        }

        return this;
    }

    protected ManagedJvmBuilder prepare() {
        final String backupExtension = "." + System.currentTimeMillis();
        if (getStagingDir().exists()) {
            try {
                File backUp = new File(getStagingDir() + backupExtension);
                LOGGER.debug("Dir {} already exists. Backing it up to {}", getStagingDir().getAbsoluteFile(), backUp.getAbsoluteFile());
                FileUtils.moveDirectory(getStagingDir(), backUp);
            } catch (IOException e) {
                throw new JvmServiceException(e);
            }
        }

        return this;
    }

    protected ManagedJvmBuilder stageTomcat() {
        LOGGER.debug("Unzipping the tomcat binary {} to {} ", getTomcatBinary(), getStagingDir());

        fileUtility.unzip(getTomcatBinary(), getStagingDir());
        return this;
    }

    protected ManagedJvmBuilder addScripts() {
        createServiceInstallScript();
        createServiceControlScripts();

        return this;
    }

    protected void createServiceControlScripts() {
        final String commandsScriptsPath = ApplicationProperties.getRequired(PropertyKeys.SCRIPTS_PATH);
        final File generatedJvmDestDirBin = new File(getTomcatStagingDir() + "/bin");
        try {
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.START_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.STOP_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.HEAP_DUMP_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.THREAD_DUMP_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);
            FileUtils.copyFileToDirectory(new File(commandsScriptsPath + "/" + AemControl.Properties.SERVICE_STATUS_SCRIPT_NAME.getValue()), generatedJvmDestDirBin);
        } catch (IOException e) {
            throw new JvmServiceException(e);
        }
    }

    protected void createServiceInstallScript() {
        final String tomcatBinDir = getTomcatStagingDir().getAbsolutePath() + "/bin";
        String generatedText = generateServiceScriptContent();

        LOGGER.debug("Saving template to {}", tomcatBinDir + "/" + INSTALL_SERVICE_BAT);

        File templateFile = new File(tomcatBinDir + "/" + INSTALL_SERVICE_BAT);
        if (INSTALL_SERVICE_BAT.endsWith(".bat")) {
            generatedText = generatedText.replaceAll("\n", "\r\n");
        }
        try {
            FileUtils.writeStringToFile(templateFile, generatedText, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new JvmServiceException(e);
        }
    }

    protected String generateServiceScriptContent() {
        try {
            final Path templatesPath = Paths.get(ApplicationProperties.get(PATHS_RESOURCE_TEMPLATES));
            final FileInputStream installServiceBatTemplateContent = new FileInputStream(templatesPath.toAbsolutePath()
                    .normalize().toString() + "/" + new File(INSTALL_SERVICE_TEMPLATE));
            String scriptContent = resourceService.generateResourceFile("install_service.bat",
                    IOUtils.toString(installServiceBatTemplateContent, StandardCharsets.UTF_8),
                    resourceService.generateResourceGroup(), jvm,
                    ResourceGeneratorType.TEMPLATE);

            LOGGER.debug("Generated install_service.bat text: {}", scriptContent);

            return scriptContent;
        } catch (final IOException e) {
            throw new JvmServiceException("Failed to generate install service batch file!", e);
        }
    }

    protected void createServerXml() {
        final String tomcatConfDir = getTomcatStagingDir().getAbsolutePath() + "/conf";

        if (ifGroupServerXmlExists()) {
            //return, one will be created as a resource
            return;
        }

        String generatedText = generateServerXml();

        LOGGER.debug("Saving template to {}", tomcatConfDir + "/" + SERVER_XML);

        File templateFile = new File(tomcatConfDir + "/" + SERVER_XML);

        try {
            FileUtils.writeStringToFile(templateFile, generatedText, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new JvmServiceException(e);
        }
    }

    protected boolean ifGroupServerXmlExists() {
        //get the groups
        Set<Group> groups = jvm.getGroups();
        if (groups != null) {
            //generate the xml
            for (Group group : groups) {
                if (resourceService.checkJvmFileExists(group.getName(), jvm.getJvmName(), "server.xml")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected String generateServerXml() {
        try {
            final Path templatesPath = Paths.get(ApplicationProperties.get(PATHS_RESOURCE_TEMPLATES));
            final FileInputStream installServiceBatTemplateContent = new FileInputStream(templatesPath.toAbsolutePath()
                    .normalize().toString() + "/" + new File(SERVER_XML_TEMPLATE));
            String scriptContent = resourceService.generateResourceFile("server.xml",
                    IOUtils.toString(installServiceBatTemplateContent, Charset.forName("UTF-8")),
                    resourceService.generateResourceGroup(), jvm,
                    ResourceGeneratorType.TEMPLATE);

            LOGGER.debug("Generated install-service.bat text: {}", scriptContent);

            return scriptContent;
        } catch (final IOException e) {
            throw new JvmServiceException("Failed to generate install service batch file!", e);
        }
    }

    protected ManagedJvmBuilder jar() {
        LOGGER.info("Creating jar {} from dir {}", getStagingJvmJarPath(), getStagingDir());

        final List<File> files = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get(getStagingDir().getAbsolutePath()),
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            files.add(file.toFile());
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new ApplicationException(e);
        }

        fileUtility.createJarArchive(getStagingJvmJarPath(), files.toArray(new File[0]), getStagingDir().getAbsolutePath());

        LOGGER.debug("Managed jvm jar instance saved to {}", getStagingJvmJarPath());

        return this;
    }

    protected File getTomcatStagingDir() {
        String[] dirs = getStagingDir().list();
        if (dirs == null || dirs.length == 0) {
            throw new ApplicationException("Staging directory " + getStagingDir().getAbsolutePath() + " is empty.");
        }

        return new File(getStagingDir().getAbsolutePath() + "/" + dirs[0]);
    }

    private File getTomcatStagingLibDir() {
        final File generatedJvmDestDirLib = new File(getTomcatStagingDir().getAbsolutePath() + "/lib");
        if (!generatedJvmDestDirLib.exists() && !generatedJvmDestDirLib.mkdir()) {
            LOGGER.warn("Failed to create directory " + generatedJvmDestDirLib.getAbsolutePath());
        }

        return generatedJvmDestDirLib;
    }

    private File getTomcatBinary() {
        return new File(getBinaryDir() + "/" + getTomcatBinaryName());
    }

    private String getTomcatBinaryName() {
        return ApplicationProperties.getRequired(PropertyKeys.TOMCAT_BINARY_FILE_NAME);
    }

    private String getBinaryDir() {
        return ApplicationProperties.get("jwala.binary.dir");
    }

    private String getWorkDir() {
        return ApplicationProperties.get("paths.generated.resource.dir");
    }

    private String getAgentDir() {
        return ApplicationProperties.get("jwala.agent.dir");
    }

    private File getStagingJvmJarPath() {
        return new File(getWorkDir() + "/" + jvm.getJvmName() + "/" + jvm.getJvmName() + ".jar");
    }
}
