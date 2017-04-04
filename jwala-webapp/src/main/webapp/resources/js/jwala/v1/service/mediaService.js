var mediaService = {
    getAllMedia: function(responseCallback) {
        return serviceFoundation.promisedGet("v1.0/media", "json");

    },
    getMediaById: function(id) {
        return serviceFoundation.promisedGet("v1.0/media;id=" + id, "json");
    },
    getMediaByName: function(name, responseCallback) {
        return serviceFoundation.promisedGet("v1.0/media;name=" + encodeURIComponent(name), "json");
    },
    createMedia: function(formData) {
        return serviceFoundation.promisedPost("v1.0/media", "json", formData, null, true, false);
    },
    updateMedia: function(serializedArray) {
        var jsonData = {};
        serializedArray.forEach(function(item){
            jsonData[item.name] = item.value;
        });
        return serviceFoundation.promisedPut("v1.0/media", "json", JSON.stringify(jsonData));
    },
    deleteMedia: function(name) {
        return serviceFoundation.promisedDel("v1.0/media/" + encodeURIComponent(name), "json");
    },
    getMediaTypes: function() {
        return serviceFoundation.promisedGet("v1.0/media/types");
    }
};
