/** @jsx React.DOM */
var TextBox = React.createClass({
    getInitialState: function() {
        return {
            theType: this.props.isPassword ? "password" : "text",
            hintClassName: this.props.hintClassName,
            inputClassName: "input-on-blur " + this.props.className,
            value: ""
        }
    },
    render: function() {
        if (this.props.hint === undefined) {
            return <div><input id={this.props.id}
                               className={this.props.className}
                               type={this.state.theType}/></div>
        }
        return  <div>
                    <label className={this.props.hintClassName}
                           htmlFor={this.props.id}>{this.props.hint}</label>
                    <input id={this.props.id}
                           name={this.props.name}
                           className={this.state.inputClassName}
                           type={this.state.theType}
                           value={this.state.value}
                           onFocus={this.handleFocus}
                           onBlur={this.handleBlur}
                           onChange={this.handleChange}
                           onKeyPress={this.props.onKeyPress}/>
                </div>
    },
    /**
     * This method is needed to hide the label and set the state if the
     * browser filled this component with a saved value automatically.
     * This is primarily used to handle browser "remember me" log in
     * credentials.
     */
    checkForBrowserSavedInput: function() {
        var val = $(this.getDOMNode().children[1]).val();
        if (val !== "") {
            this.setComponentState(val);
        }
    },
    componentDidMount: function() {
        /**
         * The setTimeout is needed since the browser auto-fill event seems
         * to happen after componentDidMount life cycle. In addition, no html
         * input event fires on auto-fill thus the need for a method to be
         * executed at a later time to check if this component was filled up
         * by the browser.
         */
        setTimeout(this.checkForBrowserSavedInput, 100);
    },
    handleBlur: function() {
        if (this.state.value === "") {
            this.setState({inputClassName:"input-on-blur " + this.props.className});
        }
    },
    handleChange: function(event) {
        this.setComponentState(event.target.value);
    },
    setComponentState: function(val) {
        var className;
        if (val === "") {
            className = "input-on-blur " + this.props.className;
        } else {
            className = "input-on-focus " + this.props.className;
        }
        this.setState({inputClassName: className, value: val});
    }
})