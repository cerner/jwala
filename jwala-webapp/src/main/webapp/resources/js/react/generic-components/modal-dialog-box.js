/**
 * A generic modal dialog box with ok and cancel buttons.
 *
 * Properties:
 *
 * 1. content - the content of the dialog box. This can be a react component.
 * 2. width - width of the dialog. If undefined it is set to auto.
 * 3. height - height of the dialog. If undefined it is set to auto.
 * 4. show - if true the dialog box is displayed, otherwise it is hidden.
 * 5. contentDivClassName - defines the div container class of the content.
 * 6. title - the title of the dialog box.
 * 7. okCallback - the callback that is called when the ok button is clicked.
 * 8. cancelCallback - the callback that is called when the cancel button is clicked.
 * 9. okLabel - the "ok" button label. If undefined the button label shows "Ok" by default.
 * 10. cancelLabel - the "cancel" button label. If undefined the button label shows "Cancel" by default.
 * 11. top - the dialog's top position in px. This attribute only accepts a numeric value (no units or auto). If not set top will be computed to position the dialog at the middle of the screen.
 * 12. left - the dialog's left position in px. This attribute only accepts a numeric value (no units or auto). If not set left will be computed to position the dialog at the center of the screen.
 * 13. position - position of the dialog's main div element e.g. absolute, fixed, relative etc...search for "div position"
 * 14. contentReferenceName - if specified the content can be referenced outside the ModalDialogBox by this property
 * 15. hideFooter - if true the buttons at the bottom of the dialog box is not shown (if not set the buttons will show by default)
 *
 * Usage Example (in JSX)
 *
 * <ModalDialogBox title="Edit JVM"
 *                 show={this.state.showModalFormEditDialog}
 *                 okCallback={this.okEditCallback}
 *                 cancelCallback={this.cancelEditCallback}
 *                 content={<JvmConfigForm ref="jvmEditForm"
 *                                         data={this.state.selectedJvmForEditing}/>}
 */
