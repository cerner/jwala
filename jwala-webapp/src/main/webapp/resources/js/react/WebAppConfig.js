/** @jsx React.DOM */
var WebAppConfig = React.createClass({
    getInitialState: function() {
        return {selectedWebApp: null, groupData: null, err: null};
    },
    render: function() {

        if (!this.state.groupData && !this.state.err) {
            return <div>Loading...</div>;
        } if (this.state.err) {
            return <div className="WebAppConfig msg">{this.state.err.message}</div>;
        }

        return <div className="WebAppConfig">
                   <div className="btnContainer">
                       <GenericButton label="Delete" accessKey="d" callback={this.delBtnCallback}/>
                       <GenericButton label="Add" accessKey="a" callback={this.addBtnCallback}/>
                   </div>

                   <RDataTable ref="dataTable"
                               tableIndex="id.id"
                               colDefinitions={[{key: "id", isVisible: false},
                                                {key: "secure", renderCallback: this.renderSecureColumn, sortable: false},
                                                {title: "WebApp Name", key: "name", renderCallback: this.renderWebAppNameCallback},
                                                {title: "Context", key: "webAppContext"},
                                                {title: "Web Archive", key: "warName"},
                                                {key: "actionIcons", renderCallback: this.renderActionIcons, sortable: false},
                                                {title: "Group", key:"group.name"}]}
                               data={this.state.webAppData}
                               selectItemCallback={this.selectItemCallback}
                               deselectAllRowsCallback={this.deselectAllRowsCallback}/>

                   <ModalDialogBox ref="modalAddWebAppDlg"
                                   title="Add Web Application"
                                   okCallback={this.okAddCallback}
                                   content={<WebAppConfigForm ref="webAppAddForm"/>}/>

                   <ModalDialogBox ref="modalEditWebAppDlg"
                                   title="Edit Web Application"
                                   contentReferenceName="webAppEditForm"
                                   okCallback={this.okEditCallback}/>

                   <ModalDialogBox ref="confirmDeleteWebAppDlg"
                                   okLabel="Yes"
                                   okCallback={this.confirmDeleteCallback}
                                   cancelLabel="No"/>

                   <ModalDialogBox title="Upload WAR"
                                   ref="uploadWarDlg"
                                   okLabel="Upload"
                                   okCallback={this.uploadCallback}
                                   cancelCallback={this.uploadWarDlgCancelCallback}
                                   cancelLabel="Close"
                                   contentReferenceName="uploadWarWidget" />

                   <ModalDialogBox ref="confirmDeleteWarDlg"
                                   okLabel="Yes"
                                   okCallback={this.deleteWarCallback}
                                   cancelLabel="No"/>
               </div>;
    },
    componentDidMount: function() {
        this.loadTableData();
    },
    loadTableData: function(afterLoadCallback) {
        let self = this;
        let groupData;
        groupService.getGroups().then(function(response){
            groupData = response.applicationResponseContent;
            if (groupData.length > 0) {
                return ServiceFactory.getWebAppService().getWebApps();
            }
            throw new Error("There are no groups defined in Jwala. Please define a group to be able to add web applications.");
        }).then(function(response){
            if ($.isFunction(afterLoadCallback)) {
                afterLoadCallback();
            }
            self.setState({"groupData": groupData, "webAppData": response.applicationResponseContent});
        }).caught(function(response){
            console.log(response);
            self.setState({err: response});
        });
    },
    renderWebAppNameCallback: function(name) {
        var self = this;
        return <button className="button-link" onClick={function(){self.onWebAppNameClick(name)}}>{name}</button>
    },
    renderActionIcons: function(id, data) {
        var self = this;
        var uploadBtn = data.warName ? null : <RButton title="Upload WAR" className="upArrowIconBtn ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                                                       hoverClassName="ui-state-hover" spanClassName="ui-icon ui-icon-arrowreturnthick-1-n" onClick={function(){self.onUploadWarBtnClicked(data)}} />
        var deleteBtn = data.warName ? <RButton title="Delete WAR" className="trashIconBtn ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                                                hoverClassName="ui-state-hover" spanClassName="ui-icon ui-icon-trash" onClick={function(){self.onDeleteWarBtnClicked(data)}} /> : null;
        return <div>
                   {uploadBtn}
                   {deleteBtn}
               </div>
    },
    addBtnCallback: function() {
        this.refs.modalAddWebAppDlg.show();
    },
    okAddCallback: function() {
        var self = this;
        if (this.refs.webAppAddForm.isValid()) {
            var serializedData = $(this.refs.webAppAddForm.refs.form.getDOMNode()).serializeArray();
            ServiceFactory.getWebAppService().insertNewWebApp(
                serializedData,
                function(){
                    self.refs.modalAddWebAppDlg.close();
                    self.loadTableData(function(){
                        self.refs.dataTable.deselectAllRows();
                    });
                },
                function(errMsg){
                    $.errorAlert(errMsg, "Error");
                });
        }
    },
    okEditCallback: function() {
        var self = this;
        if (this.refs.modalEditWebAppDlg.refs.webAppEditForm.isValid()) {
            var serializedData = $(this.refs.modalEditWebAppDlg.refs.webAppEditForm.refs.form.getDOMNode()).serializeArray();
            this.props.service.updateWebApp(serializedData,
                                            function(response){
                                                self.refs.modalEditWebAppDlg.close();
                                                self.loadTableData(function(){
                                                    self.state.selectedWebApp = self.refs.dataTable.getSelectedItem();
                                                });
                                            },
                                            function(errMsg) {
                                                $.errorAlert(errMsg, "Error");
                                            });
        }
    },
    selectItemCallback: function(item) {
        this.state.selectedWebApp = item;
    },
    deselectAllRowsCallback: function() {
        this.state.selectedWebApp = null;
    },
    delBtnCallback: function() {
        if (this.state.selectedWebApp) {
            this.refs.confirmDeleteWebAppDlg.show("Delete Web Application", "Are you sure you want to delete " + this.state.selectedWebApp["str-name"] + " ?");
        }
    },
    confirmDeleteCallback: function() {
        var self = this;
        ServiceFactory.getWebAppService().deleteWebApp(this.state.selectedWebApp.id.id).then(function(){
            self.refs.confirmDeleteWebAppDlg.close();
            self.loadTableData(function(){
                self.state.selectedWebApp = null;
            });
        });
    },
    onWebAppNameClick: function(name) {
        var self = this;
        ServiceFactory.getWebAppService().getWebAppByName(name).then(function(response){
            var formData = {};
            formData["id"] = response.applicationResponseContent.id;
            formData["name"] = response.applicationResponseContent.name;
            formData["context"] = response.applicationResponseContent.webAppContext;
            formData["groupIds"] = [response.applicationResponseContent.group.id];
            formData["secure"] = response.applicationResponseContent.unpackWar;
            formData["unpackWar"] = response.applicationResponseContent.unpackWar;
            formData["loadBalance"] = response.applicationResponseContent.loadBalance;
            formData["loadBalanceAcrossServers"] = response.applicationResponseContent.loadBalanceAcrossServers;
            self.refs.modalEditWebAppDlg.show("Edit Web Application",
                <WebAppConfigForm formData={formData}/>);
        });
    },
    onUploadWarBtnClicked: function(data) {
        this.refs.uploadWarDlg.show("Upload WAR", <UploadWarWidget service={this.props.service} data={data}
                                                                   afterUploadCallback={this.afterUploadCallback} />);
    },
    uploadCallback: function() {
        this.refs.uploadWarDlg.refs.uploadWarWidget.performUpload();
    },
    afterUploadCallback: function(files) {
        this.loadTableData();
        this.refs.uploadWarDlg.close();
        $.alert(files[0].name + " upload successful!");
    },
    onDeleteWarBtnClicked: function(data) {
        var self = this;
        this.refs.confirmDeleteWarDlg.show("Delete WAR", "Are you sure you want to delete " + data.warName + " ?",
                                           function(){self.deleteWarCallback(data)});
    },
    deleteWarCallback: function(data) {
        var self = this;
        ServiceFactory.getResourceService()
            .deleteResources([data.warName], data.group.name, null, null, data.name).then(function(){
                self.refs.confirmDeleteWarDlg.close();
                self.loadTableData();
            }).caught(function(response){
                $.errorAlert(response, "Error");
            });
    }
})

