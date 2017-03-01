var userService = {

    login : function(serializedFormData) {
        return serviceFoundation.promisedPost("v1.0/user/login", "json", serializedFormData, "application/x-www-form-urlencoded");
    },
    logout : function() {
        return serviceFoundation.post("v1.0/user/logout", "json", "", function(){
            window.location = jwalaVars.contextPath;
        });
    },
    getAuthorization: function(){
      return serviceFoundation.promisedGet("v1.0/admin/auth/state", "json");
    },

    getIsAdmin: function(){
      return serviceFoundation.promisedGet("v1.0/user/isUserAdmin", "json");
    }
};