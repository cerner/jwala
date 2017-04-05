/** @jsx React.DOM */
var WebServerConfig = React.createClass({

    /**
     * This object is used to let the component know when to update itself.
     * It essentially uses a kind of "toggle switch" pattern wherein when set
     * the flag is set to true then when "checked/viewed" the flag is set to false.
     * This mechanism is required to essentially tell the component if the
     * table needs to be updated since the underlying table is using jQuery Datatable.
     * This should not be necessary if the entire table is purely made in React.
     */
    cancelFlag: {
        flag: false,
        set: function() {
            this.flag = true;
        },
        check: function () {
            var prevFlag = this.flag;
            this.flag = false; // reset the flag
            return prevFlag;
        }
    },
    getInitialState: function() {
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            showDeleteConfirmDialogContinue: false,
            selectedWebServerForEditing: null,
            webServerTableData: [],
            selectedWebServer: null
        }
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={"dataTables_wrapper " + this.props.className}>
                    <table className="webserver-config-table-type-container">
                        <tr>
                            <td>
                                <div style={{float:"right"}}>
                                    <GenericButton label="Delete" accessKey="d" callback={this.delBtnCallback}/>
                                    <GenericButton label="Add" accessKey="a" callback={this.addBtnCallback}/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div>
                                    <WebServerDataTable ref="webServerConfigDataTable"
                                                        data={this.state.webServerTableData}
                                                        selectItemCallback={this.selectItemCallback}
                                                        editCallback={this.editCallback}
                                                        noUpdateWhen={this.state.showModalFormAddDialog ||
                                                                      this.state.showDeleteConfirmDialog ||
                                                                      this.state.showDeleteConfirmDialogContinue ||
                                                                      this.state.showModalFormEditDialog ||
                                                                      this.cancelFlag.check()
                                                        }/>
                                </div>
                            </td>
                        </tr>
                   </table>

                   <ModalDialogBox title="Add Web Server"
                                   show={this.state.showModalFormAddDialog}
                                   okCallback={this.okAddCallback}
                                   cancelCallback={this.cancelAddCallback}
                                   content={<WebServerConfigForm ref="webServerAddForm" />}
                                   width="auto"
                                   height="auto"/>

                   <ModalDialogBox title="Edit Web Server"
                                   show={this.state.showModalFormEditDialog}
                                   okCallback={this.okEditCallback}
                                   cancelCallback={this.cancelEditCallback}
                                   content={<WebServerConfigForm ref="webServerEditForm"
                                                                 data={this.state.selectedWebServerForEditing}/>}
                    />

                    <ModalDialogBox title="Confirmation Dialog Box"
                                    show={this.state.showDeleteConfirmDialog}
                                    okCallback={this.confirmDeleteCallback}
                                    cancelCallback={this.cancelDeleteCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to delete {this.state.selectedWebServer ? this.state.selectedWebServer.name : "the selected item"} ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No" />

                   <ModalDialogBox ref="forceDeleteConfirmDlg"
                                   title="Confirmation Dialog Box"
                                   show={this.state.showDeleteConfirmDialogContinue}
                                   okCallback={this.confirmDeleteCallbackContinue}
                                   cancelCallback={this.cancelDeleteCallbackContinue}
                                   okLabel="Yes"
                                   cancelLabel="No" />
               </div>
    },
    cancelAddCallback: function() {
        this.cancelFlag.set();
        this.setState({showModalFormAddDialog:false});
    },
    cancelEditCallback: function() {
        this.cancelFlag.set();
        this.setState({showModalFormEditDialog:false});
    },
    okAddCallback: function() {
        if (this.refs.webServerAddForm.isValid()) {
            var self = this;
            this.props.service.insertNewWebServer(this.refs.webServerAddForm.state.name,
                                                  this.refs.webServerAddForm.state.groupIds,
                                                  this.refs.webServerAddForm.state.host,
                                                  this.refs.webServerAddForm.state.port,
                                                  this.refs.webServerAddForm.state.httpsPort,
                                                  this.refs.webServerAddForm.state.statusPath,
                                                  function(){
                                                      self.state.selectedWebServer = null;
                                                      self.refreshData({showModalFormAddDialog:false});
                                                  },
                                                  function(errMsg) {
                                                        $.errorAlert(errMsg, "Error");
                                                        if (errMsg.indexOf("Multiple groups were associated with the Web Server") === 0){
                                                            self.refreshData({showModalFormAddDialog:false});
                                                        }
                                                  });
        }
    },
    okEditCallback: function() {
        if (this.refs.webServerEditForm.isValid()) {
            var self = this;
            this.props.service.updateWebServer($(this.refs.webServerEditForm.getDOMNode().children[0]).serializeArray(),
                                                 function(response){
                                                    self.refreshData({showModalFormEditDialog:false});
                                                    self.state.selectedWebServer = response.applicationResponseContent;
                                                 },
                                                 function(errMsg) {
                                                     $.errorAlert(errMsg, "Error");
                                                });
        }
    },
    refreshData: function(states, doneCallback) {
        var self = this;
        this.props.service.getWebServers(function(response){
                                             states["webServerTableData"] = response.applicationResponseContent;
                                             if (doneCallback !== undefined) {
                                                 doneCallback();
                                             }
                                             self.setState(states);
                                        });
    },
    addBtnCallback: function() {
        this.setState({showModalFormAddDialog: true})
    },
    delBtnCallback: function() {
        if (this.state.selectedWebServer !== null) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    confirmDeleteCallback: function() {
        var self = this;
        this.props.service.deleteWebServer(this.state.selectedWebServer.id.id, false).then(
            function(response){
            self.refreshData({showDeleteConfirmDialog: false}, function(){self.state.selectedWebServer = null});
        }).caught(
            function(e) {
                self.setState({showDeleteConfirmDialog: false})
                if (e.responseText !== undefined && e.status !== 200) {
                    var jsonResponseText = JSON.parse(e.responseText);
                    if (jsonResponseText.applicationResponseContent) {
                        self.refs.forceDeleteConfirmDlg.setState({content: jsonResponseText.applicationResponseContent + ", are you sure you want to continue delete without remove windows services?"})
                        self.setState({showDeleteConfirmDialogContinue: true})
                    } else {
                        $.errorAlert(jsonResponseText.message, "Error");
                    }
                } else if (e.status !== 200) {
                  $.errorAlert(JSON.stringify(e), "Error");
                }
            }
        )
    },
    confirmDeleteCallbackContinue: function() {
        var self = this;
        this.props.service.deleteWebServer(this.state.selectedWebServer.id.id, true).then(
            function(response){
            self.refreshData({showDeleteConfirmDialogContinue: false}, function(){self.state.selectedWebServer = null});
        }).caught(
            function(e){
                if (e.responseText !== undefined && e.status !== 200) {
                    var jsonResponseText = JSON.parse(e.responseText);
                    if (jsonResponseText.applicationResponseContent) {
                        $.errorAlert(jsonResponseText.applicationResponseContent, "Error");
                    } else {
                        $.errorAlert(jsonResponseText.message, "Error");
                    }
                } else if (e.status !== 200) {
                    $.errorAlert(JSON.stringify(e), "Error");
                }
            }
        )
    },
    cancelDeleteCallback: function() {
        this.cancelFlag.set();
        this.setState({showDeleteConfirmDialog: false});
    },
    cancelDeleteCallbackContinue: function() {
            this.cancelFlag.set();
            this.setState({showDeleteConfirmDialogContinue: false});
        },
    selectItemCallback: function(item) {
        this.state.selectedWebServer = item;
    },
    editCallback: function(e) {
        var self = this;
        this.props.service.getWebServer(e.data.id.id,
            function(response){
                self.setState({selectedWebServerForEditing:response.applicationResponseContent,
                               showModalFormEditDialog:true});
            }
        );
    },
    componentDidMount: function() {
        this.refreshData({});
    },
    componentDidUpdate: function() {
        // When the data table is refreshed the row gets deselected even if there's a currently
        // selected web server. The code below makes sure that the row is reselected again.
        if (this.state.selectedWebServer && !this.refs.webServerConfigDataTable.refs.dataTableWrapper.getVisibleSelectedRowCount()) {
            this.refs.webServerConfigDataTable.refs.dataTableWrapper.selectRow(this.state.selectedWebServer.name);
        }
    }
});

/**
 * The form that provides data input.
 */
var WebServerConfigForm = React.createClass({
    getInitialState: function() {
        var id = "";
        var name = "";
        var host = "";
        var port = "";
        var httpsPort = "";
        var statusPath = jwalaVars.loadBalancerStatusMount;
        var groupIds = [];

        if (this.props.data !== undefined) {
            id = this.props.data.id;
            name = this.props.data.name;
            host = this.props.data.host;
            port = this.props.data.port;
            httpsPort = this.props.data.httpsPort;
            statusPath = this.props.data.statusPath.path;
            this.props.data.groups.forEach(function(group) {
                groupIds.push(group.id);
            });
        }

        return {
            id: id,
            name: name,
            host: host,
            port: port,
            httpsPort: httpsPort,
            statusPath: statusPath,
            groupIds: groupIds,
            groupMultiSelectData: [],
        }
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {
        var webServerId = this.props.data !== undefined ? this.props.data.id.id : "";
        return  <div className={this.props.className}>
                    <form ref="webServerConfigForm">
                        <input name="webserverId" type="hidden" value={webServerId} />
                        <table>
                            <tr>
                                <td>*Name</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="webserverName" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input ref="webServerName" name="webserverName" type="text"
                                           valueLink={this.linkState("name")} required maxLength="255"
                                           className="width-max"/></td>
                            </tr>
                            <tr>
                                <td>*Host</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="hostName" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="hostName" type="text" valueLink={this.linkState("host")} required
                                           maxLength="255" className="width-max"/></td>
                            </tr>
                            <tr>
                                <td>*HTTP Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="portNumber" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="portNumber" type="text" valueLink={this.linkState("port")} required maxLength="5"/></td>
                            </tr>
                            <tr>
                                <td>HTTPS Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="httpsPort" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="httpsPort" type="text" valueLink={this.linkState("httpsPort")} maxLength="5"/></td>
                            </tr>
                            <tr>
                                <td>Status Path</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="statusPath" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <div className="webServerStatusUrl">
                                        {window.location.protocol + "//" + this.state.host + ":" + (window.location.protocol === "https:" ? this.state.httpsPort : this.state.port) + this.state.statusPath}
                                    </div>
                                    <input name="statusPath" type="text" valueLink={this.linkState("statusPath")}
                                           maxLength="64" className="width-max"/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    *Group
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="groupSelector[]" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                  <div className="webServerStatusUrl">
                                    {this.props.data === undefined && this.state.groupIds.length > 1 ? "The Webserver templates will only be inherited from a single group" : ""}
                                  </div>
                                    <DataMultiSelectBox name="groupSelector[]"
                                                        data={this.state.groupMultiSelectData}
                                                        selectedValIds={this.state.groupIds}
                                                        dataField="id.id"
                                                        val="name"
                                                        className="data-multi-select-box"
                                                        onSelectCallback={this.onSelectGroups}
                                                        idKey="groupId"/>
                                </td>
                            </tr>

                        </table>
                    </form>
                </div>
    },
    onSelectGroups: function(groupIds) {
        this.setState({groupIds:groupIds});
    },
    validator: null,
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({
                                ignore: ":hidden",
                                rules: {
                                    "groupSelector[]": {
                                        required: true
                                    },
                                    "portNumber": {
                                        range: [1, 65535]
                                    },
                                    "httpsPort": {
                                        range: [1, 65535]
                                    },
                                    "webserverName": {
                                        nameCheck: true
                                    },
                                    "hostName": {
                                        hostNameCheck: true
                                    },
                                    "statusPath": {
                                        pathCheck: true
                                    }
                                },
                                messages: {
                                    "groupSelector[]": {
                                        required: "Please select at least 1 group"
                                    }
                                }
                            });

        $(this.refs.webServerName.getDOMNode()).focus();

        $(this.refs.webServerConfigForm.getDOMNode()).submit(function(e) {
            e.preventDefault();
        });

        this.retrieveGroups();
    },
    isValid: function() {
        this.validator.form();
        if (this.validator.numberOfInvalids() === 0) {
            return true;
        }
        return false;
    },
    retrieveGroups: function() {
        var self = this;
        groupService.getGroups().then(function(response){
                                          self.setState({groupMultiSelectData:response.applicationResponseContent});
                                      });
    }
});

/**
 * The Web Server data table.
 */
var WebServerDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                        {sTitle:"Name", mData:"name", jwalaType:"custom", jwalaRenderCfgFn:this.renderNameLink},
                        {sTitle:"Host", mData:"host", maxDisplayTextLen:45},
                        {sTitle:"Port", mData:"port"},
                        {sTitle:"HTTPS Port", mData:"httpsPort"},
                        {sTitle:"Status Path", mData:"statusPath.path", maxDisplayTextLen:20},
                        {sTitle:"Group",
                         mData:"groups",
                         jwalaType:"array",
                         displayProperty:"name",
                         sWidth: "40%", maxDisplayTextLen:20}];
        return <JwalaDataTable ref="dataTableWrapper"
                               tableId="webserver-config-datatable"
                               tableDef={tableDef}
                               colHeaders={["JVM Name", "Host Name"]}
                               data={this.props.data}
                               selectItemCallback={this.props.selectItemCallback}
                               editCallback={this.props.editCallback}
                               rowSubComponentContainerClassName="row-sub-component-container"
                               isColResizable={true}/>
    },
    renderNameLink:function(dataTable, data, aoColumnDefs, itemIndex) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function ( nTd, sData, oData, iRow, iCol ) {
            return React.renderComponent(React.createElement("button", {className:"button-link", title:sData}, sData), nTd, function() {
                $(this.getDOMNode()).click(oData, self.props.editCallback);
            });
        };
    }
});
