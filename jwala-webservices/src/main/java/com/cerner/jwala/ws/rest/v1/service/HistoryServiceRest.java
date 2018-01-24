package com.cerner.jwala.ws.rest.v1.service;

import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Restful history service contract.
 *
 * Created by Jedd Cuison on 12/7/2015.
 */
@Api(value = "/history", tags = "history")
@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
public interface HistoryServiceRest {

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param numOfRec The Number of records to fetch. If null, all records are retrieved.
     * @return {@link Response} containing history data.
     */
    @GET
    @Path("/{groupName}")
    @ApiOperation(value = "Retrieve history data",
            response = JpaHistory.class
    )
    Response findHistory(@ApiParam(value = "The name of the group to retrieve data", required = true) @PathParam("groupName") String groupName,
                         @ApiParam(value = "The number of history records to retrieve", required = true) @QueryParam("numOfRec") Integer numOfRec);

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param serverName the server name, if null the history of all the servers belonging to the group will be queried
     * @param numOfRec The Number of records to fetch. If null, all records are retrieved.
     * @return {@link Response} containing history data.
     */
    @GET
    @Path("/{groupName}/{serverName}")
    @ApiOperation(value = "Retrieve history data for a specific host",
            response = JpaHistory.class
    )
    Response findHistory(@ApiParam(value = "The name of the group to retrieve data", required = true) @PathParam("groupName") String groupName,
                         @ApiParam(value = "The name of the host to retrieve data", required = true) @PathParam("serverName") String serverName,
                         @ApiParam(value = "The number of history records to retrieve", required = true) @QueryParam("numOfRec") Integer numOfRec);

}
