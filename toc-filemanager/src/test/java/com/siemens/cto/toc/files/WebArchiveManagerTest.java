package com.siemens.cto.toc.files;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryImpl;
import com.siemens.cto.toc.files.impl.PropertyFilesConfigurationImpl;
import com.siemens.cto.toc.files.impl.WebArchiveManagerImpl;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    WebArchiveManagerTest.CommonConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class WebArchiveManagerTest {
    
    static class MemoryNameSynthesizer implements NameSynthesizer {
        
        LinkedList<Path> appliedNames = new LinkedList<>();
        
        public Path unique(Path originalName) { 
            synchronized(appliedNames) { appliedNames.push(originalName); }
            return originalName;
        }
        
        public Path pop() { return appliedNames.pop(); }
        
    }
    
    static class CommonConfiguration { 
        
        @Bean FileSystem getPlatformFileSystem() {
            return FileSystems.getDefault();
        }

        @Bean NameSynthesizer getNameSynthesizer() {
            return new MemoryNameSynthesizer();
        }
        
        @Bean WebArchiveManager getWebArchiveManager() {
            return new WebArchiveManagerImpl();
        }
        
        @Bean Repository getFileSystemStorage() throws IOException {
            return new LocalFileSystemRepositoryImpl();
        }
        
        @Bean FilesConfiguration getFilesConfiguration() throws IOException {
            Path storageFolder = Files.createTempDirectory("archives");
            
            Properties p = new Properties();
            p.put(TocPath.WEB_ARCHIVE.getProperty(), storageFolder.toString());

            return new PropertyFilesConfigurationImpl(p);
        }
    }
    
    @Autowired 
    WebArchiveManager webArchiveManager;

    @Autowired 
    Repository fsRepository;
    
    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Autowired 
    NameSynthesizer nameSynthesizer;

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;
    
    @Before
    public void setup() {
        ByteBuffer buf = java.nio.ByteBuffer.allocate(1*1024*1024); // 1 Mb file
        buf.asShortBuffer().put((short)0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());
        
        app = new Application(null, null, null, null, null);        
    }
    
    @After 
    public void tearDown() {
        
    }
    
    private void testResults(long expectedSize, RepositoryAction result) throws IOException {
        assertEquals("Size mismatch after store of file.", expectedSize, (long)result.getLength());
        
        Path storedPath = ((MemoryNameSynthesizer)nameSynthesizer).pop();
        
        FileChannel fc = FileChannel.open(filesConfiguration.getConfiguredPath(TocPath.WEB_ARCHIVE).resolve(storedPath), StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE );
        
        assertNotNull(fc);
        
        ByteBuffer dst = ByteBuffer.allocate(2);
        
        fc.read(dst);
        dst.flip();
        
        assertTrue(dst.asShortBuffer().get(0) == (short)0xc0de);
        
        fc.close();        
    }
    
    @Test
    public void testWriteArchive() throws IOException { 
                
        UploadWebArchiveCommand cmd = new UploadWebArchiveCommand(app, "filename.war", 1*1024*1024L, uploadedFile);
        cmd.validateCommand(); // may trigger BadRequestException
        
        
        testResults(
                1*1024*1024L,
                webArchiveManager.store(Event.<UploadWebArchiveCommand>create(cmd, AuditEvent.now(new User("test-user")))));  
    }

}
