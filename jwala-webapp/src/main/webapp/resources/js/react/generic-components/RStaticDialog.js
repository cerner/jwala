/**
 * A dialog box container that is positioned in the page hence the word "static" in the name.
 */
var RStaticDialog = React.createClass({
    getInitialState: function() {
        return {contentContainer: {width: this.props.defaultContentWidth ? this.props.defaultContentWidth : "auto",
                                   height: this.props.defaultContentHeight ? this.props.defaultContentHeight : "auto"}};
    },
    render: function() {
        var customClass = this.props.className === undefined ? "" : this.props.className;
        return React.createElement("div", {ref: "containerDiv", className: "RStaticDialog container"},
                   React.createElement("div", {ref: "headerDiv", className: "header ui-dialog-titlebar ui-widget-header ui-helper-clearfix"},
                       React.createElement("span", {className: "ui-dialog-title text-align-center"}, this.props.title)),
                   React.createElement("div", {ref: "contentDiv", style: this.state.contentContainer, className: "content"}, this.props.children));
    },
    recomputeContentContainerSize: function(parentDimension) {
        if (parentDimension) {
            var contentContainer = {};
            if ($.isNumeric(parentDimension.width)) {
                contentContainer["width"] = parentDimension.width;
            } else {
                contentContainer["width"] = $(this.refs.contentDiv.getDOMNode()).width();
            }
            if ($.isNumeric(parentDimension.height)) {
                contentContainer["height"] = parentDimension.height - $(this.refs.headerDiv.getDOMNode()).height();
            } else {
                contentContainer["height"] = $(this.refs.contentDiv.getDOMNode()).height();
            }
            this.setState({contentContainer: contentContainer});
        }
    }
});
