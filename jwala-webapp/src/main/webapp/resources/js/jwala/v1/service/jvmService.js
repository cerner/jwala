    var jvmService = {
    serializedJvmFormToJson: function(serializedArray, forUpdate) {
        var json = {};
        var groupIdArray = [];
        $.each(serializedArray, function() {

            var excludeProp = false;
            if (forUpdate !== true && this.name === "id") {
                excludeProp = true;
            }

            if (excludeProp !== true) {
                if (this.name.indexOf("groupSelector[]") > -1) {
                    var id = {};
                    id["groupId"] = this.value;
                    groupIdArray.push(id);
                    // groupIdArray.push(this.value);
                } else if (this.name === "id") {
                    json["jvmId"] = this.value;
                } else {
                    json[this.name] = this.value;
                }
            }

        });
        json["groupIds"] = groupIdArray;
        return JSON.stringify(json);
    },
    insertNewJvm: function(jvmName,
                           groupIds,
                           hostName,
                           statusPath,
                           sysProps,
                           httpPort,
                           httpsPort,
                           redirectPort,
                           shutdownPort,
                           ajpPort,
                           userName,
                           encryptedPassword,
                           jdkMediaId,
                           tomcatVersion,
                           successCallback,
                           errorCallback) {
        return serviceFoundation.post("v1.0/jvms",
                                      "json",
                                      JSON.stringify({jvmName: jvmName,
                                                      groupIds: groupIds,
                                                      hostName:hostName,
                                                      statusPath:statusPath,
                                                      systemProperties:sysProps,
                                                      httpPort: httpPort,
                                                      httpsPort: httpsPort,
                                                      redirectPort: redirectPort,
                                                      shutdownPort: shutdownPort,
                                                      ajpPort: ajpPort,
                                                      userName: userName,
                                                      encryptedPassword: encryptedPassword,
                                                      jdkMediaId: jdkMediaId,
                                                      tomcatVersion: tomcatVersion}),
                                                      successCallback,
                                                      errorCallback);
    },
    updateJvm: function(jvm, updateJvmPassword, successCallback, errorCallback) {
        jvm = this.serializedJvmFormToJson(jvm, true);
        return serviceFoundation.put("v1.0/jvms?updateJvmPassword=" + updateJvmPassword, "json", jvm, successCallback,
                                     errorCallback );
    },
    deleteJvm: function(id, hardDelete) {
        hardDelete = hardDelete ? true : false;
        return serviceFoundation.promisedDel("v1.0/jvms/" + id + "?hardDelete=" + hardDelete, "json");
    },
    getJvm : function(id, responseCallback) {
        return serviceFoundation.get("v1.0/jvms/" + id, "json", responseCallback);
    },
    getJvms : function(responseCallback) {
        let restCall = "v1.0/jvms?all";
        if (responseCallback) {
            return serviceFoundation.get(restCall, "json", responseCallback);
        }
        return serviceFoundation.promisedGet(restCall, "json");
    },
    diagnoseJvm: function(id, responseCallback) {
    	return serviceFoundation.get("v1.0/jvms/" + id + "/diagnosis", "json", responseCallback);
    },
    getResources : function(jvmName, responseCallback) {
        return serviceFoundation.get("v1.0/jvms/" + encodeURIComponent(jvmName) + "/resources/name", "json", responseCallback);
    },
    previewResourceFile: function(resourceTemplateName, jvmName, groupName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/jvms/" + encodeURIComponent(jvmName) + "/resources/preview/" + encodeURIComponent(resourceTemplateName) + ";groupName=" + encodeURIComponent(groupName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    deployJvmConfAllFiles: function(jvmName, responseCallback, caughtCallback) {
        return serviceFoundation.put("v1.0/jvms/" + jvmName + "/conf", "json", null, responseCallback, caughtCallback, false);
    },
    updateResourceTemplate: function(jvmName, resourceTemplateName, resourceTemplateContent) {
        return serviceFoundation.promisedPut("v1.0/jvms/" + encodeURIComponent(jvmName) + "/resources/template/" + encodeURIComponent(resourceTemplateName),
                                             "json",
                                             resourceTemplateContent,
                                             false,
                                             "text/plain; charset=utf-8");
    }
};
