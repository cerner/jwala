var uiLoadingIndicator = function() {
    var loadingDialog;
    return {
        showLoadingIndicator : function() {
            loadingDialog =  $("#loading");
            loadingDialog.dialog({
                                     modal:true,
                                     resizable:false,
                                     draggable:false,
                                     width:"auto",
                                     height:"auto"
                                 });
            loadingDialog.parents(".ui-dialog")
                .css("border", "0 none")
                .css("background", "transparent")
                .css("z-index", "999")
                .find(".ui-dialog-titlebar").remove();
        },
        removeLoadingIndicator : function() {
            if (loadingDialog !== undefined) {
                loadingDialog.dialog("close");
            }
        }
    };
}();