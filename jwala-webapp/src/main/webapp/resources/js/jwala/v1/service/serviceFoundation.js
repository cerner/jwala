var serviceFoundation = {

    get : function (url, dataType, thenCallback, loadingVisible) {
        var loadingUiBehavior = serviceFoundationUi.visibleLoading(loadingVisible === undefined ? true : loadingVisible);
        return Promise.cast($.ajax({url: url,
                                    dataType: dataType,
                                    type: 'GET',
                                    cache: false,
                                    beforeSend: loadingUiBehavior.showLoading,
                                    complete: loadingUiBehavior.hideLoading
                                })).then(function(response){
                                    if ($.isFunction(thenCallback)) {
                                        thenCallback(response);
                                    }
                                }).caught(function(response){
                                    if (response.message !== undefined) {
                                        $.errorAlert(response.message, "Error");
                                    } else if (response.status !== 200) {
                                        $.errorAlert(JSON.stringify(response), "Error");
                                    }
                                });
    },
    promisedGet : function(url, dataType, showLoading) {
        var loadingUiBehavior = serviceFoundationUi.visibleLoading(showLoading === undefined ? false : showLoading);
        return Promise.cast($.ajax({url: url,
                                       dataType: dataType,
                                       type: 'GET',
                                       cache: false,
                                       beforeSend: loadingUiBehavior.showLoading,
                                       complete: loadingUiBehavior.hideLoading
                                   }));
    },
    post : function(url, dataType, content, thenCallback, caughtCallback, showDefaultAjaxProcessingAnimation, contentType, isFileUpload) {
        var ajaxParams = {url: url,
                          dataType: dataType,
                          type: 'POST',
                          data: content,
                          contentType: contentType !== undefined ? contentType : 'application/json',
                          cache: false};
        if (isFileUpload) {
            ajaxParams.cache = false;
            ajaxParams.contentType = false;
            ajaxParams.processData = false;
        }

        if (showDefaultAjaxProcessingAnimation !== false) {
            var loadingUiBehavior = serviceFoundationUi.visibleLoading(true);
            ajaxParams["beforeSend"] = loadingUiBehavior.showLoading;
            ajaxParams["complete"] = loadingUiBehavior.hideLoading;
        }

        return Promise.cast($.ajax(ajaxParams)).then(function(response){
                                        if ($.isFunction(thenCallback)) {
                                            thenCallback(response);
                                        }
                                   }).caught(function(e) {
                                        if ($.isFunction(caughtCallback)) {
                                            try {
                                                var jsonResponseText = JSON.parse(e.responseText);
                                                var errorDetails = jsonResponseText.content;
                                                if (jsonResponseText.applicationResponseContent) {
                                                    caughtCallback(jsonResponseText.applicationResponseContent, errorDetails);
                                                } else {
                                                    caughtCallback(jsonResponseText.message, errorDetails);
                                                }
                                            }catch(e) {
                                                caughtCallback("Unexpected content in error response: " + e.responseText);
                                            }
                                        }
                                   });
    },
    promisedPost : function(url, dataType, content, contentType, isFileUpload, showLoading) {
        var loadingUiBehavior = serviceFoundationUi.visibleLoading(showLoading ? showLoading : false);
        var ajaxParams = {url: url,
                          dataType: dataType,
                          type: 'POST',
                          data: content,
                          cache: false,
                          beforeSend: loadingUiBehavior.showLoading,
                          complete: loadingUiBehavior.hideLoading};

        if (isFileUpload) {
            ajaxParams.contentType = false;
            ajaxParams.processData = false;
        } else {
            ajaxParams.contentType = contentType ? contentType : 'application/json';
        }

        return Promise.cast($.ajax(ajaxParams));
    },
    del : function(url, dataType, caughtCallback) {
        var loadingUiBehavior = serviceFoundationUi.visibleLoading(true);
        return Promise.cast($.ajax({
            url: url,
            dataType: dataType,
            type: 'DELETE',
            cache: false,
            beforeSend: loadingUiBehavior.showLoading,
            complete: loadingUiBehavior.hideLoading
        })).caught(function(e){
           if (e.responseText !== undefined && e.status !== 200) {
               var jsonResponseText = JSON.parse(e.responseText);
               if (jsonResponseText.applicationResponseContent) {
                   $.errorAlert(jsonResponseText.applicationResponseContent, "Error");
               } else {
                   $.errorAlert(jsonResponseText.message, "Error");
               }

           } else if (e.status !== 200) {
               $.errorAlert(JSON.stringify(e), "Error");
           }

           if ($.isFunction(caughtCallback)) {
                caughtCallback();
           }
        });
    },
    promisedDel : function(url, dataType) {
        var loadingUiBehavior = serviceFoundationUi.visibleLoading(true);
        var ajaxParams = {url: url,
                          dataType: dataType,
                          type: 'DELETE',
                          cache: false,
                          beforeSend: loadingUiBehavior.showLoading,
                          complete: loadingUiBehavior.hideLoading};
        return Promise.cast($.ajax(ajaxParams));
    },
    put : function(url, dataType, content, thenCallback, caughtCallback, showLoading, contentType) {
        var loadingUiBehavior = serviceFoundationUi.visibleLoading(showLoading === undefined ? true : showLoading);
        return Promise.cast($.ajax({url: url,
                                        dataType: dataType,
                                        type: 'PUT',
                                        data: content,
                                        contentType: contentType === undefined ? 'application/json' : contentType,
                                        cache: false,
                                        beforeSend: loadingUiBehavior.showLoading,
                                        complete: loadingUiBehavior.hideLoading
                                    })).then(function(response){
                                        if ($.isFunction(thenCallback)) {
                                            thenCallback(response);
                                        }
                                    }).caught(function(e) {
                                        if ($.isFunction(caughtCallback)) {
                                            try {
                                                var jsonResponseText = JSON.parse(e.responseText);
                                                var errorDetails = jsonResponseText.content;
                                                if (jsonResponseText.applicationResponseContent) {
                                                    caughtCallback(jsonResponseText.applicationResponseContent, errorDetails);
                                                } else {
                                                    caughtCallback(jsonResponseText.message, errorDetails);
                                                }
                                            }catch(e) {
                                                caughtCallback("Unexpected content in error response: " + e.responseText);
                                            }
                                        }
                                    });
            },

    promisedPut: function(url, dataType, content, showLoading, contentType) {
                     var loadingUiBehavior = serviceFoundationUi.visibleLoading(showLoading === undefined ? true : showLoading);
                     var params = {url: url,
                                   dataType: dataType,
                                   type: 'PUT',
                                   data: content,
                                   contentType: contentType === undefined ? 'application/json' : contentType,
                                   cache: false,
                                   beforeSend: loadingUiBehavior.showLoading,
                                   complete: loadingUiBehavior.hideLoading};
                     return Promise.cast($.ajax(params));
                  },


    /**
     * Form data converted to a serialized array will have a constant name-value pair i.e.
     * [{name:value}...] therefore using JSON stringify on it will not produce
     * the desired JSON format thus the need for the function serializedFormToJson
     * to correctly build the JSON data.
     *
     * Example of form data converted to a serialized array then "stringified"
     *
     * [{"name":"jvmName","value":"default-jvm-name"},{"name":"hostName","value":"default-host-name"}]
     *
     * NOTE: This is not what we want! It should be like this:
     *
     * [{"jvmName":"default-jvm-name"},{"hostName":"default-host-name"}]
     *
     */
    serializedFormToJsonNoId: function(serializedArray) {
        var json = {};
        $.each(serializedArray, function() {
            json[this.name] = this.value;
        });
        return JSON.stringify(json);
    }

};