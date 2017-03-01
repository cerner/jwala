var webAppService = {
    baseUrl: "v1.0/applications",
    serializedWebAppFormToJson: function(serializedArray, forUpdate) {
        var json = {};
        $.each(serializedArray, function() {

            var excludeProp = false;
            if (forUpdate !== true && this.name === "webappId") {
                excludeProp = true;
            }

            if (excludeProp !== true) {
                if (this.name === "secure") {
                    json[this.name] = this.value === "on" ? true : false;
                } else if (this.name === "loadBalance") {
                    json["loadBalanceAcrossServers"] = this.value === "acrossServers" ? true : false;
                } else if (this.name === "unpackWar") {
                    json[this.name] = this.value === "on" ? true : false;
                }
                else {
                    json[this.name] = this.value;
                }
            }
        });

        return JSON.stringify(json);
    },
    deleteWar : function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/applications/" + id + "/war", "json", caughtCallback);
    },
	insertNewWebApp : function(webAppFromArray, successCallback, errorCallback) {
	    return serviceFoundation.post("v1.0/applications",
		                              "json",
		                              this.serializedWebAppFormToJson(webAppFromArray, false),
		                                                successCallback,
		                                                errorCallback);
	},
	updateWebApp : function(webAppFromArray, successCallback, errorCallback) {
		return serviceFoundation.put("v1.0/applications/",
		                             "json",
				                     this.serializedWebAppFormToJson(webAppFromArray, true),
				                     successCallback,
				                     errorCallback);
	},
	deleteWebApp : function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/applications/" + id, "json", caughtCallback);
    },
	getWebApp : function(id, responseCallback) {
		return serviceFoundation.get("v1.0/applications/" + id, "json", responseCallback);
	},
	getWebAppByName : function(name) {
        return serviceFoundation.promisedGet("v1.0/applications/application;name=" + name, "json");
    },
	getWebApps : function(responseCallback) {
		return serviceFoundation.get("v1.0/applications?all", "json", responseCallback);
	},
	getWebAppsByGroup : function(groupId, responseCallback, loadingVisible) {
        return serviceFoundation.get("v1.0/applications?group.id=" + groupId, "json", responseCallback, loadingVisible);
    },
    getWebAppsByJvm : function(jvmId, responseCallback) {
        return serviceFoundation.get("v1.0/applications/jvm/" + jvmId, "json", responseCallback);
    },
    getResources : function(appName, jvmName, responseCallback) {
        return serviceFoundation.get("v1.0/applications/" + encodeURIComponent(jvmName) + "/" + encodeURIComponent(appName) + "/resources/name", "json", responseCallback);
    },
    updateResourceTemplate: function(appName, resourceTemplateName, resourceTemplateContent, jvmName, groupName) {
        var jvmMatrix = jvmName ? ";jvmName=" + encodeURIComponent(jvmName) : "";
        return serviceFoundation.promisedPut("v1.0/applications/" + encodeURIComponent(appName) + "/resources/template/" + encodeURIComponent(resourceTemplateName) + ";groupName=" + encodeURIComponent(groupName) + jvmMatrix,
                                     "json",
                                     resourceTemplateContent,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    previewResourceFile: function(resourceTemplateName, appName, groupName, jvmName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/applications/" + encodeURIComponent(appName) + "/resources/preview/ " + encodeURIComponent(resourceTemplateName) + ";groupName=" +
                                     encodeURIComponent(groupName) + ";jvmName=" + encodeURIComponent(jvmName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    deployConf: function(appName) {
        return serviceFoundation.promisedPut("v1.0/applications/" + appName + "/conf");
    }
};