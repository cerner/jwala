/**
 *  @jsx React.DOM
 *
 * This component displays the message specified in its msg property.
 * If the msg property is empty or is not specified, it will render an empty string.
 * The reason behind this component is to prevent creating unnecessary DOM elements
 * if the msg is empty as in the case of <span>{errorMsg}</span>. If errorMsg is empty
 * the span element is still rendered.
 *
 * Properties:
 *
 * 1. msg - the message to display
 * 2. className - give one the option of customizing message label look and feel
 */
var MessageLabel = React.createClass({
    render: function() {
        if (this.props.msg !== undefined || this.props.msg !== null || this.props.msg.trim() !== "") {
            return <div className={this.props.className}>
                        <span>{this.props.msg}</span>
                   </div>
        }
        return "";
    }
});
