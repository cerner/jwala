package com.cerner.jwala.persistence.service.jvm;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.media.MediaType;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.NotFoundException;
import com.cerner.jwala.dao.MediaDao;
import com.cerner.jwala.persistence.jpa.domain.JpaMedia;
import com.cerner.jwala.persistence.service.CommonGroupPersistenceServiceBehavior;
import com.cerner.jwala.persistence.service.CommonJvmPersistenceServiceBehavior;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@Transactional
public abstract class AbstractJvmPersistenceServiceTest {

    @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private GroupPersistenceService groupPersistenceService;

    @Autowired
    private MediaDao mediaDao;

    private CommonJvmPersistenceServiceBehavior jvmHelper;
    private CommonGroupPersistenceServiceBehavior groupHelper;
    private String userId;

    @Before
    public void setup() {
        User user = new User("testUser");

        JpaMedia media = new JpaMedia();
        media.setName("jdk");
        media.setType(MediaType.JDK);
        media.setLocalPath(new File("d:/not/a/real/path.zip").toPath());
        media.setRemoteDir(new File("d:/fake/remote/path").toPath());
        media.setMediaDir(new File("test-media").toPath());
        mediaDao.create(media);

        media = new JpaMedia();
        media.setName("tomcat");
        media.setType(MediaType.TOMCAT);
        media.setLocalPath(new File("d:/not/a/real/path.zip").toPath());
        media.setRemoteDir(new File("d:/fake/remote/path").toPath());
        media.setMediaDir(new File("test-media").toPath());
        mediaDao.create(media);

        jvmHelper = new CommonJvmPersistenceServiceBehavior(jvmPersistenceService);
        groupHelper = new CommonGroupPersistenceServiceBehavior(groupPersistenceService);
        userId = "TestUserId";
    }

