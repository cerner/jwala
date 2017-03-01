package com.cerner.jwala.common.domain.model.app;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;

import static org.junit.Assert.assertEquals;

public class ApplicationRequestTest {

    private CreateApplicationRequest initCreateAndTest(String name, String ctx, Long id) {
        CreateApplicationRequest cac = new CreateApplicationRequest(
                Identifier.id(id, Group.class),
                name, 
                ctx, true, true, false);
        assertEquals(name, cac.getName());
        assertEquals(ctx, cac.getWebAppContext());
        assertEquals(Identifier.id(id, Group.class), cac.getGroupId());
        
        return cac;
    }
    
    @Test
    public void testCreateOk() {
        CreateApplicationRequest cac = initCreateAndTest("name", "ctx", 1L);
        cac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateFailName() {
        CreateApplicationRequest cac = initCreateAndTest(null, "ctx", 1L);
        cac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateFailContext() {
        CreateApplicationRequest cac = initCreateAndTest("name", null, 1L);
        cac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testCreateFailId() {
        CreateApplicationRequest cac = initCreateAndTest("name", "ctx", null);
        cac.validate();
    }

    private UpdateApplicationRequest initUpdateAndTest(Long appId, String name, String ctx, Long groupId) {
        UpdateApplicationRequest uac = new UpdateApplicationRequest(
                Identifier.id(appId, Application.class),
                Identifier.id(groupId, Group.class),
                ctx,
                name,
                true, true, false);
        assertEquals(name, uac.getNewName());
        assertEquals(ctx, uac.getNewWebAppContext());
        assertEquals(Identifier.id(appId, Application.class), uac.getId());
        assertEquals(Identifier.id(groupId, Group.class), uac.getNewGroupId());
        
        return uac;
    }
    
    @Test
    public void testUpdateOk() {
        UpdateApplicationRequest uac = initUpdateAndTest(2L, "name", "ctx", 1L);
        uac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailId() {
        UpdateApplicationRequest uac = initUpdateAndTest(null, null, "ctx", 1L);
        uac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailName() {
        UpdateApplicationRequest uac = initUpdateAndTest(2L, null, "ctx", 1L);
        uac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailContext() {
        UpdateApplicationRequest uac = initUpdateAndTest(2L, "name", null, 1L);
        uac.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateFailGroupId() {
        UpdateApplicationRequest uac = initUpdateAndTest(2L, "name", "ctx", null);
        uac.validate();
    }

}
