var groupService = {
    getGroups: function(queryString) {
        queryString = queryString === undefined ? "" : queryString;
        return serviceFoundation.promisedGet("v1.0/groups?" + queryString, "json");
    },
	insertNewGroup: function(name, successCallback, errorCallback) {
	    return serviceFoundation.post("v1.0/groups",
	                                  "json",
	                                  name,
	                                  successCallback,
	                                  errorCallback);
	},
	updateGroup: function(group, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/groups/",
                                     "json",
                                     serviceFoundation.serializedFormToJsonNoId(group),
                                     successCallback,
                                     errorCallback);
    },
	deleteGroup: function(name, caughtCallback) {
	    return serviceFoundation.del("v1.0/groups/" + encodeURIComponent(name) + "?byName=true", "json", caughtCallback);
	},
	getGroup: function(idOrName, responseCallback, byName) {
	    var queryString = "";
	    if (byName !== undefined) {
            queryString = "byName=" + byName;
	    }
	    return serviceFoundation.get("v1.0/groups/" + idOrName + "?" + queryString, "json", responseCallback);
	},
	getChildrenOtherGroupConnectionDetails: function(id, groupChildType) {
	    var queryParam = "";
	    if (groupChildType === "jvm") {
            queryParam = "?groupChildType=JVM";
	    } else if (groupChildType === "webServer") {
	        queryParam = "?groupChildType=WEB_SERVER";
	    }
        return serviceFoundation.promisedGet("v1.0/groups/" + id + "/children/otherGroup/connectionDetails" + queryParam,
                                             "json",
                                             true);
	},
	getGroupWebServerResources: function(name, responseCallback) {
        return serviceFoundation.get("v1.0/groups/" + encodeURIComponent(name) + "/webservers/resources/name", "json", responseCallback);
	},
	getGroupJvmResources: function(name, responseCallback) {
        return serviceFoundation.get("v1.0/groups/" + encodeURIComponent(name) + "/jvms/resources/name", "json", responseCallback);
	},
    updateGroupJvmResourceTemplate: function(groupName, resourceTemplateName, resourceTemplateContent) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/jvms/resources/template/" + encodeURIComponent(resourceTemplateName),
                                             "json",
                                             resourceTemplateContent,
                                             false,
                                             "text/plain; charset=utf-8");
    },
    updateGroupAppResourceTemplate: function(groupName, appName, resourceTemplateName, resourceTemplateContent) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/" + encodeURIComponent(appName) + "/apps/resources/template/" + encodeURIComponent(resourceTemplateName),
                                             "json",
                                             resourceTemplateContent,
                                             false,
                                             "text/plain; charset=utf-8");
    },
	getGroupAppResourceTemplate : function(wsName, tokensReplaced, resourceTemplateName, responseCallback) {
        return serviceFoundation.get("v1.0/groups/" + encodeURIComponent(wsName) + "/apps/resources/template/" + encodeURIComponent(resourceTemplateName) + "?tokensReplaced=" + tokensReplaced, "json", responseCallback);
    },
    updateGroupWebServerResourceTemplate: function(groupName, resourceTemplateName, resourceTemplateContent) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/webservers/resources/template/" + encodeURIComponent(resourceTemplateName),
                                             "json",
                                             resourceTemplateContent,
                                             false,
                                             "text/plain; charset=utf-8");
    },
    previewGroupWebServerResourceFile: function(resourceTemplateName, groupName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/groups/" + encodeURIComponent(groupName) + "/webservers/resources/preview/" + encodeURIComponent(resourceTemplateName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    previewGroupJvmResourceFile: function(resourceTemplateName, jvmName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/groups/" + encodeURIComponent(jvmName) + "/jvms/resources/preview/" + encodeURIComponent(resourceTemplateName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    previewGroupAppResourceFile: function(groupName, templateName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/groups/" + encodeURIComponent(groupName) + "/apps/resources/preview/" + encodeURIComponent(templateName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    getStartedWebServersAndJvmsCount: function(groupName) {
        if (groupName) {
            return serviceFoundation.promisedGet("v1.0/groups/" + encodeURIComponent(groupName) + "/children/startedCount");
        }
        return serviceFoundation.promisedGet("v1.0/groups/children/startedCount");
    },
    getStartedAndStoppedWebServersAndJvmsCount: function(groupName) {
        if (groupName) {
            return serviceFoundation.promisedGet("v1.0/groups/" + encodeURIComponent(groupName) + "/children/startedAndStoppedCount");
        }
        return serviceFoundation.promisedGet("v1.0/groups/children/startedAndStoppedCount");
    },
    getAllGroupJvmsAreStopped: function(groupName) {
        return serviceFoundation.promisedGet("v1.0/groups/" + encodeURIComponent(groupName) + "/jvms/allStopped");
    },
    getAllGroupWebServersAreStopped: function(groupName) {
        return serviceFoundation.promisedGet("v1.0/groups/" + encodeURIComponent(groupName) + "/webservers/allStopped");
    },
    getHosts: function(groupName) {
        return serviceFoundation.promisedGet("v1.0/groups/" + encodeURIComponent(groupName) + "/hosts");
    },
    getAllHosts: function() {
        return serviceFoundation.promisedGet("v1.0/groups/hosts");
    }
}