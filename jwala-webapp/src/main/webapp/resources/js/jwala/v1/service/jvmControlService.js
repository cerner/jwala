var jvmControlService = function() {

    var control = function(jvmId, operation, thenCallback, caughtCallback) {
        return serviceFoundation.post("v1.0/jvms/" + jvmId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      thenCallback,
                                      caughtCallback,
                                      false);
    };

    var controlPromise = function(jvmId, operation) {
    	return serviceFoundation.promisedPost("v1.0/jvms/" + jvmId + "/commands",
                                              "json",
                                              JSON.stringify({ controlOperation : operation}));
    };

    return {
        startJvm : function(jvmId, thenCallback, caughtCallback) {
            return control(jvmId, "start", thenCallback, caughtCallback);
        },
        stopJvm : function(jvmId, thenCallback, caughtCallback) {
            return control(jvmId, "stop", thenCallback, caughtCallback);
        },
        heapDump : function(jvmId) {
            return controlPromise(jvmId, "heapdump");
        }
    };

}();