/**
 * A toolbar component.
 */
var RToolbar = React.createClass({
    render: function() {
        var self = this;
        var btns = [];
        this.props.metaData.forEach(function(meta){
            btns.push(React.createElement(RToolbarButton, {ref: meta.ref, key: meta.title, meta: meta,
                btnClassName: self.props.btnClassName, busyBtnClassName: self.props.busyBtnClassName}));
        });

        return React.createElement("ul", {className: "RToolBar ui-widget ui-helper-clearfix"}, btns);
    }
});

/**
 * The toolbar button.
 */
var RToolbarButton = React.createClass({
    timeout: null,
    getInitialState: function() {
        return {clicked: false, hover: false, enabled: true};
    },
    render: function() {
        var hoverClassName = this.state.hover ? "ui-state-hover " : "";
        if (!this.state.clicked) {
            var defaultClassName = "button ui-corner-all " + (this.state.enabled ? "ui-state-default " : "ui-state-disabled ");
            var btnClassName = this.props.btnClassName ? " " + this.props.btnClassName : "";

            var liAttributes = {};
            liAttributes["className"]  = defaultClassName +  hoverClassName + btnClassName;
            if (this.state.enabled) {
                liAttributes["onClick"] = this.onClick;
                liAttributes["onMouseMove"] = this.onMouseMove;
                liAttributes["onMouseOut"] = this.onMouseOut;
            }
            return React.createElement("li", liAttributes,
                       React.createElement("span", {className:"ui-icon " + this.props.meta.icon, title: this.props.meta.title}));
        }
        return React.createElement("li", {className: defaultClassName + this.props.busyBtnClassName});
    },
    onClick: function() {
        if (this.props.busyBtnClassName !== undefined) {
            this.setState({clicked: true});
            // TODO: Make hard coded timeout definable.
            this.timeout = setTimeout(this.timeout, 60000);
        }
        this.props.meta.onClickCallback(this.ajaxProcessDoneCallback)
    },
    timeout: function() {
        this.setState({clicked: false});
    },
    onMouseMove: function() {
        if (!this.state.hover) {
            this.setState({hover: true});
        }
    },
    onMouseOut: function() {
        this.setState({hover: false});
    },
    ajaxProcessDoneCallback: function() {
        this.setState({clicked: false});
    },
    setEnabled: function(enabled) {
        this.setState({enabled: enabled});
    }
});