var ModalDialogBox = React.createClass({
    getInitialState: function() {
        var top = this.props.top === undefined ? ModalDialogBox.DEFAULT_TOP : this.props.top;
        var left = this.props.left === undefined ? ModalDialogBox.DEFAULT_LEFT : this.props.left;

        return {show: this.props.show,
                top: top,
                left: left,
                title: this.props.title,
                content: this.props.content,
                okCallback: this.props.okCallback,
                cancelCallback: this.props.cancelCallback ? this.props.cancelCallback : this.close,
                mouseDownXDiff: 0,
                mouseDownYDiff: 0,
                needsRepositioning: false,
                contentHeight: "100%",
                enabled: true};
    },
    render: function() {
        if (!this.state.show) {
            return null;
        }

        var height = this.props.height ? this.props.height : ModalDialogBox.DEFAULT_HEIGHT;
        var width = this.props.width ? this.props.width : ModalDialogBox.DEFAULT_WIDTH;

        var theStyle = {overflow: "visible", zIndex: "998", position: this.props.position ? this.props.position : "absolute",
                        height: height, width: width, top: this.state.top + "px", left: this.state.left + "px", display: "block"};

        if (this.props.maximized) {
            theStyle["position"] = "fixed";
            theStyle["top"] = "0";
            theStyle["left"] = "0";
            theStyle["height"] = "100%";
            theStyle["width"] = "100%";
        }

        var theContent = this.props.contentReferenceName ? React.addons.cloneWithProps(this.state.content, {ref: this.props.contentReferenceName}) : this.state.content;
        var theFooter = this.props.hideFooter ? null : React.createElement(DialogFooter, {ref: "dialogFooter", okLabel: this.props.okLabel, cancelLabel: this.props.cancelLabel,
                                                                                          okCallback: this.okBtnOnClickHandler, cancelCallback: this.closeBtnOnClickHandler,
                                                                                          contentHasFocus: this.contentHasFocus});

        var theDialog = React.createElement("div", {ref: "theDialog", style: theStyle,
                                                    className: "ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-dialog-buttons ui-draggable ui-resizable", tabIndex: "-1", onKeyDown: this.keyDownHandler},
                            React.createElement(DialogHeader, {ref: "dialogHeader", title: this.state.title, closeBtnOnClick: this.closeBtnOnClickHandler, onMouseDown: this.mouseDownHandler, onMouseUp: this.mouseUpHandler}),
                            React.createElement("div", {ref: "contentDiv", style: {width: "100%", height: this.state.contentHeight, overflow: "auto"}}, React.createElement(DialogContent, {content: theContent})),
                            theFooter);
        return React.createElement("div", null, React.createElement("div", {className:"ui-widget-overlay ui-front"}), theDialog);
    },
    contentHasFocus: function() {
        return $(this.refs.theDialog.getDOMNode()).find(document.activeElement).length === 1;
    },
    keyDownHandler: function(e) {
        if (e.keyCode === ModalDialogBox.KEY_CODE_ESC) {
            this.closeBtnOnClickHandler();
        } else if (e.keyCode === ModalDialogBox.KEY_CODE_ENTER) {
            if (this.state.okCallback() !== false) {
                e.preventDefault();
            }
        }
    },
    componentDidMount: function() {
        // This is for the scenario where show is set to true initially.
        // Initiate re-render if top and left is not defined.
        if (this.state.show && this.state.top < 0) {
            this.show(); // Initiates render which computes top and left
        }
    },
    componentWillReceiveProps: function(nextProps) {
        if (this.props.show !== nextProps.show) {
            if (nextProps.show) {
                this.show(this.state.title, nextProps.content);
            } else {
                this.close();
            }
        }
    },
    componentDidUpdate: function() {
        if (this.refs.theDialog && this.state.needsRepositioning) {
            if (this.props.position !== "fixed" && this.props.modal) {
                $(this.getDOMNode()).parent().append(this.divOverlay);
            }
            var states = (this.props.top && this.props.left) ? {} : this.computePosition();
            states["needsRepositioning"] = false; // It's very important to set this to false to prevent infinite calling of componentDidUpdate

            // resize content container size
            if (this.refs.contentDiv) {
                var newHeight = this.refs.theDialog.getDOMNode().offsetHeight - this.refs.dialogHeader.getDOMNode().offsetHeight;
                if (this.refs.dialogFooter) {
                    newHeight = newHeight - this.refs.dialogFooter.getDOMNode().height;
                }
                states["contentHeight"] = newHeight;
            }

            this.setState(states);
        }

        if (this.refs.theDialog) {
            // If the dialog box or its content is not focused the onKeyDown will not work therefore we set the focus
            // manually if the dialog box or its content is not focused
            if ($(this.refs.theDialog.getDOMNode()).find(document.activeElement).length === 0) {
                this.refs.theDialog.getDOMNode().focus();
            }
        }
    },
    computePosition: function() {
        var position = {};
        if (this.props.position === "fixed") {
            position["top"] = Math.floor(window.innerHeight/2) - Math.floor($(this.refs.theDialog.getDOMNode()).height()/2);
            position["left"] = Math.floor(window.innerWidth/2) - Math.floor($(this.refs.theDialog.getDOMNode()).width()/2);
        } else {
            var height = $(this.refs.theDialog.getDOMNode()).height();
            var width = $(this.refs.theDialog.getDOMNode()).width();

            var offsetX = $(window).width()/2 - $(this.getDOMNode()).parent().offset().left;
            var offsetY = $(document).height()/2 - $(this.getDOMNode()).parent().offset().top;

            position["top"] = offsetY - height/2;
            position["left"] = offsetX - width/2;
        }
        return position;
    },

    /**
     * Shows and initiates dialog box repositioning
     *
     * Note: Showing the dialog box is a 2 step process. The first process is to have the dialog box render
     *       out of the screen to get the actual width and height of the dialog box. The 2nd process is to reposition
     *       the dialog in the viewable screen area which requires the dialog box's actual width and height derived
     *       in the first process.
     */
    show: function(title, content, okCallback, cancelCallback) {
        var states = {show: true, needsRepositioning: true};

        if (title) {
            states["title"] = title;
        }

        if (content) {
            states["content"] = content;
        }

        if (okCallback) {
            states["okCallback"] = okCallback;
        }

        if (cancelCallback) {
            states["cancelCallback"] = cancelCallback;
        }

        states["enabled"] = true;
        this.setState(states);
    },
    close: function() {
        this.setState({show: false});
    },
    okBtnOnClickHandler: function() {
        if (this.state.enabled) {
            this.state.okCallback();
        }
    },
    closeBtnOnClickHandler: function() {
        if (this.state.enabled) {
            if (this.state.cancelCallback) {
                this.state.cancelCallback();
            } else {
                this.close();
            }
        }
    },
    mouseDownHandler: function(e) {
        e.preventDefault();
        e.stopPropagation();
        this.mouseDown = true;
        this.state.mouseDownXDiff = e.pageX - this.state.left;
        this.state.mouseDownYDiff = e.pageY - this.state.top;
        $(document).on("mousemove", this.mouseMoveHandler);
    },
    mouseUpHandler: function(e) {
        e.preventDefault();
        e.stopPropagation();
        $(document).off("mousemove", this.mouseMoveHandler);
    },
    mouseMoveHandler: function(e) {
        e.preventDefault();
        e.stopPropagation();
        this.setState({top: e.pageY - this.state.mouseDownYDiff, left: e.pageX - this.state.mouseDownXDiff});
    },
    isShown: function() {
        return this.state.show;
    },
    setEnabled: function(enabled) {
        this.state.enabled = enabled;
    },
    statics: {
        DEFAULT_TOP: -10000,
        DEFAULT_LEFT: -10000,
        DEFAULT_HEIGHT: "auto",
        DEFAULT_WIDTH: "auto",
        KEY_CODE_ESC: 27,
        KEY_CODE_ENTER: 13
    }
});

