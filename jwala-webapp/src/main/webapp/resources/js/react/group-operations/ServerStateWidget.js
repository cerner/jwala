/**
 * Displays the state and an error indicator if there are any errors in the state.
 */
var ServerStateWidget = React.createClass({
    getInitialState: function() {
        return {status:this.props.defaultStatus, errorMessages:[], showErrorBtn:false, newErrorMsg:false,
                statusColorCode: null};
    },
    render: function() {
        var errorBtn = null;
        if (this.state.showErrorBtn) {
           errorBtn = <FlashingButton className="ui-button-height ui-alert-border ui-state-error error-indicator-button"
                                      spanClassName="ui-icon ui-icon-alert"
                                      flashing={this.state.newErrorMsg.toString()}
                                      flashClass="flash"
                                      callback={this.showErrorMsgCallback}/>
        }

        return <div className="status-widget-container">
                   <div ref="errorDlg" className="react-dialog-container"/>
                   <span className="status-label">{this.state.status}</span>
                   {errorBtn}
                   <div className={"dot " + this.state.statusColorCode} title={this.state.statusColorCode} />
               </div>;
    },
    setStatus: function(newStatus, dateTime, errorMsg, statusColorCode) {
        var newState = {status:newStatus};

        if (statusColorCode) {
            newState["statusColorCode"] = statusColorCode;
        }

        if (errorMsg) {
            newState["newErrorMsg"] = true;
            newState["showErrorBtn"] = true;
            var errMsg = groupOperationsHelper.splitErrorMsgIntoShortMsgAndStackTrace(errorMsg);
            if (this.state.errorMessages.length === 0 || this.state.errorMessages[this.state.errorMessages.length - 1].msg !== errMsg[0]) {
                this.state.errorMessages.push({dateTime:moment(dateTime).format("MM/DD/YYYY hh:mm:ss"),
                                               msg:errMsg[0],
                                               pullDown:errMsg[1]});
            }
        } else {
            newState["newErrorMsg"] = false;
            newState["showErrorBtn"] = false;
        }
        this.setState(newState);
    },
    showErrorMsgCallback: function() {
        this.setState({newErrorMsg:false});
        React.render(<DialogBox title={this.props.errorMsgDlgTitle}
                                contentDivClassName="maxHeight400px"
                                content={<ErrorMsgList msgList={this.state.errorMessages}/>} />,
                     this.refs.errorDlg.getDOMNode());
    }
});