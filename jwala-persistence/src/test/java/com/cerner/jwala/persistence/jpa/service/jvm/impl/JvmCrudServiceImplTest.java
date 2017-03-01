package com.cerner.jwala.persistence.jpa.service.jvm.impl;

import com.cerner.jwala.common.configuration.TestExecutionProfile;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.request.jvm.CreateJvmRequest;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.dao.impl.MediaDaoImpl;
import com.cerner.jwala.persistence.configuration.TestJpaConfiguration;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.cerner.jwala.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.cerner.jwala.persistence.jpa.type.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {JvmCrudServiceImplTest.Config.class
        })
public class JvmCrudServiceImplTest {

    public static final String SERVER_XML = "server.xml";

    @Autowired
    private JvmCrudServiceImpl jvmCrudService;

    @Autowired
    private MediaDao mediaDao;

    private User user;
    private Jvm jvm;
    private JpaMedia jpaMedia;

    @Before
    public void setup() throws Exception {
        user = new User("testUser");

        String testJvmName = "testJvmName";
        final JpaMedia media = new JpaMedia();
        media.setName("test-media");
        media.setType(MediaType.JDK);
        media.setLocalPath(new File("d:/not/a/real/path.zip").toPath());
        media.setRemoteDir(new File("d:/fake/remote/path").toPath());
        media.setMediaDir(new File("test-media").toPath());
        jpaMedia = mediaDao.create(media);
        CreateJvmRequest createJvmRequest = new CreateJvmRequest(testJvmName, "testHostName", 100, 101, 102, 103, 104, new Path("./jwala.png"), "", null, null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia);
        jvm = new Jvm(Identifier.<Jvm>id(jpaJvm.getId()), jpaJvm.getName(), new HashSet<Group>());
    }

    @Test
    public void testUploadJvmTemplateXml() throws FileNotFoundException {
        final String expectedTemplateName = SERVER_XML;
        File testTemplate = new File("./src/test/resources/HttpdSslConfTemplate.tpl");
        Scanner scanner = new Scanner(testTemplate).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(jvm, expectedTemplateName,
                templateContent, StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return SERVER_XML;
            }
        };
        JpaJvmConfigTemplate result = jvmCrudService.uploadJvmConfigTemplate(uploadJvmTemplateRequest);
        assertEquals(expectedTemplateName, result.getTemplateName());

        // test get resource template names
        List<String> resultList = jvmCrudService.getResourceTemplateNames(jvm.getJvmName());
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        assertEquals(SERVER_XML, resultList.get(0));

        // test get resource template
        String resultText = jvmCrudService.getResourceTemplate(jvm.getJvmName(), SERVER_XML);
        assertFalse(resultText.isEmpty());

        // test update template
        jvmCrudService.updateResourceTemplate(jvm.getJvmName(), SERVER_XML, "<server>updated content</server>");
        String resultUpdate = jvmCrudService.getResourceTemplate(jvm.getJvmName(), SERVER_XML);
        assertTrue(resultUpdate.contains("updated content"));
    }

    @Test
    public void testGetJvmTemplate() {
        String result = jvmCrudService.getJvmTemplate(SERVER_XML, jvm.getId());
        assertNotNull(result);
    }

    @Test
    public void testGetJvmByExactName() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvm-1", "testHost", 9101, 9102, 9103, -1, 9104, new Path("./"), "", null, null, null);
        CreateJvmRequest createJvmWithSimilarNameRequest = new CreateJvmRequest("jvm-11", "testHost", 9111, 9112, 9113, -1, 9114, new Path("./"), "", null, null, null);
        JpaJvm jvmOne = jvmCrudService.createJvm(createJvmRequest, jpaMedia);
        JpaJvm jvmOneOne = jvmCrudService.createJvm(createJvmWithSimilarNameRequest, jpaMedia);

        Jvm foundJvm = jvmCrudService.findJvmByExactName("jvm-1");
        assertEquals(jvmOne.getName(), foundJvm.getJvmName());

        foundJvm = jvmCrudService.findJvmByExactName("jvm-11");
        assertEquals(jvmOneOne.getName(), foundJvm.getJvmName());
    }

    @Test
    public void testUpdateState() throws InterruptedException {
        final CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvmName", "hostName", 0, 0, 0, 0, 0,
                new Path("./jwala.png"), StringUtils.EMPTY, null, null, null);
        final JpaJvm newJpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia);
        final Identifier<Jvm> jpaJvmId = new Identifier<>(newJpaJvm.getId());
        assertEquals(1, jvmCrudService.updateState(jpaJvmId, JvmState.JVM_STOPPED));
    }

    @Test
    public void testUpdateErrorStatus() {
        final CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvmName", "hostName", 0, 0, 0, 0, 0,
                new Path("./jwala.png"), StringUtils.EMPTY, null, null, null);
        final JpaJvm newJpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia);
        final Identifier<Jvm> jpaJvmId = new Identifier<>(newJpaJvm.getId());
        assertEquals(1, jvmCrudService.updateErrorStatus(jpaJvmId, "error!"));
    }

    @Test
    public void testUpdateStateAndErrSts() {
        final CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvmName", "hostName", 0, 0, 0, 0, 0,
                new Path("./jwala.png"), StringUtils.EMPTY, null, null, null);
        final JpaJvm newJpaJvm = jvmCrudService.createJvm(createJvmRequest, jpaMedia);
        final Identifier<Jvm> jpaJvmId = new Identifier<>(newJpaJvm.getId());
        assertEquals(1, jvmCrudService.updateState(jpaJvmId, JvmState.JVM_FAILED, "error!"));
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public JvmCrudServiceImpl getJvmCrudServiceImpl() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public MediaDao getMediaDao() {
            return new MediaDaoImpl();
        }
    }
}
