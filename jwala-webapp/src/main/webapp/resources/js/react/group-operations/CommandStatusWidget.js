/**
 * Display command action and error status as a result of the said command action.
 */
var CommandStatusWidget = React.createClass({
    getInitialState: function() {
        return {statusRows: [], isOpen: this.props.isOpen === undefined ? true : this.props.isOpen, fullPreviewMode: false, data: null};
    },
    render: function() {
        var self = this;
        this.props.numofRec=0;
        var openCloseBtnClassName = "ui-icon-triangle-1-e";
        var content = null;
        if (this.state.isOpen) {
            openCloseBtnClassName = "ui-icon-triangle-1-s";
            content = <div ref="content" className="ui-dialog-content ui-widget-content command-status-background command-status-content">
                          <table>
                              {this.state.statusRows}
                          </table>
                      </div>;
        }

        var fullPreviewComponent = this.state.fullPreviewMode ? <ModalDialogBox ref="previewDlg" title="Preview" maximized={true} show={true} cancelCallback={this.onCloseFullPreviewWindowHandler} hideFooter={true}/> : null;

        return  <div>
                    {fullPreviewComponent}
                    <div ref="commandStatusContainer" className="CommandStatusWidget container ui-dialog ui-widget ui-widget-content ui-front title-container-style">
                        <div className="title accordion-title nowrap ui-accordion-header ui-state-default ui-corner-all ui-accordion-icons">
                            <span className={"ui-accordion-header-icon ui-icon " + openCloseBtnClassName} style={{display:"inline-block"}} onClick={this.clickOpenCloseWindowHandler}></span>
                            <span className="ui-dialog-title" style={{display:"inline-block", float:"none", width:"auto"}}>Action and Event Logs</span>
                            <span className="ui-icon ui-icon-newwin" style={{display:"inline-block", float:"right", cursor: "pointer"}}
                                                                             onClick={this.clickOpenInNewWindowHandler}/>
                        </div>
                        <img ref="processingIcon" style={{visibility: "hidden", position: "absolute", zIndex: 10, top: 65, left: 525}} src="public-resources/img/blue-and-light-blue-gears.gif"/>
                        {content}
                    </div>
                </div>;

    },
    onCloseFullPreviewWindowHandler: function() {
        this.refs.processingIcon.getDOMNode().style.visibility = "hidden";
        this.props.numofRec=0;
        this.setState({fullPreviewMode: false});
    },
    componentDidMount: function() {
        this.readHistory();
    },
    componentDidUpdate: function(prevProps, prevState) {
        if (this.refs.content) {
            this.refs.content.getDOMNode().scrollTop = this.refs.content.getDOMNode().scrollHeight;
        }

        if (this.refs.previewDlg) {
           this.refs.previewDlg.setState({content: <JQueryDataTableComponent data={this.state.data}/>});
           document.body.style.overflow = "hidden";
        } else {
            document.body.style.overflow = "scroll";
        }
    },
    readHistory: function(readSuccessfulCallback) {
        var self = this;
        historyService.read(this.props.groupName, this.props.serverName, this.props.numofRec)
            .then(function(data) {
                      var statusArray = [];
                      for (var i = data.length - 1; i >= 0; i--) {
                          var status = {};
                          status["from"] = data[i].serverName;
                          status["userId"] = data[i].createBy;
                          status["asOf"] = data[i].createDate;
                          status["message"] = data[i].event;
                          self.push(status, data[i].eventType === "APPLICATION_EVENT" || data[i].eventType ===  "SYSTEM_ERROR"
                                                                      ? "error-status-font" : "action-status-font",
                              (i === 0));
                          statusArray.push(status);
                      }
                      self.state.data = statusArray;

                      if ($.isFunction(readSuccessfulCallback)) {
                        readSuccessfulCallback();
                      }
            }).caught(function(response) {console.log(response)});
    },
    clickOpenCloseWindowHandler: function() {
        this.setState({isOpen: !this.state.isOpen});
    },
    clickOpenInNewWindowHandler: function() {
        //TODO: parameterise and paginate
        this.props.numofRec=jwalaVars["historyReadMaxRecCount"];
        var self = this;
        this.readHistory(function(){
            self.refs.processingIcon.getDOMNode().style.visibility = "visible";
            // wait for the processing icon to get drawn before letting react do its thing by state setting
            setTimeout(function(){self.setState({fullPreviewMode: true})}, 100);
        });
    },
    showDetails: function(msg) {
        var myWindow = window.open("", "Error Details", "width=500, height=500");
        myWindow.document.write(msg);
    },
    onXBtnClick: function() {
        this.props.closeCallback();
    },
    onXBtnMouseOver: function() {
        this.setState({xBtnHover: true});
    },
    onXBtnMouseOut: function() {
        this.setState({xBtnHover: false});
    },
    push: function(status, fontClassName, forceUpdate) {
        var errMsg = status.message === "" ? [status.stateString] : groupOperationsHelper.splitErrorMsgIntoShortMsgAndStackTrace(status.message);

        // Do simple cleanup when status array reaches 200 items.
        if (this.state.statusRows.length >= 200) {
            this.state.statusRows.splice(0, 50); // remove first 50 items
        }

        if (errMsg[1] && errMsg[1].trim() !== "") {
            this.state.statusRows.push(<tr className={fontClassName}>
                                           <td>{moment(status.asOf).format("MM/DD/YYYY HH:mm:ss")}</td>
                                           <td className="command-status-td">{status.from}</td>
                                           <td>{status.userId}</td>
                                           <td className="command-status-td" style={{textDecoration: "underline", cursor: "pointer"}} onClick={this.showDetails.bind(this, errMsg[1])}>{errMsg[0]}</td>
                                       </tr>);
        } else {
            this.state.statusRows.push(<tr className={fontClassName}>
                                           <td>{moment(status.asOf).format("MM/DD/YYYY HH:mm:ss")}</td>
                                           <td className="command-status-td">{status.from}</td>
                                           <td>{status.userId}</td>
                                           <td>{errMsg[0]}</td>
                                       </tr>);
        }

        if (forceUpdate === undefined || forceUpdate === true) {
            this.forceUpdate();
        }
    }
});

