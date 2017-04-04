var historyService = {

    read: function(groupName, serverName, numOfRec) {
        if (serverName === undefined || serverName === null) {
            return serviceFoundation.promisedGet("v1.0/history/" + groupName +"?numOfRec="+numOfRec,
                                                 "json", true);
        }
        return serviceFoundation.promisedGet("v1.0/history/" + groupName + "/" + serverName+"?numOfRec="+numOfRec,
                                             "json", true);
    }

}