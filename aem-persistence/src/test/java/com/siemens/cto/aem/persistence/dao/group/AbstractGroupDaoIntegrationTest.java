package com.siemens.cto.aem.persistence.dao.group;

import com.siemens.cto.aem.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.domain.model.id.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@Transactional
public abstract class AbstractGroupDaoIntegrationTest {

    @Autowired
    private GroupDao groupDao;

    private Group preCreatedGroup;
    private String userName;

    @Before
    public void setUp() throws Exception {

        userName = "Test User Name";

        preCreatedGroup = groupDao.createGroup(GroupEventsTestHelper.createCreateGroupEvent("Pre-Created Group Name",
                                                                      userName));
    }

    @Test
    public void testCreateGroup() {

        final Event<CreateGroupRequest> createGroup = GroupEventsTestHelper.createCreateGroupEvent("newGroupName",
                                                                             userName);

        final Group actualGroup = groupDao.createGroup(createGroup);

        assertEquals(createGroup.getRequest().getGroupName(),
                     actualGroup.getName());
        assertNotNull(actualGroup.getId());
    }

    @Test(expected = BadRequestException.class)
    public void testCreateDuplicateGroup() {

        final Event<CreateGroupRequest> createGroup = GroupEventsTestHelper.createCreateGroupEvent(preCreatedGroup.getName(),
                                                                             userName);

        groupDao.createGroup(createGroup);
    }

    @Test
    public void testUpdateGroup() {

        final Event<UpdateGroupRequest> updateGroup = GroupEventsTestHelper.createUpdateGroupEvent(preCreatedGroup.getId(),
                                                                             "My New Name",
                                                                             userName);

        final Group actualGroup = groupDao.updateGroup(updateGroup);

        assertEquals(updateGroup.getRequest().getNewName(),
                     actualGroup.getName());
        assertEquals(updateGroup.getRequest().getId(),
                     actualGroup.getId());
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);

        groupDao.updateGroup(GroupEventsTestHelper.createUpdateGroupEvent(nonExistentGroupId,
                                                    "Unused",
                                                    userName));
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateDuplicateGroup() {

        final Group newGroup = groupDao.createGroup(GroupEventsTestHelper.createCreateGroupEvent("Group Name to turn into a duplicate",
                                                                           userName));

        groupDao.updateGroup(GroupEventsTestHelper.createUpdateGroupEvent(newGroup.getId(),
                                                    preCreatedGroup.getName(),
                                                    userName));
    }

    @Test
    public void testGetGroup() {

        final Identifier<Group> expectedGroupIdentifier = preCreatedGroup.getId();

        final Group group = groupDao.getGroup(expectedGroupIdentifier);

        assertEquals(preCreatedGroup.getName(),
                     group.getName());
        assertEquals(expectedGroupIdentifier,
                     group.getId());
    }

    @Test(expected = NotFoundException.class)
    public void testGetNonExistentGroup() {

        groupDao.getGroup(new Identifier<Group>(-123456L));
    }

    @Test
    public void testGetGroups() {

        groupDao.createGroup(GroupEventsTestHelper.createCreateGroupEvent("Auto-constructed Group " + (1),
                                                        "Auto-constructed User " + (1)));
        final List<Group> actualGroups = groupDao.getGroups();

        assertTrue(actualGroups.size() > 0);
    }

    @Test
    public void testFindGroups() {

        final String expectedContains = preCreatedGroup.getName().substring(3, 5);

        final List<Group> actualGroups = groupDao.findGroups(expectedContains);

        for(final Group group : actualGroups) {
            assertTrue(group.getName().contains(expectedContains));
        }
    }

    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testRemoveGroup() {

        final Identifier<Group> groupId = preCreatedGroup.getId();

        groupDao.removeGroup(groupId);

        try {
            groupDao.getGroup(groupId);
        } catch (final NotFoundException nfe) {
            //Success (This could be declared as expected in the @Test annotation, but I want to verify
            //that removeGroup() actually succeeded and didn't throw a NotFoundException itself
        }
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistent() {

        final Identifier<Group> nonExistentGroupId = new Identifier<>(-123456L);

        groupDao.removeGroup(nonExistentGroupId);
    }

}