var WebAppConfigForm = React.createClass({
    mixins: [
        React.addons.LinkedStateMixin
    ],
    getInitialState: function() {
        var name = this.props.formData && this.props.formData.name ? this.props.formData.name : null;
        var context = this.props.formData && this.props.formData.context ? this.props.formData.context : null;
        var groupIds = this.props.formData && this.props.formData.groupIds && this.props.formData.groupIds instanceof Array ? this.props.formData.groupIds : [];
        var secure = this.props.formData && this.props.formData.secure ? this.props.formData.secure : false;
        var unpackWar = this.props.formData && this.props.formData.unpackWar ? this.props.formData.unpackWar : false;
        var loadBalanceAcrossServers = this.props.formData && this.props.formData.loadBalanceAcrossServers ? this.props.formData.loadBalanceAcrossServers : false;
        return {groupData: {}, name: name, context: context, groupIds: groupIds, secure: secure, unpackWar: unpackWar,
                loadBalanceAcrossServers: loadBalanceAcrossServers};
    },
    render: function() {
        var idTextHidden = null;
        if (this.props.formData && this.props.formData.id) {
            idTextHidden = <input type="hidden" name="webappId" value={this.props.formData.id.id}/>;
        }

        return <div>
                   <form ref="form">
                       {idTextHidden}
                       <label>*Name</label><br/>
                       <label htmlFor="name" className="error"/>
                       <input ref="webServerName" name="name" type="text" valueLink={this.linkState("name")} maxLength="255" required
                              className="width-max"/>
                       <label>*Context path</label><br/>
                       <label htmlFor="webappContext" className="error"/>
                       <input name="webappContext" type="text" valueLink={this.linkState("context")}
                              required maxLength="255" className="width-max"/>
                       <label>*Associated Group</label><br/>
                       <label htmlFor="groupId" className="error"/>
                       <DataMultiSelectBox name="groupId"
                                           data={this.state.groupData}
                                           selectedValIds={this.state.groupIds}
                                           dataField="id.id"
                                           val="name"
                                           className="data-multi-select-box"
                                           onSelectCallback={this.onSelectGroups}
                                           idKey="groupId"
                                           singleSelect={true}/>
                       <label>Secure</label>
                       <br/>
                       <input name="secure" type="checkbox" checked={this.state.secure} onChange={this.onSecureCheckBoxChange}/>
                       <br/>
                       <label>Unpack WAR</label>
                       <br/>
                       <input name="unpackWar" type="checkbox" checked={this.state.unpackWar} onChange={this.onUnpackWarCheckboxChange}/>
                       <br/>
                       <label>Load Balance</label>
                       <br/>
                       <input type="radio"
                              name="loadBalance"
                              value="acrossServers"
                              checked={this.state.loadBalanceAcrossServers}
                              onClick={this.onLbAcrossServersCheckboxChange}>Across Servers</input>
                       <input type="radio"
                              name="loadBalance"
                              value="locally"
                              checked={!this.state.loadBalanceAcrossServers}
                              onClick={this.onLbLocallyCheckboxChange}>Local Only</input>
                   </form>
               </div>
    },
    validator: null,
    componentDidMount: function() {
        var self = this;
        ServiceFactory.getGroupService().getGroups().then(function(response){
            self.setState({groupData: response.applicationResponseContent});
        }).lastly(function(){
            $(self.refs.webServerName.getDOMNode()).focus();
            if (self.validator === null) {
                self.validator = $(self.getDOMNode().children[0])
                    .validate({ignore: ":hidden", rules: {"groupId": {required: true}},
                               messages: {
                                   "groupId": {
                                       required: "Please select at least 1 group"
                                   }
                               }});
                    }
        });
    },
    onSecureCheckBoxChange: function() {
        this.setState({secure: !this.state.secure});
    },
    onUnpackWarCheckboxChange: function() {
        this.setState({unpackWar: !this.state.unpackWar});
    },
    onLbAcrossServersCheckboxChange: function() {
        this.setState({loadBalanceAcrossServers: true});
    },
    onLbLocallyCheckboxChange: function() {
        this.setState({loadBalanceAcrossServers: false});
    },
    onSelectGroups: function(groupIds) {
        this.setState({groupIds: groupIds});
    },
    isValid: function() {
        if (this.validator !== null) {
            this.validator.form();
            if (this.validator.numberOfInvalids() === 0) {
                return true;
            }
        } else {
            alert("There is no validator for the form!");
        }
        return false;
    }
});

