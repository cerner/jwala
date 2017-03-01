/**
 * A basic button that uses jquery UI themes
 *
 * Properties:
 *
 * 1. callback - method that gets called to do a specific action when the button is clicked
 * 2. label - the button's label i.e. Ok, Cancel, Save etc...
 *
 * Note: React's recommended way of doing component interaction is through callbacks
 */
var GenericButton = React.createClass({
    render: function() {
    	var accessKey = (this.props.accessKey !== undefined) ? this.props.accessKey : "";
        var className = (this.props.className !== undefined) ? this.props.className : "";
        var spanClassName = (this.props.spanClassName !== undefined) ? this.props.spanClassName : "ui-button-text";
        return React.DOM.button({className:"ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only " + className,
                                 type:"button",
                                 role:"button",
                                 ariaDisabled:false,
                                 accessKey:accessKey,
                                 onClick:this.handleClick},
                                 React.DOM.span({className:spanClassName}, this.props.label));
    },
    handleClick: function() {
        this.props.callback();
    }
});