    @Test
    public void testCreateJvm() {

        final String jvmName = "A Jvm Name";
        final String hostName = "A Host Name";

        final Jvm jvm = jvmHelper.createJvm(jvmName, hostName, 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        assertNotNull(jvm);
        assertNotNull(jvm.getId());
        assertEquals(jvmName,
                jvm.getJvmName());
        assertEquals(hostName,
                jvm.getHostName());
    }

    @Test(expected = EntityExistsException.class)
    public void testCreateJvmWithDuplicateName() {

        final Jvm existingJvm = jvmHelper.createJvm("A Jvm Name", "A Host Name", 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        final Jvm duplicateNameJvm = jvmHelper.createJvm(existingJvm.getJvmName(), "A different Host Name", 5, 4, 3, 2, 1,
                userId, new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);
    }

    @Test
    public void testUpdateJvm() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name", "A Host Name", 10, 9, 8, 7, 6, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        final String newJvmName = "A New Jvm Name";
        final String newHostName = "A New Host Name";
        final Integer newHttpPort = 5;
        final Integer newHttpsPort = 4;
        final Integer newRedirectPort = 3;
        final Integer newShutdownPort = 2;
        final Integer newAjpPort = 1;
        final Path newStatusPath = new Path("/def");
        final String newSystemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";
        final String newUserName = "new username";
        final String newEncryptedPassword = "the quick brown fox";
        final Identifier<Media> newJdkMediaId = new Identifier<>(1L);
        final Identifier<Media> newTomcatMediaId = new Identifier<>(2L);


        final Jvm updatedJvm = jvmHelper.updateJvm(jvm.getId(), newJvmName, newHostName, newHttpPort, newHttpsPort,
                newRedirectPort, newShutdownPort, newAjpPort, userId, newStatusPath, newSystemProperties, newUserName,
                newEncryptedPassword, newJdkMediaId, newTomcatMediaId);

        assertEquals(jvm.getId(),
                updatedJvm.getId());
        assertEquals(newJvmName,
                updatedJvm.getJvmName());
        assertEquals(newHostName,
                updatedJvm.getHostName());
        assertEquals(newHttpPort,
                updatedJvm.getHttpPort());
        assertEquals(newHttpsPort,
                updatedJvm.getHttpsPort());
        assertEquals(newRedirectPort,
                updatedJvm.getRedirectPort());
        assertEquals(newShutdownPort,
                updatedJvm.getShutdownPort());
        assertEquals(newAjpPort,
                updatedJvm.getAjpPort());
        assertEquals(newStatusPath,
                updatedJvm.getStatusPath());
        assertEquals(newUserName,
                updatedJvm.getUserName());
        assertEquals(newEncryptedPassword,
                updatedJvm.getEncryptedPassword());

    }

    @Test
    public void testUpdateJvmDoNotUpdatePassword() {

        jvmHelper.setUpdateJvmPassword(false);

        final Jvm jvm = jvmHelper.createJvm("jvm2", "host2", 10, 9, 8, 7, 6, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, "password", null, null);

        final String newJvmName = "jvmTwo";
        final String newHostName = "hostTwo";
        final Integer newHttpPort = 5;
        final Integer newHttpsPort = 4;
        final Integer newRedirectPort = 3;
        final Integer newShutdownPort = 2;
        final Integer newAjpPort = 1;
        final Path newStatusPath = new Path("/def");
        final String newSystemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";
        final String newUserName = "new username";
        final String newEncryptedPassword = "the quick brown fox";

        final Jvm updatedJvm = jvmHelper.updateJvm(jvm.getId(), newJvmName, newHostName, newHttpPort, newHttpsPort,
                newRedirectPort, newShutdownPort, newAjpPort, userId, newStatusPath, newSystemProperties, newUserName,
                newEncryptedPassword, null, null);

        assertEquals(jvm.getId(),
                updatedJvm.getId());
        assertEquals(newJvmName,
                updatedJvm.getJvmName());
        assertEquals(newHostName,
                updatedJvm.getHostName());
        assertEquals(newHttpPort,
                updatedJvm.getHttpPort());
        assertEquals(newHttpsPort,
                updatedJvm.getHttpsPort());
        assertEquals(newRedirectPort,
                updatedJvm.getRedirectPort());
        assertEquals(newShutdownPort,
                updatedJvm.getShutdownPort());
        assertEquals(newAjpPort,
                updatedJvm.getAjpPort());
        assertEquals(newStatusPath,
                updatedJvm.getStatusPath());
        assertEquals(newUserName,
                updatedJvm.getUserName());
        assertEquals("password",
                updatedJvm.getEncryptedPassword());
    }

    @Test(expected = EntityExistsException.class)
    public void testUpdateJvmWithDuplicateName() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name", "A Host Name", 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        final Jvm secondJvm = jvmHelper.createJvm("A different Jvm Name", "A different Host Name", 5, 4, 3, 2, 1,
                userId, new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        jvmHelper.updateJvm(secondJvm.getId(), jvm.getJvmName(), "Some different Host Name", 5, 4, 3, 2, 1, userId,
                new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistentJvm() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        jvmHelper.updateJvm(nonExistentJvm, "New Jvm Name", "New Host Name", 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);
    }

    @Test
    public void testGetJvm() {

        final Jvm jvm = jvmHelper.createJvm("A Jvm Name", "A Host Name", 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        final Jvm theSameJvm = jvmPersistenceService.getJvm(jvm.getId());

        assertEquals(jvm,
                theSameJvm);
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentJvm() {

        final Identifier<Jvm> nonExistentJvm = new Identifier<>(-123456L);

        jvmPersistenceService.getJvm(nonExistentJvm);
    }

    @Test
    public void testGetJvms() {

        final int numberToCreate = 10;

        for (int i = 1; i <= numberToCreate; i++) {
            jvmHelper.createJvm("Auto-created JVM Name " + i, "Auto-created Host Name " + i, 5, 4, 3, 2, 1, userId,
                    new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);
        }

        final List<Jvm> jvms = jvmPersistenceService.getJvms();

        assertTrue(jvms.size() >= numberToCreate);
    }

    @Test
    public void testRemoveJvm() {

        final Jvm jvm = jvmHelper.createJvm("JVM to Remove", "Hostname to Remove", 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        jvmPersistenceService.removeJvm(jvm.getId());

        try {
            jvmPersistenceService.getJvm(jvm.getId());
            fail("JVM should not have been found");
        } catch (final NotFoundException nfe) {
            assertTrue(true);
        }
    }

    @Test
    public void testRemoveJvmAssignedToAGroup() {

        final Jvm jvm = jvmHelper.createJvm("JVM assigned to group to Remove", "Hostname to Remove", 5, 4, 3, 2, 1,
                userId, new Path("/abc"), "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);

        final Group group = groupHelper.createGroup("Group to assign JVMs to",
                userId);

        final Identifier<Jvm> jvmId = jvm.getId();

        groupHelper.addJvmToGroup(group.getId(),
                jvmId,
                userId);

        jvmPersistenceService.removeJvm(jvmId);
    }

    @Test
    public void testRemoveJvmFromAllGroups() {

        final Jvm jvm = jvmHelper.createJvm("A new JVM", "A host name", 5, 4, 3, 2, 1, userId, new Path("/abc"),
                "EXAMPLE_OPTS=%someEnv%/someVal", null, null, null, null);
        final Identifier<Jvm> jvmId = jvm.getId();
        final Group firstGroup = groupHelper.createGroup("Group 1",
                userId);
        final Group secondGroup = groupHelper.createGroup("Group 2",
                userId);

        groupHelper.addJvmToGroup(firstGroup.getId(),
                jvmId,
                userId);
        groupHelper.addJvmToGroup(secondGroup.getId(),
                jvmId,
                userId);

        final Set<Identifier<Group>> assignedGroups = new HashSet<>();
        assignedGroups.add(firstGroup.getId());
        assignedGroups.add(secondGroup.getId());

        final Jvm jvmWithGroups = jvmPersistenceService.getJvm(jvmId);

        assertFalse(jvmWithGroups.getGroups().isEmpty());
        for (final Group group : jvmWithGroups.getGroups()) {
            assertTrue(assignedGroups.contains(group.getId()));
        }

        jvmPersistenceService.removeJvmFromGroups(jvmId);

        final Jvm jvmWithoutGroups = jvmPersistenceService.getJvm(jvmId);

        assertTrue(jvmWithoutGroups.getGroups().isEmpty());
    }

    @Test
    public void testFindJvmByExactName() {
        jvmHelper.createJvm("jvm-1", "testHost", 9101, 9102, 9103, -1, 9104, userId, new Path("./"), "", null, null, null, null);
        jvmHelper.createJvm("jvm-11", "testHost", 9111, 9112, 9113, -1, 9114, userId, new Path("./"), "", null, null, null, null);
        Jvm jvm = jvmPersistenceService.findJvmByExactName("jvm-1");
        assertEquals("jvm-1", jvm.getJvmName());
        assertEquals(new Integer(9101), jvm.getHttpPort());
        assertEquals(new Integer(9102), jvm.getHttpsPort());
        assertEquals(new Integer(9103), jvm.getRedirectPort());
        assertEquals(new Integer(-1), jvm.getShutdownPort());
        assertEquals(new Integer(9104), jvm.getAjpPort());
    }
}