/**
 * Upload WAR widget.
 */
var UploadWarWidget = React.createClass({
    getInitialState: function() {
        return {
            properties: {},
            showProperties: false,
            uploadFilename: "",
            deployPath: "",
            uploadData: null,
            assignToJvms: false
        }
    },
    render: function() {
        var self = this;

        var rows = [];
        for (var key in this.state.properties) {
            if (UploadWarWidget.isPossiblePath(this.state.properties[key]) && UploadWarWidget.isEncrypted(this.state.properties[key])) {
                rows.push(<PropertyRow key={key} onAddPropertiesClickCallback={self.onAddPropertiesClick}
                                       propertyName={key} propertyValue={this.state.properties[key]} />);
            }
        }

        var propertiesTable = <div><table ref="propertiesTable">{rows}</table></div>;

        console.log('v1.0/resources/' + this.props.data.id.id + '/war');

        return <div className="UploadWarWidget">
                   <form ref="warUploadForm">
                       <label>*WAR File</label>
                       <label ref="fileInputLabel" htmlFor="fileInput" className="error"/>
                       <input ref="fileInput" name="fileInput" type="file" required onChange={this.onChangeFileInput}/>
                       <br/>
                       <input type="checkbox" value={this.state.assignToJvms} onChange={this.onChangeAssignToJvms}>
                           <span>Assign to JVMs</span>
                       </input>
                       <br/>
                       <label>Deploy Path</label>
                       <input ref="deployPathInput" name="deployPath" value={this.state.deployPath} onChange={this.onChangeDeployPath}/>
                       <div className="openCloseProperties">
                            <span>Select deploy path from properties</span>
                            <span ref="openClosePropertiesIcon"
                                  className={this.state.showProperties ? "ui-icon ui-icon-triangle-1-s" : "ui-icon ui-icon-triangle-1-e"}
                                  onClick={this.openClosePropertiesIconCallback} />
                            {this.state.showProperties ? propertiesTable : null}
                       </div>
                   </form>
               </div>;
    },
    onComponentDidMount: function() {
        $(this.refs.warUploadForm.getDOMNode()).validate({
                                rules: {"fileInput": {
                                            required: true
                                            }
                                        },
                                messages: {
                                    "fileInput": {
                                        required: "Please select a file for upload"
                                     },
                                }});
    },
    openClosePropertiesIconCallback: function() {
        if (this.state.showProperties) {
            this.setState({showProperties: false});
        } else {
            ServiceFactory.getAdminService().reloadProperties(this.onPropertiesLoad);
        }
    },
    onPropertiesLoad: function(response) {
        this.setState({properties: response.applicationResponseContent, showProperties: true});
    },
    onChangeFileInput: function() {
        // This is necessary since jquery validate does not clear the error
        // after user specifies a file not unless user clicks on something
        // Note: We are not modifying the DOM here in such a way that will affect React
        if ($(this.refs.fileInputLabel.getDOMNode()).hasClass("error")) {
            $(this.refs.fileInputLabel.getDOMNode()).removeClass("error");
            $(this.refs.fileInputLabel.getDOMNode()).html("");
            $(this.refs.fileInput.getDOMNode()).removeClass("error");
        }

        this.setState({uploadFilename: $(this.refs.fileInput.getDOMNode()).val()});
    },
    onChangeAssignToJvms: function() {
        this.setState({assignToJvms: !this.state.assignToJvms});
    },
    onChangeDeployPath: function() {
        this.setState({deployPath: $(this.refs.deployPathInput.getDOMNode()).val()});
    },
    onAddPropertiesClick: function(val) {
        this.setState({deployPath: this.state.deployPath + val});
    },
    performUpload: function() {
        if (!$(this.refs.warUploadForm.getDOMNode()).validate().form()) {
            return;
        }

        var self = this;
        var formData = new FormData();

        formData.append("assignToJvms", this.state.assignToJvms);
        formData.append("templateFile", this.refs.fileInput.getDOMNode().files[0]);
        $(this.refs.warUploadForm.getDOMNode()).serializeArray().forEach(function(item){
            formData.append(item.name, item.value);
        });

        ServiceFactory.getResourceService().createResource(this.props.data.group.name, null, null, this.props.data.name,
            formData, null, this.state.uploadFilename.split("\\").pop()).then(function(response){
                if ($.isFunction(self.props.afterUploadCallback)) {
                    self.props.afterUploadCallback(self.refs.fileInput.getDOMNode().files);
                }
            }).caught(function(response){
                $.errorAlert(response, "Error");
            });
    },
    statics: {
        isPossiblePath: function(path) {
            return path.indexOf(":") > -1 || path.indexOf("\\") > -1 || path.indexOf("/") > -1;
        },
        isEncrypted: function(val) {
            return val.charAt(val.length - 1) !== '=';
        }
    }
});

var PropertyRow = React.createClass({
    render: function() {
        var self = this;
        return <tr>
                   <td><span className="ui-icon ui-icon-plus" onClick={function(){self.props.onAddPropertiesClickCallback("${vars['" + self.props.propertyName + "']}")}} /></td>
                   <td><span className="key">{self.props.propertyName}</span>: {self.props.propertyValue}</td>
               </tr>;
    }
});