package com.cerner.jwala.ws.rest;

import com.cerner.jwala.ws.rest.v2.service.ResponseContent;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit test for {@link JsonResponseBuilder}
 * Created by Jedd Cuison on 2/14/2017
 */
public class JsonResponseBuilderTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testBuildJsonResponse() throws Exception {
        final JsonResponseBuilder<String> jsonResponseBuilder = new JsonResponseBuilder<>();
        final Response response = jsonResponseBuilder.setContent("the content").setMessage("success")
                .setStatus(Response.Status.OK).setStatusCode(0).build();
        System.out.println();
        assertEquals(200, response.getStatus());
        final ResponseContent<String> responseContent = (ResponseContent<String>) response.getEntity();
        assertEquals("the content", responseContent.getContent());
        assertEquals("success", responseContent.getMessage());
        assertEquals(200, responseContent.getStatus());
    }

}
