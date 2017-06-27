var resourceService = {
    createResource: function(groupName, webServerName, jvmName, webAppName, formData, metaDataFile, deployFilename) {
        console.log(formData);
        if (metaDataFile) {
            // Legacy resource creation
            // The target is inside the attached JSON meta data file
            return serviceFoundation.promisedPost("v1.0/resources/template/", "json", formData, null, true);
        }
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.promisedPost("v1.0/resources/" + encodeURIComponent(deployFilename) + matrixParam, "json", formData, null, true, true);
    },
    deleteAllResource: function(resourceName) {
        return serviceFoundation.del("v1.0/resources/template/" + resourceName);
    },
    getResourceAttrData: function() {
        return serviceFoundation.promisedGet("v1.0/resources/data/");
    },
    getResourceTopology: function() {
        return serviceFoundation.promisedGet("v1.0/resources/topology/");
    },
    // TODO: All things regarding resources should be in here therefore we have to put resource related methods for JVM and web server here as well in the future.
    // NOTE: Also make sure to rewrite the REST service calls related to resources (for JVM and web servers) to be in the resource service, not in the group service.
    getAppResources : function(groupName, appName, responseCallback) {
        return serviceFoundation.get("v1.0/resources/" + encodeURIComponent(groupName) + "/" + encodeURIComponent(appName) + "/name", "json", responseCallback);
    },
    getResourceContent: function(resourceName, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.promisedGet("v1.0/resources/" + encodeURIComponent(resourceName) + "/content" + matrixParam);
    },

    deleteResource: function(resourceName, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.del("v1.0/resources/template/" + resourceName + matrixParam);
    },
    deleteResources: function(resourceNameArray, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);

        resourceNameArray.forEach(function(name){
            matrixParam += ";name=" + encodeURIComponent(name);
        });

        return serviceFoundation.del("v1.0/resources/templates" + matrixParam);
    },
    deployGroupAppResourceToHost: function(groupName, fileName, host, appName) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/apps/conf/" + encodeURIComponent(fileName) +
                                      "/" + encodeURIComponent(appName) + "?hostName=" + encodeURIComponent(host));
    },
    deployResourceToHost: function(fileName, host, groupName, webServerName, jvmName, webAppName){
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.promisedPut("v1.0/resources/template/" + encodeURIComponent(fileName) + "/deploy/host/" + encodeURIComponent(host) + matrixParam);
    },
    deployWebServerResource: function(webServerName, fileName) {
        return serviceFoundation.promisedPut("v1.0/webservers/" + encodeURIComponent(webServerName) + "/conf/" + encodeURIComponent(fileName));
    },
    deployJvmResource: function(jvmName, fileName) {
        return serviceFoundation.promisedPut("v1.0/jvms/" + encodeURIComponent(jvmName) + "/conf/" + encodeURIComponent(fileName), "json", null, false);
    },
    deployJvmWebAppResource: function(webAppName, groupName, jvmName, fileName) {
        return serviceFoundation.promisedPut("v1.0/applications/" + encodeURIComponent(webAppName) + "/conf/" +
                                             encodeURIComponent(fileName) + ";groupName=" + encodeURIComponent(groupName) +
                                             ";jvmName=" + encodeURIComponent(jvmName),
                                             "json",
                                             null,
                                             false);
    },
    deployGroupLevelWebServerResource: function(groupName, fileName) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/webservers/conf/" +
                                             encodeURIComponent(fileName), "json", null, false);
    },
    deployGroupLevelJvmResource: function(groupName, fileName) {
        return serviceFoundation.promisedPut("v1.0/groups/" + encodeURIComponent(groupName) + "/jvms/conf/" +
                                             encodeURIComponent(fileName), "json", null, false);
    },
    getExternalProperties: function(){
        return serviceFoundation.promisedGet("v1.0/resources/properties", "json");
    },
    getExternalPropertiesFile: function(callback){
        return serviceFoundation.get("v1.0/resources/templates/names", "json", callback);
    },
    updateResourceContent: function(resourceTemplateName, template, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.promisedPut("v1.0/resources/template/" + encodeURIComponent(resourceTemplateName) + matrixParam,
                                                    "json",
                                                     template,
                                                     false,
                                                     "text/plain; charset=utf-8")
    },
    previewResourceFile: function(resourceTemplateName, template, groupName, webServerName, jvmName, webAppName, successCallback, errorCallback) {
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.put("v1.0/resources/template/preview/"+ encodeURIComponent(resourceTemplateName) + matrixParam,
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    createMatrixParam:function(groupName, webServerName, jvmName, webAppName){
        var matrixParam = "";
        if (groupName) {
            matrixParam += ";group=" + encodeURIComponent(groupName);
        }
        if (webServerName) {
            matrixParam += ";webServer=" + encodeURIComponent(webServerName);
        }
        if (jvmName) {
            matrixParam += ";jvm=" + encodeURIComponent(jvmName);
        }
        if (webAppName) {
            matrixParam += ";webApp=" + encodeURIComponent(webAppName);
        }
        return matrixParam;
    },
    updateResourceMetaData: function(jvmName, webServerName, groupName, webAppName, resourceTemplateName, metaData) {
        var matrixParam = this.createMatrixParam(groupName, webServerName, jvmName, webAppName);
        return serviceFoundation.promisedPut("v1.0/resources/template/metadata/" + encodeURIComponent(resourceTemplateName) + matrixParam,
                                                            "json",
                                                             metaData,
                                                             false,
                                                             "text/plain; charset=utf-8");
    }
};
