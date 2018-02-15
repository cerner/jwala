/**
 * A basic button has it's onclick event attached by JQuery so that it can be used by
 * the DataTable component. The DataTable component renders a button to string which
 * nullifies React's event handling/binding hence the JQuery binding approach.
 *
 * Properties:
 *
 *  1. id - the button's id
 *  2. callback - method that gets called to do a specific action when the button is clicked
 *  3. label - the button's label i.e. Ok, Cancel, Save etc...
 *  4. isToggleBtn - if the button is a on/off or a switch button
 *  5. label2 - the label to display for the 2nd toggle state
 *  6. callback2 - the callback to execute for the 2nd toggle state
 *  7. className - container div css style
 *  8. customBtnClassName - for custom button look and feel
 *  9. clickedStateClassName - css style when button is in a "clicked" state
 * 10. clickedStateTimeout - duration in which to show the button clicked state style,
 *                           default is 10 seconds if this is not set
 *
 * NOTE: Documentation needs to be updated!
 *
 */
var DataTableButton = React.createClass({
    /**
     * Note: Since this button was designed to be fully compatible with renderComponentToString
     * and renderComponentToStaticMarkup, we can't use React state management since if we so
     * component re-rendering after state change will result to the error
     * "Cannot read property 'firstChild' of undefined"
     */
    toggleStatus: 0,
    busyTimeout: null,
    render: function () {
        DataTableButton.bindEvents(this);

        var spanClassName = this.props.customSpanClassName;
        if (this.props.customSpanClassName === undefined) {
            spanClassName = "ui-button-text";
        }

        var theLabel = this.toggleStatus === 0 ? this.props.label : this.props.label2;
        var buttonClassName = this.props.buttonClassName !== undefined ? this.props.buttonClassName : "";
        var className = "ui-button ui-widget ui-corner-all ui-button-text-only " + buttonClassName;
        className += !this.props.disabled ?  " ui-state-default"  : " ui-state-disabled";
        return React.DOM.div({className: this.props.className },
                              React.DOM.button({ id: this.props.id,
                              type: "button",
                              role: "button",
                              ariaDisabled: false,
                              className: className,
                              title: this.props.sTitle },
                              React.DOM.span({ className: spanClassName }, theLabel)));
    },
    setToNonBusyState: function () {
        var buttonClassName = this.props.buttonClassName !== undefined ? this.props.buttonClassName : "";
        $("#" + this.props.id).attr("class", "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only " + buttonClassName);
        $("#" + this.props.id).find("span").attr("class", this.props.customSpanClassName);
    },
    busyTimeoutCallback: function () {
        if (this.props.clickedStateClassName !== undefined) {
            this.setToNonBusyState();
        }
    },
    pollForStateChange: function () {
        if ($("#" + this.props.id).hasClass(this.props.clickedStateClassName)) {
            // if still busy check status
            if (!this.props.isBusyCallback(this.props.itemId, this.props.expectedState)) {
                clearTimeout(this.busyTimeout);
                this.setToNonBusyState();
            } else {
                var self = this;
                setTimeout(function () {
                    self.pollForStateChange();
                }, 1000);
            }
        }
    },
    statics: {
        handleClick: function (self) {
            if (self.props.disabled) {
                return;
            }

            if (!self.props.onClickMessage && $("#tooltip" + self.props.id).length === 0) {
                var top = $("#" + self.props.id).parent().position().top - $("#" + self.props.id).height() / 2;
                var left = $("#" + self.props.id).parent().position().left + $("#" + self.props.id).width() / 2;
                $("#" + self.props.id).parent().append("<div id='tooltip" + self.props.id + "' role='tooltip' class='ui-tooltip ui-widget ui-corner-all ui-widget-content' " + "style='top:" + top + "px;left:" + left + "px'>" + self.props.onClickMessage + "</div>");
                $("#tooltip" + self.props.id).fadeOut(3000, function () {
                    $("#tooltip" + self.props.id).remove();
                });
            }

            // Set the timeouts
            // Note: Manual state monitoring using timeouts are not needed anymore once we are using "the REACT" grid already ;)
            if (self.props.clickedStateClassName !== undefined) {
                $("#" + self.props.id).attr("class", self.props.clickedStateClassName);
                $("#" + self.props.id).find("span").removeClass();

                // Timeout if when the status gets stuck!
                var timeout = self.props.clickedStateTimeout === undefined ? self.props.busyStatusTimeout : self.props.clickedStateTimeout;
                self.busyTimeout = setTimeout(function () {
                    self.busyTimeoutCallback();
                }, timeout);

                if (self.props.isBusyCallback !== undefined) {
                    // Timeout used for polling status change
                    setTimeout(function () {
                        self.pollForStateChange();
                    }, 1000);
                }
            }

            if (self.props.isToggleBtn) {
                if (self.toggleStatus === 0) {
                    if (self.props.callback(self.props.itemId)) {
                        self.toggleStatus = 1;
                    }
                } else {
                    if (self.props.callback2(self.props.itemId)) {
                        self.toggleStatus = 0;
                    }
                }

                $("#" + self.props.id).val(self.toggleStatus === 1 ? self.props.label2 : self.props.label);
            } else {
                self.props.callback(self.props.itemId, "#" + self.props.id, self.props.extraDataToPassOnCallback, self.props.parentItemId, self.busyTimeoutCallback);
            }
        },
        hoverCallback: function (id, label) {
            var MARKER = "jquery-button-applied";
            var theBtn = $("#" + id);
            if (label !== undefined && label !== "" && !theBtn.hasClass(MARKER)) {
                theBtn.html(label);
                theBtn.button();
                theBtn.addClass(MARKER);
            }
        },
        bindEvents: function (self) {
            $("#" + self.props.id).off("click");
            $("#" + self.props.id).on("click", DataTableButton.handleClick.bind(self, self));

            var theLabel = self.toggleStatus === 0 ? self.props.label : self.props.label2;
            if (theLabel === undefined || theLabel === "") {
                // This means that this button is graphical e.g. play, stop button
                // We have to handle button highlight on hover ourselves since we don't want
                // the default button handler to convert this button back to a regular text
                // button when the user finishes hovering over it!
                $("#" + self.props.id).off("mouseenter");
                $("#" + self.props.id).off("mouseleave");
                $("#" + self.props.id).on({
                    mouseenter: function () {
                        if (!$("#" + self.props.id).hasClass(self.props.clickedStateClassName)) {
                            $("#" + self.props.id).addClass("ui-state-hover");
                        }
                    },
                    mouseleave: function () {
                        $("#" + self.props.id).removeClass("ui-state-hover");
                    }
                });
            }
        }
    }

});