var springBootAppService = {
    getAllSpringBootApp: function(responseCallback) {
        return serviceFoundation.promisedGet("v1.0/springboot", "json");
    },
    getSpringBootAppById: function(id) {
        return serviceFoundation.promisedGet("v1.0/springboot;id=" + id, "json");
    },
    getSpringBootAppByName: function(name, responseCallback) {
        return serviceFoundation.promisedGet("v1.0/springboot;name=" + encodeURIComponent(name), "json");
    },
    createSpringBootApp: function(formData) {
        return serviceFoundation.promisedPost("v1.0/springboot", "json", formData, null, true, false);
    },
    updateMedia: function(serializedArray) {
        var jsonData = {};
        serializedArray.forEach(function(item){
            jsonData``[item.name] = item.value;
        });
        return serviceFoundation.promisedPut("v1.0/springboot", "json", JSON.stringify(jsonData));
    },
    deleteSpringBootApp: function(name) {
        return serviceFoundation.promisedDel("v1.0/springboot/" + encodeURIComponent(name), "json");
    }
};
