/**
 * A button that can mimic "flashing" behaviour.
 */
var FlashingButton = React.createClass({
    flasher: null,
    getInitialState: function() {
        var flashing = this.props.flashing === "true" ? true : false;
        return {
            flashing: flashing,
            flash: false
        }
    },
    render: function() {
        var className = (this.props.className !== undefined) ? this.props.className : "";
        className = (this.state.flash ? this.props.flashClass + " " + className : className);
        var spanClassName = (this.props.spanClassName !== undefined) ? this.props.spanClassName : "ui-button-text";
        return React.DOM.button({className:"ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-state-hover " + className,
                                 type:"button",
                                 role:"button",
                                 ariaDisabled:false,
                                 onClick:this.handleClick},
                                 React.DOM.span({className:spanClassName}, this.props.label));
    },
    componentDidMount: function() {
        if (this.state.flashing) {
            this.flasher = setTimeout(this.flashCallback,
                                      this.props.flashDuration === undefined ? 500 : this.props.flashDuration);
        }
    },
    shouldComponentUpdate: function(nextProps, nextState) {
        var flashing = nextProps.flashing === "true" ? true : false;
        if (flashing !== this.state.flashing && nextState.flash === this.state.flash) {
            // State "flashing" update does not require re-rendering
            return false;
        }
        return true;
    },
    componentWillReceiveProps: function(nextProps) {
        if (nextProps.flashing === "true" && this.state.flashing === false) {
            this.setState({flashing:true});
            this.flasher = setTimeout(this.flashCallback,
                                      this.props.flashDuration === undefined ? 500 : this.props.flashDuration);
        }
    },
    flashCallback: function() {
        if (this.state.flash) {
            this.setState({flash:false});
        } else {
            this.setState({flash:true});
        }
        this.flasher = setTimeout(this.flashCallback,
                                  this.props.flashDuration === undefined ? 500 : this.props.flashDuration);
    },
    stopFlashing: function() {
        this.setState({flashing:false, flash:false});
        clearTimeout(this.flasher);
    },
    handleClick: function() {
        /**
         * Since the initial requirement for this button is to stop flashing when
         * clicked, for now there's no need to define a customizable mechanism to
         * determine when to stop flashing.
         */
        this.stopFlashing();

        this.props.callback();
    },
    componentWillUnmount: function() {
        clearTimeout(this.flasher);
    }
});