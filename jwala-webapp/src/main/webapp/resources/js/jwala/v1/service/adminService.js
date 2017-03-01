var adminService = {

    encryptServerSide: function(toEncrypt, successCallback, errorCallback) {
        return serviceFoundation.post("v1.0/admin/properties/encrypt", "json", toEncrypt, successCallback, errorCallback, true, "text/plain");
    },
    
    reloadProperties: function(successCallback) {
    	return  serviceFoundation.get("v1.0/admin/properties/reload", "json", successCallback);
    },

    viewProperties: function(successCallback) {
        if (successCallback) {
            return  serviceFoundation.get("v1.0/admin/properties/view", "json", successCallback);
        }
        return serviceFoundation.promisedGet("v1.0/admin/properties/view", "json");
    },

    viewManifest: function(successCallback) {
        return serviceFoundation.get("v1.0/admin/manifest", "json", successCallback);
    },

    getAuthorizationDetails: function(){
        return serviceFoundation.promisedGet("v1.0/admin/context/authorization", "json");
    }
    
}