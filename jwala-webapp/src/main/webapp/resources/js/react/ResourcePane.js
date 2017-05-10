/** @jsx React.DOM */

/**
 * Displays the available resources.
 *
 * TODO: Unit tests.
 */
var ResourcePane = React.createClass({
    getInitialState: function() {
        return ResourcePane.INITIAL_STATES;
    },
    render: function() {
        var metaData = [{ref: "createBtn", icon: "ui-icon-plusthick", title: "create", onClickCallback: this.createResource},
        			    {ref: "deleteBtn", icon: "ui-icon-trash", title: "delete", onClickCallback: this.deleteResource}];
        var toolbar = <RToolbar className="resourcePane toolbarContainer" metaData={metaData}/>;
        if (this.state.resourceOptions.length > 0) {
            return <div className="ResourcePane">
                       {toolbar}
                       <RListBox ref="listBox" options={this.state.resourceOptions} selectCallback={this.selectCallback}
                                 multiSelect={true} onContextMenu={this.onContextMenu} />

                       <RMenu ref="groupLevelWebAppsResourceMenu"
                              menuItems={[{key: "deploy", label: "deploy", menuItems: [{key: "deployToAllHosts", label: "all hosts"},
                                                                                       {key: "deployToAHost", label: "a host"}]}]}
                              onItemClick = {this.onGroupLevelWebAppsResourceContextMenuItemClick}/>

                       <RMenu ref="externalPropertiesResourceMenu"
                              menuItems={[]}
                              onItemClick = {this.onExternalPropertiesResourceContextMenuItemClick}/>

                       <RMenu ref="deployResourceMenu" menuItems={[{key: "deploy", label: "deploy"}]}
                              onItemClick ={this.onDeployResourceContextMenuItemClick}/>

                       <ModalDialogBox ref="confirmDeployResourceDlg"
                                       okLabel="Yes"
                                       okCallback={this.deployResourceCallback}
                                       cancelLabel="No"
                                       position="fixed" />

                       <ModalDialogBox ref="selectHostDlg" okCallback={this.selectHostDlgOkClickCallback} position="fixed" />

                   </div>
        }

        return <div className="ResourcePane">
                   {toolbar}
                   <span>{this.state.data === null ? "Please select a JVM, Web Server or Web Application..." : "No resources found..."}</span>
               </div>
    },
    getData: function(data) {
        this.state.data = data; // We don't want the component to render that's why we just assign data via '='
        if (data !== null) {
            if (data.rtreeListMetaData.entity === "jvms") {
                this.props.jvmService.getResources(data.jvmName, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webServers") {
                this.props.wsService.getResources(data.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webApps" && data.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                this.props.webAppService.getResources(data.name, data.rtreeListMetaData.parent.jvmName, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webApps" && data.rtreeListMetaData.parent.rtreeListMetaData.entity === "webAppSection") {
                ServiceFactory.getResourceService().getAppResources(data.group.name, data.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webServerSection") {
                this.props.groupService.getGroupWebServerResources(data.rtreeListMetaData.parent.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "jvmSection") {
                this.props.groupService.getGroupJvmResources(data.rtreeListMetaData.parent.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "extProperties") {
                this.props.resourceService.getExternalPropertiesFile(this.getDataCallback);
            }
        } else {
            // Reset states
            this.setState(ResourcePane.INITIAL_STATES);
        }
    },
    getDataCallback: function(response) {
        var options = [];
        response.applicationResponseContent.forEach(function(resourceName){
            if (resourceName.entityType && resourceName.resourceName) {
                options.push({value:resourceName.resourceName, label:resourceName.resourceName, entityType: resourceName.entityType})
            } else {
                options.push({value: resourceName, label: resourceName});
            }
        });
        this.setState({resourceOptions: options});
    },
    selectCallback: function(value) {
         var groupJvmEntityType;
         this.state.resourceOptions.some(function(resource){
            if(resource.value && resource.value === value) {
                groupJvmEntityType = resource.entityType;
                return true;
            }
         });
         return this.props.selectCallback(value, groupJvmEntityType);
    },
    getSelectedValue: function() {
        if (this.refs.listBox !== undefined) {
            return this.refs.listBox.getSelectedValue();
        }
        return null;
    },
    getCheckedItems: function() {
        if (this.refs.listBox !== undefined) {
            return this.refs.listBox.getCheckedItems();
        }
        return null;
    },
    createResource: function() {
        this.props.createResourceCallback(this.state.data);
    },
    deleteResource: function() {
        if (this.refs.listBox.getCheckedItems().length > 0) {
            this.props.deleteResourceCallback();
        } else {
            $.alert("Please select resource(s) to delete.", "Info", true);
        }
    },
    // Right click a resource is called onContextMenu event in js.
    onContextMenu: function(e, val) {
        this.state["rightClickedItem"] = val;
        if (this.state.data.rtreeListMetaData.entity === "webApps" && this.state.data.rtreeListMetaData.parent.rtreeListMetaData.entity === "webAppSection") {
            this.refs.groupLevelWebAppsResourceMenu.show((e.clientY - 5) + "px", (e.clientX - 5) + "px");
        } else if (this.state.data.rtreeListMetaData.entity === "extProperties") {
            this.refs.externalPropertiesResourceMenu.show((e.clientY - 5) + "px", (e.clientX - 5) + "px");
        } else {
            this.refs.deployResourceMenu.show((e.clientY - 5) + "px", (e.clientX - 5) + "px");
        }
    },
    onGroupLevelWebAppsResourceContextMenuItemClick: function(val) {
        var self = this;
        if (val === "deployToAHost") {
            var groupName = this.state.data.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
            ServiceFactory.getGroupService().getHosts(groupName).then(function(response){
                self.refs.selectHostDlg.show("Select a host",
                    <SelectHostWidget getSelectedItemCallback={self.getSelectedItemCallback}>{response.applicationResponseContent}</SelectHostWidget>);
            }).caught(function(e){
                $.errorAlert(e, "Error");
            });
        } else {
            this.refs.confirmDeployResourceDlg.show("Deploy resource confirmation", 'Are you sure you want to deploy "'
                                                    + this.state.rightClickedItem + '" to all hosts ?');
        }
    },
    onExternalPropertiesResourceContextMenuItemClick: function(val) {
        var self = this;
        if (val === "deployToAHost") {
            ServiceFactory.getGroupService().getAllHosts().then(function(response){
                self.refs.selectHostDlg.show("Select a host",
                    <SelectHostWidget getSelectedItemCallback={self.getSelectedItemCallback}>{response.applicationResponseContent}</SelectHostWidget>);
            }).caught(function(e){
                $.errorAlert(e, "Error");
            });
        } else {
            this.refs.confirmDeployResourceDlg.show("Deploy resource confirmation", 'Are you sure you want to deploy "'
                                                    + this.state.rightClickedItem + '" to all hosts ?');
        }
    },
    onDeployResourceContextMenuItemClick: function(val) {
        var name = this.state.data.name ? this.state.data.name : this.state.data.jvmName;
        var resourceName = this.state.rightClickedItem;
        var entityNode = this.state.data.rtreeListMetaData.entity;
        if ( entityNode === "webServerSection" || entityNode === "jvmSection"){
            var htmlMsg = <div>Are you sure you want to deploy {'"' + resourceName + '"'} to {'"' + name + '"'}? <br/><br/> Any previous customizations to an individual instance of {'"' + resourceName + '"'} will be overwritten.</div>;
            this.refs.confirmDeployResourceDlg.show("Deploy resource confirmation", htmlMsg);
        } else {
            var msg = 'Are you sure you want to deploy "' + resourceName + '" to "' + name + '"?';
            this.refs.confirmDeployResourceDlg.show("Deploy resource confirmation", msg);
        }
    },
    displayDeployErrorMessage: function(response){
        $.errorAlert(ResourcePane.parseDetailedErrorMsg(response, ResourcePane.DEFAULT_DEPLOY_ERR_MSG), "");
    },
    deployResourceCallback: function() {
        var data = this.state.data;
        var self = this;
        if (data !== null) {
            if (data.rtreeListMetaData.entity === "jvms") {
                ServiceFactory.getResourceService().deployJvmResource(data.jvmName, this.state.rightClickedItem)
                    .then(function(response) {
                        $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                    }).caught(function(response){
                        console.log(response);
                        self.displayDeployErrorMessage(response);
                    });
            } else if (data.rtreeListMetaData.entity === "webServers") {
                ServiceFactory.getResourceService().deployWebServerResource(data.name, this.state.rightClickedItem)
                    .then(function(response){
                        $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                    }).caught(function(response) {
                        console.log(response);
                        self.displayDeployErrorMessage(response);
                    });
            } else if (data.rtreeListMetaData.entity === "webApps" && data.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                var groupName = this.state.data.rtreeListMetaData.parent.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                var jvmName = this.state.data.rtreeListMetaData.parent.jvmName;
                ServiceFactory.getResourceService().deployJvmWebAppResource(this.state.data.name, groupName, jvmName, this.state.rightClickedItem)
                    .then(function(response){
                        $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                    }).caught(function(response){
                        console.log(response);
                        self.displayDeployErrorMessage(response);
                    });
            } else if (data.rtreeListMetaData.entity === "webApps" && data.rtreeListMetaData.parent.rtreeListMetaData.entity === "webAppSection") {
                var groupName = this.state.data.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                ServiceFactory.getResourceService().deployGroupAppResourceToHost(groupName, this.state.rightClickedItem, "", this.state.data.name)
                    .then(function(response){
                        $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                    }).caught(function(response){
                        console.log(response);
                        self.displayDeployErrorMessage(response);
                    });
            } else if (data.rtreeListMetaData.entity === "webServerSection") {
                ServiceFactory.getResourceService().deployGroupLevelWebServerResource(this.state.data.rtreeListMetaData.parent.name,
                    this.state.rightClickedItem)
                        .then(function(response){
                            $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                        }).caught(function(response){
                            console.log(response);
                            self.displayDeployErrorMessage(response);
                        });
            } else if (data.rtreeListMetaData.entity === "jvmSection") {
                ServiceFactory.getResourceService().deployGroupLevelJvmResource(this.state.data.rtreeListMetaData.parent.name,
                    this.state.rightClickedItem)
                        .then(function(response){
                            $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                        }).caught(function(response){
                            console.log(response);
                            self.displayDeployErrorMessage(response);
                        });
            }
        }
        this.refs.confirmDeployResourceDlg.close();
    },
    getSelectedItemCallback: function(host) {
        this.state.host = host;
    },
    selectHostDlgOkClickCallback: function() {
        var self = this;
        this.refs.selectHostDlg.close();
        if (this.state.data.rtreeListMetaData.entity === "extProperties"){
            ServiceFactory.getResourceService().deployResourceToHost(this.state.rightClickedItem, this.state.host)
                .then(function(response){
                    $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                }).caught(function(response){
                    console.log(response);
                    self.displayDeployErrorMessage(response);
                });
        } else {
            var groupName = this.state.data.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
            ServiceFactory.getResourceService().deployGroupAppResourceToHost(groupName, this.state.rightClickedItem, this.state.host, this.state.data.name)
                .then(function(response){
                    $.alert("Deploy successful!", ResourcePane.DEPLOY_RESOURCE_TITLE, true);
                }).caught(function(response){
                    console.log(response);
                    self.displayDeployErrorMessage(response);
                });
        }
    },
    statics: {
        INITIAL_STATES: {resourceOptions: [], showModalResourceTemplateMetaData: false, data: null, rightClickedItem: null, host: null},
        parseDetailedErrorMsg: function(response, defaultErrMsg) {
            try {
                return JSON.parse(response.responseText).message;
            } catch (e) {
                console.log("There was an error parsing the detailed error message from the response:");
                console.log(response);
                console.log("The exception is:")
                console.log(e);
                console.log('Returning default error message = "' + defaultErrMsg + '"');
                return defaultErrMsg;
            }
        },
        DEFAULT_DEPLOY_ERR_MSG: "There was an error deploying the resource!",
        DEPLOY_RESOURCE_TITLE: "Deploy Resource"
    }
});

SelectHostWidget = React.createClass({
    getInitialState: function() {
        return {host: ($.isArray(this.props.children) && this.props.children.length > 0) ? this.props.children[0] : null};
    },
    render: function() {
        var self = this;
        var options = [];
        if ($.isArray(this.props.children)) {
            this.props.children.forEach(function(host){
                if (self.state.host === host) {
                    options.push(<option value={host} selected>{host}</option>);
                } else {
                    options.push(<option value={host}>{host}</option>);
                }
            });
            return <select className="selectHostStyle" ref="select" onChange={this.onSelectChange}>{options}</select>;
        }
        return null;
    },
    componentDidMount: function() {
        this.props.getSelectedItemCallback(this.state.host);
    },
    componentDidUpdate: function() {
        this.props.getSelectedItemCallback(this.state.host);
    },
    onSelectChange: function(e) {
        var selectBox = this.refs.select.getDOMNode();
        var host = selectBox.options[selectBox.options.selectedIndex].value;
        this.setState({host: host});
    }
});