/**
 * Dialog box head
 */
var DialogHeader = React.createClass({
    render: function() {
        return React.createElement("div", {className: "ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix",
                                           onMouseDown: this.props.onMouseDown, onMouseUp: this.props.onMouseUp},
                   React.createElement(DialogHeaderTitle, {title: this.props.title}),
                   React.createElement(DialogHeaderCloseBtn, {onClick: this.props.closeBtnOnClick}));
    }
});

/**
 * Dialog box title
 */
var DialogHeaderTitle = React.createClass({
    render: function() {
        return React.createElement("span", {className: "ui-dialog-title text-align-center"}, this.props.title);
    }
});

/**
 * Dialog box header "x" button
 */
var DialogHeaderCloseBtn = React.createClass({
    render: function() {
        return React.createElement(RButton, {title: "close",
                                             className: "ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close",
                                             spanClassName:"ui-button-icon-primary ui-icon ui-icon-closethick",
                                             onClick: this.onClickHandler});
    },
    onClickHandler: function() {
        this.props.onClick();
    }
});

/**
 * Dialog box content component
 */
var DialogContent = React.createClass({
    render: function() {
        return React.createElement("div", {className: "ui-dialog-content ui-widget-content " + this.props.className,
                   style: this.props.style}, this.props.content);
    }
});

/**
 * Dialog box footer which contains ok and cancel buttons
 */
var DialogFooter = React.createClass({
    render: function() {
        var okBtn = this.props.okCallback ? RButton({ref: "okBtn", onClick: this.props.okCallback, label: this.props.okLabel === undefined ? "Ok" : this.props.okLabel}) : null;
        var cancelBtn = RButton({ref: "cancelBtn", onClick: this.props.cancelCallback, label: this.props.cancelLabel === undefined ? "Cancel" : this.props.cancelLabel});
        return React.createElement("div", {className: "ui-dialog-buttonpane ui-widget-content ui-helper-clearfix"},
                   React.createElement("div", {className: "ui-dialog-buttonset"},
                       React.createElement("div", {className: "ui-dialog-buttonset"}, okBtn, cancelBtn)));
    },
    componentDidMount: function() {
        if (this.refs.okBtn && !this.props.contentHasFocus()) {
            // Focus on ok button by default
            this.refs.okBtn.getDOMNode().focus();
        }
    }
});