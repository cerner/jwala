var serviceFoundationUi = function(theActualLoadingFunc, theActualHidingFunc) {

    var noOpFunc = function() {};
    var gracePeriod = 0; //milliseconds before the loading indicator is actually shown
    var loadingTimeout;

    var setLoadingTimeout = function(aFunc) {
        if (loadingTimeout === undefined) {
            loadingTimeout = setTimeout(aFunc, gracePeriod);
        }
    };

    var clearLoadingTimeout = function() {
        if (loadingTimeout !== undefined) {
            clearTimeout(loadingTimeout);
            loadingTimeout = undefined;
        }
    };

    var loadingThunk = function(aFunc) {
        return function() {
            setLoadingTimeout(aFunc);
        }
    };

    var hidingThunk = function(aFunc) {
        return function() {
            clearLoadingTimeout();
            aFunc();
        }
    };

    var constructBehavior = function(showLoadingFunc, hideLoadingFunc) {
        return {
            showLoading : showLoadingFunc,
            hideLoading : hideLoadingFunc
        };
    };

    var invisible = constructBehavior(noOpFunc,
                                      noOpFunc);
    var visible = constructBehavior(loadingThunk(theActualLoadingFunc),
                                    hidingThunk(theActualHidingFunc));

    return {
        visibleLoading : function(isVisible) {
            if (isVisible) {
                return visible;
            } else {
                return invisible;
            }
        }
    };
}(uiLoadingIndicator.showLoadingIndicator, uiLoadingIndicator.removeLoadingIndicator);