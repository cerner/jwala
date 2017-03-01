/** @jsx React.DOM */
var LoginArea = React.createClass({
    getInitialState: function () {
        return {
            error: "", showLoginBusy: false
        }
    },

    render: function() {
         var loginBusyImg = this.state.showLoginBusy ? <img src="public-resources/img/busy-circular.gif"/> : null;
         return <div className={"LoginDialogBox " + this.props.className}>
                   <form id="logInForm">
                      <br/>
                      <br/>
                      <img src="public-resources/img/jwala-logo.png"/>
                      <br/>
                      <br/>
                      <TextBox ref="userName" id="userName" name="userName" className="input" hint="User Name" hintClassName="hint"
                               onKeyPress={this.userNameTextKeyPress}/>
                      <br/>
                      <TextBox id="password" name="password" isPassword={true} className="input" hint="Password"
                              hintClassName="hint" onKeyPress={this.passwordTextKeyPress}/>
                      <div className="status">
                          {loginBusyImg}
                          <MessageLabel msg={this.state.error} className="login-error-msg"/>
                      </div>
                      <input type="button" value="Log In" onClick={this.logIn} />
                  </form>
              </div>
    },

    componentDidMount: function () {
        // Set initial focus on the user name text field
        $(this.refs.userName.getDOMNode()).children().focus();
    },
    userNameTextKeyPress: function () {
        if (event.charCode === 13) {
            return false; // prevent beep in IE8
        }
        return true;
    },
    passwordTextKeyPress: function (event) {
        if (event.charCode === 13) {
            this.logIn();
            return false;
        }
        return true;
    },
    logIn: function () {
        // TODO: Refactor to use dynamic state update to make this more inline with React.
        // NOTE! You might have to modify TextBox component for to Reactify this.
        if (!$("#userName").val().trim() || !$("#password").val()) {
            this.setState({ error: "User name and password are required." });
        } else {
            var self = this;
            self.setState({showLoginBusy: true, error: ""});
            userService.login($("#logInForm").serialize()).then(function(response){
                self.loginSuccessHandler();
            }).caught(function(response){
                self.loginErrorHandler(response);
            });
        }
    },
    loginSuccessHandler: function() {
        document.cookie = "userName=" + $("#userName").val(); // This is a quick fix to get the user id to the diagnose and resolve status.
        window.location = window.location.href.replace("/login", "");
    },
    loginErrorHandler: function (response) {
        var state = {showLoginBusy: false};
        if (response.responseJSON) {
            state["error"] = response.responseJSON.applicationResponseContent;
        } else {
            state["error"] = response.status;
        }
        this.setState(state);
    },
    statics: {
        isAdminRole: false
    }
});

$(document).ready(function () {
    var errorMessage = jwalaVars.loginStatus === "error" ? "Your user name or password is incorrect." : "";
    React.renderComponent(LoginArea({ className: "login-area", error: errorMessage }), document.body);
});