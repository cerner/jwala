package com.cerner.jwala.service.state.impl;

import org.junit.Test;

import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.state.impl.InMemoryStateManagerServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link InMemoryStateManagerServiceImpl}.
 *
 * Created by Jedd Cuison on 4/18/2016.
 */
public class InMemoryStateManagerServiceImplTest {

    private InMemoryStateManagerService<String, String> inMemoryStateManagerService =
            new InMemoryStateManagerServiceImpl<>();

    @Test
    public void testAll() {
        inMemoryStateManagerService.put("key",  "val");
        assertTrue(inMemoryStateManagerService.containsKey("key"));
        assertEquals(inMemoryStateManagerService.get("key"), "val");
        inMemoryStateManagerService.remove("key");
        assertFalse(inMemoryStateManagerService.containsKey("key"));
    }
}
