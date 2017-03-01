var webServerControlService = function() {

    var control = function(webServerId, operation, thenCallback, caughtCallback) {
        return serviceFoundation.post("v1.0/webservers/" + webServerId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      thenCallback,
                                      caughtCallback,
                                      false);
    };

    return {
        startWebServer : function(webServerId, thenCallback, caughtCallback) {
            return control(webServerId, "start", thenCallback, caughtCallback);
        },
        stopWebServer : function(webServerId, thenCallback, caughtCallback) {
            return control(webServerId, "stop", thenCallback, caughtCallback);
        }
    };

}();