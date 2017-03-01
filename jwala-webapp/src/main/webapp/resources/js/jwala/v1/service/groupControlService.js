var groupControlService = function() {

    var controlGroups = function(operation) {
        return serviceFoundation.promisedPost("v1.0/groups/commands",
                                              "json",
                                              JSON.stringify({controlOperation : operation}));
    };

    var control = function(groupId, operation) {
        return serviceFoundation.promisedPost("v1.0/groups/" + groupId + "/commands",
                                              "json",
                                              JSON.stringify({ controlOperation : operation}));
    };

    var controlJvms = function(groupId, operation) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/jvms/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      undefined,
                                      undefined,
                                      false);
    };

    var controlWebServers = function(groupId, operation) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/webservers/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      undefined,
                                      undefined,
                                      false);
    };

    var generate = function(groupId, entity, successCallback, errorCallback) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/" + entity + "/conf/deploy",
                                      "json",
                                      JSON.stringify({ controlOperation : "generate"}),
                                      successCallback,
                                      errorCallback,
                                      false);
    };
    var drain = function(groupName, errorCallback) {
        return serviceFoundation.post("v1.0/balancermanager/" + encodeURIComponent(groupName),
                                      "json",
                                       "",
                                       undefined,
                                       errorCallback,
                                       false);
    };

    return {
        startGroups: function() {
            return controlGroups("start");
        },
        stopGroups: function() {
            return controlGroups("stop");
        },
        startGroup : function(groupId) {
            return control(groupId, "start");
        },
        stopGroup : function(groupId) {
            return control(groupId, "stop");
        },
        startJvms : function(groupId) {
            return controlJvms(groupId, "start");
        },
        stopJvms : function(groupId) {
            return controlJvms(groupId, "stop");
        },
        startWebServers : function(groupId) {
            return controlWebServers(groupId, "start");
        },
        stopWebServers : function(groupId) {
            return controlWebServers(groupId, "stop");
        },
        generateWebServers : function(groupId, successCallback, errorCallback) {
            return generate(groupId, "webservers", successCallback, errorCallback);
        },
        generateJvms : function(groupId, successCallback, errorCallback) {
            return generate(groupId, "jvms", successCallback, errorCallback)
        },
        drainWebServers : function(groupName, errorCallback) {
            return drain(groupName, errorCallback);
        }
    };

}();