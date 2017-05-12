/**
 * Component that covers the screen which effectively disables application
 */
var ScreenOverlay = React.createClass({

    render: function() {
        let style = this.props.show ?
            {position: "fixed", top: 0, left: 0, zIndex: 999, background: "black", opacity: "0.10", width: "100%",
             height: "100%", filter: "alpha(opacity=10)"} :
            {display: "none"};
        return React.createElement("div", {style: style});
    }

});