var JQueryDataTableComponent = React.createClass({
    render: function() {
        var headerArray = [];
        var rowArray = [];
        for (var rowKey in this.props.data) {
            var colArray = [];
            for (var colKey in this.props.data[rowKey]) {
                if (rowArray.length === 0) {
                    headerArray.push(<th>{colKey}</th>);
                }

                // TODO: Refactor - specific code should not be defined here
                if (colKey === "asOf") {
                    colArray.push(<td>{moment(this.props.data[rowKey][colKey]).format("MM/DD/YYYY HH:mm:ss")}</td>);
                } else {
                    colArray.push(<td>{this.props.data[rowKey][colKey]}</td>);
                }
            }
            rowArray.push(<tr>{colArray}</tr>);
        }

        return <div style={{width: "100%", height: "100%", overflow: "auto"}}><table ref="dataTable"><thead>{headerArray}</thead><tbody>{rowArray}</tbody></table></div>;
    },
    componentDidMount: function() {
        // TODO: Refactor - specific code should not be defined here
        var colDefs = [{sWidth: "350px", aTargets: [0]}, {sWidth: "150px", aTargets: [2]}];

        // TODO: aaSorting should not be hard coded
        $(this.refs.dataTable.getDOMNode()).dataTable({bJQueryUI: true, iDisplayLength: 100, aLengthMenu: [[25, 50, 100, 200, -1],
                                                       [25, 50, 100, 200, "All"]], aaSorting: [[2, "asc"]], aoColumnDefs: colDefs});
    }
});