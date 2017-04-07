/**
 * A stand alone button that displays an icon instead of a caption.
 */
var IconBtn = React.createClass({
    getInitialState: function() {
        return {onMouseOver: false, onMouseOut: false};
    },
    render: function() {
        let outerSpanClassName = "IconBtn ui-state-default ui-corner-all";
        let innerSpanClassName = "ui-icon " + this.props.className;

        // We do dynamic className assignment because the application is using jQuery themes
        let stateHoverClassName = this.state.onMouseOver ? " ui-state-hover" : "";
        return <span title={this.props.title} className={outerSpanClassName + stateHoverClassName}
                     onMouseOver={this.onMouseOverCallback} onMouseOut={this.onMouseOutCallback} onClick={this.onClickCallback}>
                   <span className={innerSpanClassName}/>
               </span>;
    },
    onMouseOverCallback: function() {
        if (!this.state.onMouseOver) {
            this.setState({onMouseOver: true});
        }
    },
    onMouseOutCallback: function() {
        if (this.state.onMouseOver) {
            this.setState({onMouseOver: false});
        }
    },
    onClickCallback: function() {
        if ($.isFunction(this.props.onClick)) {
            this.props.onClick();
        }
    }
});