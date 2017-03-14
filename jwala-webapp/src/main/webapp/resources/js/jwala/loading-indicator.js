var uiLoadingIndicator = function() {
    var loadingDialog;
    return {
        showLoadingIndicator : function() {
            $(".AppBusyScreen").addClass("show");
        },
        removeLoadingIndicator : function() {
            $(".AppBusyScreen").removeClass("show");
        }
    };
}();