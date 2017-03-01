var historyService = {

    read: function(groupName, serverName) {
        if (serverName === undefined || serverName === null) {
            return serviceFoundation.promisedGet("v1.0/history/" + groupName,
                                                 "json", true);
        }
        return serviceFoundation.promisedGet("v1.0/history/" + groupName + "/" + serverName,
                                             "json", true);
    }

}