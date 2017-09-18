/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<ResourceEditor ref="resourceEditor"
                                                resourceService={this.props.resourceService}
                                                groupService={this.props.groupService}
                                                jvmService={this.props.jvmService}
                                                wsService={this.props.wsService}
                                                webAppService={this.props.webAppService}
                                                getTemplateCallback={this.getTemplateCallback}
                                                selectEntityCallback={this.selectEntityCallback}
                                                selectResourceTemplateCallback={this.selectResourceTemplateCallback}
                                                createResourceCallback={this.createResourceCallback}
                                                deleteResourceCallback={this.deleteResourceCallback}/>);
        splitterComponents.push(<XmlTabs jvmService={this.props.jvmService}
                                         wsService={this.props.wsService}
                                         webAppService={this.props.webAppService}
                                         groupService={this.props.groupService}
                                         resourceService={this.props.resourceService}
                                         ref="xmlTabs"
                                         uploadDialogCallback={this.launchUpload}
                                         updateGroupTemplateCallback={this.launchUpdateGroupTemplate}
                                         updateGroupMetaDataCallback={this.launchUpdateGroupMetaData}
                                         updateExtPropsAttributesCallback={this.updateExtPropsAttributesCallback}
                                         />);

        var splitter = <RSplitter disabled={true}
                                  components={splitterComponents}
                                  orientation={RSplitter.VERTICAL_ORIENTATION}
                                  updateCallback={this.verticalSplitterDidUpdateCallback}
                                  onSplitterChange={this.onChildSplitterChangeCallback}/>;

        return <div className="react-dialog-container resources-div-container">
                    <div className="resource-container">{splitter}</div>
                    <ModalDialogBox
                     title="Confirm update"
                     show={false}
                     cancelCallback={this.cancelUpdateGroupTemplateCallback}
                     ref="templateUpdateGroupModal"/>
                    <ModalDialogBox
                     title="Confirm meta data update"
                     show={false}
                     cancelCallback={this.cancelUpdateGroupMetaDataCallback}
                     ref="metaDataUpdateGroupModal"/>
                    <ModalDialogBox ref="selectTemplateFilesModalDlg"
                                                        title="Upload External Properties"
                                                        show={false}
                                                        okCallback={this.onCreateResourceOkClicked}
                                                        content={<SelectTemplateFilesWidget ref="selectTemplateFileWidget" getResourceOptions={this.getResourceOptions}/>}/>
                    <ModalDialogBox ref="createResourceModalDlg"
                                    title="Create Resource Template"
                                    show={false}
                                    okCallback={this.onCreateResourceOkClicked}
                                    content={<SelectMetaDataAndTemplateFilesWidget ref="selectMetaDataAndTemplateFilesWidget" uploadMetaData={false} parent={this} />}/>
                    <ModalDialogBox ref="selectMetaDataAndTemplateFilesModalDlg"
                                    title="Create Resource Template"
                                    show={false}
                                    okCallback={this.onCreateResourceOkClicked}
                                    content={<SelectMetaDataAndTemplateFilesWidget ref="selectMetaDataAndTemplateFilesWidget" uploadMetaData={true} hideUploadMetaDataOption={true} />}/>
                    <ModalDialogBox ref="confirmDeleteResourceModalDlg"
                                    title="Confirm Resource Template Deletion"
                                    show={false}
                                    okCallback={this.confirmDeleteResourceCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to delete the selected resource template(s) ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No" />
                    <ModalDialogBox ref="confirmDeletePropertyModalDlg"
                                    title="Confirm Property file deletion"
                                    show={false}
                                    okCallback={this.confirmDeleteResourceCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to delete the property file ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No" />
                </div>
    },
    validator: null,
    getResourceOptions: function() {
        return this.refs.resourceEditor.refs.resourcePane.state.resourceOptions;
    },
    componentDidMount: function() {
        MainArea.unsavedChanges = false;
        window.onbeforeunload = function() {
                                    console.log(this.refs);
                                    if (MainArea.unsavedChanges === true) {
                                        return "A resource template has recently been modified.";
                                    }
                                };
    },
    onChildSplitterChangeCallback: function(dimensions) {
        this.refs.resourceEditor.onParentSplitterChange(dimensions);
    },
    generateXmlSnippetResponseCallback: function(response) {
        this.refs.xmlTabs.refreshXmlDisplay(response.applicationResponseContent);
    },
    getTemplateCallback: function(template) {
        this.refs.xmlTabs.refreshTemplateDisplay(template);
    },
    selectEntityCallback: function(data, entity, parent) {
        if (this.refs.xmlTabs.refs.codeMirrorComponent !== undefined && this.refs.xmlTabs.refs.codeMirrorComponent.isContentChanged() ||
            this.refs.xmlTabs.refs.metaDataEditor !== undefined && this.refs.xmlTabs.refs.metaDataEditor.isContentChanged()) {
            var ans = confirm("All your changes won't be saved if you view another resource. Are you sure you want to proceed ?");
            if (!ans) {
                return false;
            }
            MainArea.unsavedChanges = false;
        }

        this.refs.xmlTabs.setState({entityType: data ? data.rtreeListMetaData.entity : null,
                                    entity: data,
                                    entityGroupName: data ? data.rtreeListMetaData.parent.name : null,
                                    resourceTemplateName: null,
                                    entityParent: data ? data.rtreeListMetaData.parent : null,
                                    template: ""});
        return true;
    },
    selectResourceTemplateCallback: function(entity, resourceName, groupJvmEntityType) {
        if (this.refs.xmlTabs.refs.codeMirrorComponent !== undefined && this.refs.xmlTabs.refs.codeMirrorComponent.isContentChanged() ||
            this.refs.xmlTabs.refs.metaDataEditor !== undefined && this.refs.xmlTabs.refs.metaDataEditor.isContentChanged()) {
            var ans = confirm("All your changes won't be saved if you view another resource. Are you sure you want to proceed ?");
            if (!ans) {
                return false;
            }
            MainArea.unsavedChanges = false;
        }

        this.refs.xmlTabs.setState({groupJvmEntityType: groupJvmEntityType});
        this.refs.xmlTabs.reloadTemplate(entity, resourceName, groupJvmEntityType);
        return true;
    },
    templateComponentDidMount: function(template) {
         var fileName = this.refs.xmlTabs.state.resourceTemplateName;
         var entityType = this.refs.xmlTabs.state.entityType;
        template.setState({
            fileName: fileName,
            entityName: ResourcesConfig.getEntityName(this.refs.xmlTabs.state.entity, this.refs.xmlTabs.state.entityType),
            entityType: entityType,
            entityGroupName: this.refs.xmlTabs.state.entityParent.name
        })
    },
    confirmUpdateGroupDidMount: function (confirmDialog){
        var entityType = this.refs.xmlTabs.state.entityType;
        var entityGroupName = this.refs.xmlTabs.state.entityParent.name;
        confirmDialog.setState({
            entityType: entityType,
            entityGroupName: entityGroupName
        });
    },
    okUpdateGroupTemplateCallback: function(template) {
        this.refs.xmlTabs.saveGroupTemplate(template);
        this.refs.templateUpdateGroupModal.close();
    },
    cancelUpdateGroupTemplateCallback: function() {
        this.refs.templateUpdateGroupModal.close();
    },
    launchUpdateGroupTemplate: function(template){
        var self = this;
        this.refs.templateUpdateGroupModal.show("Confirm update",
            <ConfirmUpdateGroupDialog componentDidMountCallback={this.confirmUpdateGroupDidMount}
                                      resourceType="templates"
                                      template={template}/>, function() {
                                        self.okUpdateGroupTemplateCallback(template);
                                      });
    },
    okUpdateGroupMetaDataCallback: function(metaData) {
        this.refs.xmlTabs.saveGroupMetaData(metaData);
        this.refs.metaDataUpdateGroupModal.close();
    },
    cancelUpdateGroupMetaDataCallback: function() {
        this.refs.metaDataUpdateGroupModal.close();
    },
    launchUpdateGroupMetaData: function(metaData){
        var self = this;
        this.refs.metaDataUpdateGroupModal.show("Confirm meta data update",
            <ConfirmUpdateGroupDialog componentDidMountCallback={this.confirmUpdateGroupDidMount}
                                      resourceType="meta data"
                                      metaData={metaData}/>, function() {
                                        self.okUpdateGroupMetaDataCallback(metaData);
                                      });
    },
    updateExtPropsAttributesCallback: function(){
        var self = this;
        this.props.resourceService.getExternalProperties().then(function(response){
            var attributes = self.refs.resourceEditor.refs.resourceAttrPane.state.attributes;
            attributes["ext"] = response.applicationResponseContent;
            self.refs.resourceEditor.refs.resourceAttrPane.setState({attributes: attributes})
        });
    },
    verticalSplitterDidUpdateCallback: function() {
        if (this.refs.xmlTabs.refs.codeMirrorComponent !== undefined) {
            this.refs.xmlTabs.refs.codeMirrorComponent.resize();
        }

        if (this.refs.xmlTabs.refs.xmlPreview !== undefined) {
            this.refs.xmlTabs.refs.xmlPreview.resize();
        }

        var tabContentHeight = $(".horz-divider.rsplitter.childContainer.vert").height() - 20;
        $(".xml-editor-preview-tab-component").not(".content").css("cssText", "height:" + tabContentHeight + "px !important;");
    },
    createResourceCallback: function(data) {
        if (data && data.rtreeListMetaData.entity === "extProperties") {
            this.refs.selectTemplateFilesModalDlg.show();
        } else if (data) {
            this.refs.createResourceModalDlg.show();
        } else {
            this.refs.selectMetaDataAndTemplateFilesModalDlg.show();
        }
    },
    deleteResourceCallback: function() {
    if (this.refs.xmlTabs.state.entityType !== "extProperties") {
        this.refs.confirmDeleteResourceModalDlg.show();
    } else {
        this.refs.confirmDeletePropertyModalDlg.show();
    }
    },
    confirmDeleteResourceCallback: function() {
        var groupName;
        var webServerName;
        var jvmName;
        var webAppName;
        var node = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();

        if (this.refs.xmlTabs.state.entityType !== "extProperties") {
            if (node.rtreeListMetaData.entity === "webApps") {
                webAppName = node.name;
                if (node.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                    jvmName = node.rtreeListMetaData.parent.jvmName;
                } else {
                    groupName = node.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                }
            } else if (node.rtreeListMetaData.entity === "jvmSection") {
                groupName = node.rtreeListMetaData.parent.name;
                jvmName = "*";
            } else if (node.rtreeListMetaData.entity === "jvms") {
                jvmName = node.jvmName;
            } else if (node.rtreeListMetaData.entity === "webServerSection") {
                groupName = node.rtreeListMetaData.parent.name;
                webServerName = "*";
            } else if (node.rtreeListMetaData.entity === "webServers") {
                webServerName = node.name;
            }
             var self = this;
             this.refs.confirmDeleteResourceModalDlg.close();
        }
        else{
            var self = this;
            this.refs.confirmDeletePropertyModalDlg.close();
        }

        this.props.resourceService.deleteResources(this.refs.resourceEditor.refs.resourcePane.getCheckedItems(),
                                                  groupName, webServerName, jvmName, webAppName).then(function(response){
            self.refreshResourcePane();

            // clear the editor
            self.refs.xmlTabs.clearEditor();

            if (self.refs.xmlTabs.state.entityType === "extProperties"){
                self.updateExtPropsAttributesCallback();
            }
        }).caught(function(e){
            console.log(e);
            $.errorAlert("Error deleting resource template(s)!", "Error", true);
        });

    },

    // TODO: We need to refactor this. For starters we should put external properties in its own event handler.
    onCreateResourceOkClicked: function() {
        if (this.refs.selectMetaDataAndTemplateFilesWidget && !$(this.refs.selectMetaDataAndTemplateFilesWidget.refs.form.getDOMNode())) {
            return;
        }

        var isExtProperties = this.refs.xmlTabs.state.entityType === "extProperties";
        var metaDataFile,templateFile;

        if(!isExtProperties){
            if (this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataFile) {
                metaDataFile = this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataFile.getDOMNode().files[0];
                this.refs.selectMetaDataAndTemplateFilesWidget.setState({invalidMetaFile: !metaDataFile});
            }

            templateFile = this.refs.selectMetaDataAndTemplateFilesWidget.refs.templateFile.getDOMNode().files[0];
            if (templateFile === undefined) {
                this.refs.selectMetaDataAndTemplateFilesWidget.setState({invalidTemplateFile: true});
            }
        } else {
            templateFile = this.refs.selectTemplateFileWidget.refs.templateFile.getDOMNode().files[0];
            if (templateFile === undefined) {
                this.refs.selectTemplateFileWidget.setState({invalidTemplateFile: true});
            }
        }

        if ((isExtProperties && templateFile) || templateFile) {
            // Submit!
            var formData = new FormData();
            var self = this;

            if (metaDataFile) {
                formData.append("metaData", metaDataFile);
            } else if (!isExtProperties){
                formData.append("deployPath", this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataEntryForm.getDeployPath());
                formData.append("assignToJvms", this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataEntryForm.getAssignToJvms());
            }
            formData.append("templateFile", templateFile);

            var groupName;
            var webServerName;
            var jvmName;
            var webAppName;
            var node = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();

            if (node && !isExtProperties) {
                if (node.rtreeListMetaData.entity === "webApps") {
                    webAppName = node.name;
                    if (node.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                        jvmName = node.rtreeListMetaData.parent.jvmName;
                    } else {
                        groupName = node.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                    }
                } else if (node.rtreeListMetaData.entity === "jvmSection") {
                    groupName = node.rtreeListMetaData.parent.name;
                    jvmName = "*";
                } else if (node.rtreeListMetaData.entity === "jvms") {
                    groupName = node.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                    jvmName = node.jvmName;
                } else if (node.rtreeListMetaData.entity === "webServerSection") {
                    groupName = node.rtreeListMetaData.parent.name;
                    webServerName = "*";
                } else if (node.rtreeListMetaData.entity === "webServers") {
                    webServerName = node.name;
                }
            }

            var self = this;

            var deployFilename = null;
            if (this.refs.selectMetaDataAndTemplateFilesWidget && this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataEntryForm) { // user form was used
                deployFilename = this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataEntryForm.getDeployFilename()
            } else if (this.refs.selectMetaDataAndTemplateFilesWidget) { // upload meta data was used
                deployFilename = $(this.refs.selectMetaDataAndTemplateFilesWidget.refs.templateFile.getDOMNode()).val();
                deployFilename = deployFilename.substr(deployFilename.lastIndexOf("\\") + 1);
            } else {
                deployFilename = "ext.properties"
            }

            $("body").css("cursor", "progress");
            this.props.resourceService.createResource(groupName, webServerName, jvmName, webAppName, formData,
                metaDataFile, deployFilename).then(function(response){
                    if(!isExtProperties){
                        if (self.refs.selectMetaDataAndTemplateFilesModalDlg.isShown()) {
                            self.refs.selectMetaDataAndTemplateFilesModalDlg.close();
                        } else {
                            self.refs.createResourceModalDlg.close();
                        }
                    } else {
                        self.refs.selectTemplateFilesModalDlg.close();
                    }
                    self.refreshResourcePane();
                    if (isExtProperties){
                        self.updateExtPropsAttributesCallback();
                    }
            }).caught(function(response){
                console.log(response);
                var errMsg = "Unexpected error. Please check log for error details.";
                var responseJson = response.responseJSON
                if (responseJson) {
                    if (responseJson.message) {
                        errMsg = responseJson.message;
                    } else if (responseJson.applicationResponseContent) {
                        errMsg = responseJson.applicationResponseContent;
                    }
                }

                $.errorAlert("Error creating resource template! " + errMsg, "Error", true);
            }).lastly(function(){$("body").css("cursor", "default");});
        }
    },
    refreshResourcePane: function() {
        var data = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();
        if (data === null && this.refs.xmlTabs.state.entityType === "extProperties"){
            // External Properties was the last node selected
            var rtreeListMetaData = {
                entity: "extProperties",
                parent:{
                    name:"Ext Properties parent",
                    key:"extPropertiesParent"
                }
            };
            data = {
                rtreeListMetaData: rtreeListMetaData,
                name: "External Properties"
            };

        }
        this.refs.resourceEditor.refs.resourcePane.getData(data);
    },
    statics: {
        getEntityName: function(entity, type) {
            if (type === "jvms") {
                return entity.jvmName;
            }
            return entity.name;
        }
    }
})

var XmlTabs = React.createClass({
    getInitialState: function() {
        return {entityType: null, entity: null, entityParent: null, resourceTemplateName: null, template: "",
                metaData: "", lastSaved: "Template", entityGroupName: "", groupJvmEntityType: null, readOnly: false}
    },
    clearEditor: function() {
        this.setState({resourceTemplateName: null, template: ""});
    },
    render: function() {
        var codeMirrorComponent;
        var metaDataEditor;
        var metaDataPreview;
        var xmlPreview;

        if (this.state.resourceTemplateName === null) {
            codeMirrorComponent = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
            xmlPreview = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
            metaDataEditor = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
            metaDataPreview = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
        } else {
            codeMirrorComponent = <CodeMirrorComponent ref="codeMirrorComponent" content={this.state.template}
                                   className="xml-editor-container" saveCallback={this.saveResource}
                                   onChange={this.onChangeCallback} readOnly={this.state.readOnly} mode="xml"/>
            metaDataEditor = <CodeMirrorComponent ref="metaDataEditor" content={this.state.metaData}
                                className="xml-editor-container" saveCallback={this.saveResourceMetaData}
                                onChange={this.onChangeCallback} mode="application/ld+json" formatCallback={this.formatMetaDataCallback}/>
            if (this.state.entityType === "webServerSection" || this.state.entityType === "jvmSection") {
                xmlPreview = <div className="Resource preview msg">A group level web server or JVM template cannot be previewed. Please select a specific web server or JVM instead.</div>;
                metaDataPreview = <div className="Resource preview msg">A group level web server or JVM template cannot be previewed. Please select a specific web server or JVM instead.</div>;
            } else {
                if (this.state.metaData && this.state.entityType === "webApps" && !this.state.entityParent.jvmName) {
                    try {
                        var parsedMetaData = JSON.parse(this.state.metaData.replace(/\\/g, "\\\\"));
                        var deployToJvms = parsedMetaData.entity.deployToJvms;
                        if (deployToJvms !== undefined && (!deployToJvms || deployToJvms === "false")) {
                            xmlPreview = <XmlPreview ref="xmlPreview" mode="xml"/>
                            metaDataPreview = <MetaDataPreview ref="metaDataPreview" mode="application/ld+json"/>
                        } else {
                            xmlPreview = <div><div className="Resource preview msg resource-preview-br">{this.state.resourceTemplateName} cannot be previewed at this level since it is configured to be deployed to JVMs. </div><div className="Resource preview msg">To preview this template, select a JVM instance from the topology tree that is associated with this Web App and template.</div></div>;
                            metaDataPreview = <div><div className="Resource preview msg resource-preview-br">The meta data for {this.state.resourceTemplateName} cannot be previewed at this level since it is configured to be deployed to JVMs. </div><div className="Resource preview msg">To preview the meta data of this template, select a JVM instance from the topology tree that is associated with this Web App and meta data.</div></div>;
                        }
                    } catch (e) {
                        xmlPreview = <div className="Resource preview msg">Error parsing the meta data: unable to provide a preview until the meta data is corrected.</div>;
                        metaDataPreview = <div className="Resource preview msg">Error parsing the meta data: unable to provide a preview until the meta data is corrected.</div>;
                    }
                } else {
                    xmlPreview = <XmlPreview ref="xmlPreview" mode="xml"/>
                    metaDataPreview = <MetaDataPreview ref="metaDataPreview" mode="application/ld+json"/>
                }
            }
        }

        var xmlTabItems = [{title: "Template", content:codeMirrorComponent},
                            {title: "Template Preview", content:xmlPreview},
                            {title: "Meta Data", content: metaDataEditor},
                            {title: "Meta Data Preview", content: metaDataPreview}];

        if (this.state.entityType === "extProperties") {
            xmlTabItems = [{title: "Template", content:codeMirrorComponent},
                                                      {title: "Template Preview", content:xmlPreview}];
        }

        return <RTabs ref="tabs" items={xmlTabItems} depth={2} onSelectTab={this.onSelectTab}
                      className="xml-editor-preview-tab-component"
                      contentClassName="xml-editor-preview-tab-component content" />
    },
    formatMetaDataCallback: function() {
        try {
            var metaDataJson = JSON.parse(this.refs.metaDataEditor.getText().replace(/\\/g, "\\\\"));
            this.refs.metaDataEditor.codeMirror.setValue(JSON.stringify(metaDataJson, null, XmlTabs.SPACING_LEVEL));
        } catch(e) {
            console.log(e);
            $.errorAlert(e.message, "Error formatting meta data", false);
        }
    },
    componentWillUpdate: function(nextProps, nextState) {
        this.refs.tabs.setState({activeHash: "#/Configuration/Resources/" + this.state.lastSaved + "/",
            entityGroupName: nextState.entityParent ? nextState.entityParent.name : null});
    },
    onChangeCallback: function() {
        if ((this.refs.codeMirrorComponent !== undefined && this.refs.codeMirrorComponent.isContentChanged())
                || (this.refs.metaDataEditor !== undefined && this.refs.metaDataEditor.isContentChanged())) {
            MainArea.unsavedChanges = true;
        } else {
            MainArea.unsavedChanges = false;
        }
    },
    checkGroupJvmsAreStopped: function(groupName){
        return this.props.groupService.getAllGroupJvmsAreStopped(groupName);
    },
    checkGroupWebServersAreStopped: function(groupName){
        return this.props.groupService.getAllGroupWebServersAreStopped(groupName);
    },
    /*** Save and Deploy methods: Start ***/
    saveResource: function(template) {
        if (this.state.entityType !== "extProperties") {
            try {
                var parsedMetaData = JSON.parse(this.state.metaData.replace(/\\/g, "\\\\")); // escape any backslashes before parsing
                var deployToJvms = parsedMetaData.entity.deployToJvms;
                if (this.state.entityType === "jvmSection" || this.state.entityType === "webServerSection" || (this.state.entityType === "webApps" && this.state.entityParent.name === "Web Apps" && (deployToJvms === undefined || deployToJvms === "true" || deployToJvms === true))){
                    this.props.updateGroupTemplateCallback(template);
                } else {
                    this.saveResourcePromise(template).then(this.savedResourceCallback).caught(this.failed.bind(this, "Save Resource Template"));
                }
            } catch(e) {
                $.errorAlert("Unable to save changes until the meta data errors are fixed: " + e.message, "", false);
            }
        } else {
            this.saveResourcePromise(template).then(this.savedResourceCallback).caught(this.failed.bind(this, "Save External Properties"));
        }
    },
    saveResourcePromise: function(template) {
        var thePromise;
        console.log("saving...");
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            if (this.state.entityType === "jvms") {
                thePromise = this.props.jvmService.updateResourceTemplate(this.state.entity.jvmName,
                    this.state.resourceTemplateName, template);
            } else if (this.state.entityType === "webServers") {
                thePromise = this.props.wsService.updateResourceTemplate(this.state.entity.name,
                    this.state.resourceTemplateName, template);
            } else if (this.state.entityType === "webApps") {
                thePromise = this.props.webAppService.updateResourceTemplate(this.state.entity.name,
                    this.state.resourceTemplateName, template, this.state.entityParent.jvmName, this.state.entity.group.name);
            } else if (this.state.groupJvmEntityType && this.state.groupJvmEntityType === "webApp") {
                thePromise = this.props.groupService.updateGroupAppResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            }  else if (this.state.entityType === "webServerSection") {
                thePromise = this.props.groupService.updateGroupWebServerResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            } else if (this.state.entityType === "extProperties"){
                thePromise = this.props.resourceService.updateResourceContent(this.state.resourceTemplateName, template, null, null, null, null);
            } else {
                thePromise = this.props.groupService.updateGroupJvmResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            }
        }
        return thePromise;
    },
    saveGroupTemplate: function(template){
        var thePromise;
        var self = this;
        if (this.state.entityType === "webApps") {
            thePromise = this.props.groupService.updateGroupAppResourceTemplate(this.state.entity.group.name, this.state.entity.name, this.state.resourceTemplateName, template);
        }  else if (this.state.entityType === "webServerSection") {
            thePromise = this.props.groupService.updateGroupWebServerResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
        } else {
            thePromise = this.props.groupService.updateGroupJvmResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
        }
        thePromise.then(this.savedResourceCallback).caught(this.failed.bind(this, "Save Group Resource Template"));
    },
    savedResourceCallback: function(response) {
        if (response.message === "SUCCESS") {
            console.log("Save success!");
            MainArea.unsavedChanges = false;
            this.showFadingStatus("Saved", this.refs.codeMirrorComponent.getDOMNode());
            this.setState({template:response.applicationResponseContent, lastSaved: "Template"});
            if (this.state.entity === "extProperties"){
                this.props.updateExtPropsAttributesCallback();
            }
        } else {
            throw response;
        }
    },
    failed: function(title, response) {
        try {
            // Note: This will do for now. The problem is that the response's responseText is in HTML for save and
            //       JSON for deploy. We should standardize the responseText (e.g. JSON only) before we can modify
            //       this method to display the precise error message.
            var jsonResponseText = JSON.parse(response.responseText);
            var msg = jsonResponseText.applicationResponseContent === undefined ? "Operation was not successful!" :
                jsonResponseText.applicationResponseContent;
            $.errorAlert(msg, title, false);
        } catch (e) {
            console.log(response);
            console.log(e);
            $.errorAlert("Operation was not successful! Please check console logs for details.", title, false);
        }
    },
    saveResourceMetaData: function(metaData) {
        try {
            var parsedMetaData = JSON.parse(this.refs.metaDataEditor.getText().replace(/\\/g, "\\\\")); // escape any backslashes before parsing
            var deployToJvms = parsedMetaData.entity.deployToJvms;
            if (this.state.entityType === "jvmSection" || this.state.entityType === "webServerSection" || (this.state.entityType === "webApps" && this.state.entityParent.name === "Web Apps" && (deployToJvms === undefined || deployToJvms === "true" || deployToJvms === true))){
                this.props.updateGroupMetaDataCallback(metaData);
            } else {
                this.saveResourceMetaDataPromise(metaData).then(this.savedResourceMetaDataCallback).caught(this.failed.bind(this, "Save Resource Meta Data"));
            }
        } catch(e) {
            $.errorAlert("Unable to save changes until the meta data errors are fixed: " + e.message, "", false);
        }
    },
    saveResourceMetaDataPromise: function(metaData) {
        var thePromise;
        console.log("saving meta data...");
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            if (this.state.entityType === "jvms" || this.state.entityType === "webServers") {
                thePromise = this.props.resourceService.updateResourceMetaData(this.state.entity.jvmName, this.state.entity.name, this.state.entityGroupName, "",
                    this.state.resourceTemplateName, metaData);
            } else if (this.state.entityType === "webApps" && this.state.entityParent.jvmName) {
                thePromise = this.props.resourceService.updateResourceMetaData(this.state.entityParent.jvmName, "", this.state.entity.group.name, this.state.entity.name,
                    this.state.resourceTemplateName, metaData);
            } else if (this.state.entityType === "webApps") {
                thePromise = this.props.resourceService.updateResourceMetaData("", "", this.state.entity.group.name, this.state.entity.name,
                    this.state.resourceTemplateName, metaData);
            }  else if (this.state.entityType === "webServerSection") {
                thePromise = this.props.resourceService.updateResourceMetaData("", "*", this.state.entityGroupName, "",
                    this.state.resourceTemplateName, metaData);
            } else if (this.state.entityType === "extProperties"){
                thePromise = this.props.resourceService.updateResourceContent(this.state.resourceTemplateName, template, null, null, null, null);
            } else {
                thePromise = this.props.resourceService.updateResourceMetaData("*", "", this.state.entityGroupName, "",
                    this.state.resourceTemplateName, metaData);
            }
        }
        return thePromise;
    },
    savedResourceMetaDataCallback: function(response) {
        if (response.message === "SUCCESS") {
            console.log("Save meta data success!");
            MainArea.unsavedChanges = false;
            this.showFadingStatus("Saved", this.refs.metaDataEditor.getDOMNode());
            this.setState({metaData: response.applicationResponseContent, lastSaved: "Meta Data"});
            if (this.state.entity === "extProperties"){
                this.props.updateExtPropsAttributesCallback();
            }
        } else {
            throw response;
        }
    },
    saveGroupMetaData: function(metaData){
        var jvmName, webServerName, webAppName, groupName;
        var entityType = this.state.entityType;
        if (entityType === "jvmSection") {
            jvmName = "*";
            groupName = this.state.entityGroupName;
        } else if (entityType === "webServerSection") {
            webServerName = "*";
            groupName = this.state.entityGroupName;
        } else {
            webAppName = this.state.entity.name;
            groupName = this.state.entity.group.name;
        }

        var thePromise = this.props.resourceService.updateResourceMetaData(jvmName, webServerName, groupName, webAppName, this.state.resourceTemplateName, metaData);
        thePromise.then(this.savedMetaDataCallback).caught(this.failed.bind(this, "Save Group Resource Meta Data"));
    },
    savedMetaDataCallback: function(response) {
        if (response.message === "SUCCESS") {
            console.log("Save success!");
            MainArea.unsavedChanges = false;
            this.showFadingStatus("Saved", this.refs.metaDataEditor.getDOMNode());
            this.setState({metaData:response.applicationResponseContent, lastSaved: "Meta Data"});
        } else {
            throw response;
        }
    },
    /*** Save and Deploy methods: End ***/

    reloadTemplate: function(data, resourceName, groupJvmEntityType) {
        var entityType = this.state.entityType;
        if (entityType !== null && resourceName !== null) {
            if (entityType === "jvms") {
                this.getResourceContent(data, resourceName, null, null, data.jvmName);
            } else if (entityType === "webServers") {
                this.getResourceContent(data, resourceName, null, data.name);
            } else if (entityType === "webApps" && this.state.entityParent.jvmName) {
                this.getResourceContent(data, resourceName, null, null, this.state.entityParent.jvmName, data.name);
            } else if (entityType === "webApps" && this.state.entityParent.rtreeListMetaData.parent.name) {
                this.getResourceContent(data, resourceName, this.state.entity.group.name, null, null, data.name);
            } else if (entityType === "webServerSection") {
                this.getResourceContent(data, resourceName, this.state.entityGroupName, "*");
            } else if (entityType === "jvmSection") {
                this.getResourceContent(data, resourceName, this.state.entityGroupName, null, "*");
            } else if (entityType === "extProperties") {
                this.getResourceContent(data, resourceName, null, null, null, null);
            }
        } else {
            this.setState({entityType: entityType, entity: null, entityParent: null, resourceTemplateName: null,
                           template: ""});
        }
    },
    getResourceContent: function(data, resourceName, groupName, webServerName, jvmName, appName) {
        var self = this;
        ServiceFactory.getResourceService().getResourceContent(resourceName, groupName, webServerName, jvmName, appName)
        .then(function(response){
            var metaData = response.applicationResponseContent.metaData;
            var readOnly = false;
            if (metaData) {
                try {
                    var jsonMetaData = JSON.parse(metaData.replace(/\\/g, "\\\\"));
                    if (jsonMetaData.contentType !== "text/plain" && jsonMetaData.contentType !== "application/xml") {
                        readOnly = true;
                    }
                } catch(e) {
                    // TODO fail silently? -- resource generation or preview will throw the error for now
                }
            }

            self.setState({entity: data,
                           resourceTemplateName: resourceName,
                           template: response.applicationResponseContent.content,
                           metaData: metaData,
                           entityGroupName: self.state.entityGroupName,
                           readOnly: readOnly});

        }).caught(function(response) {
            $.errorAlert("Error loading template!", "Error");
        });
    },
    onSelectTab: function(index) {
        var self = this;
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            // keep the tab open for template and meta data
            if (index === 0) {
                this.state.lastSaved = "Template";
            } else if (index === 2) {
                this.state.lastSaved = "Meta Data";
            }

            // show the preview
            if (index === 1 ) {
                if (this.state.entityType === "jvms") {
                    this.props.jvmService.previewResourceFile(this.state.resourceTemplateName,
                                                              this.state.entity.jvmName,
                                                              this.state.entityParent.rtreeListMetaData.parent.name,
                                                              this.refs.codeMirrorComponent.getText(),
                                                              this.previewSuccessCallback,
                                                              this.previewErrorCallback);
                } else if (this.state.entityType === "webServers") {
                    this.props.wsService.previewResourceFile(this.state.resourceTemplateName,
                                                             this.state.entity.name,
                                                             this.state.entityParent.rtreeListMetaData.parent.name,
                                                             this.refs.codeMirrorComponent.getText(),
                                                             this.previewSuccessCallback,
                                                             this.previewErrorCallback);
                } else if (this.state.entityType === "webApps" && this.state.entityParent.jvmName) {
                        this.props.webAppService.previewResourceFile(this.state.resourceTemplateName,
                                                                 this.state.entity.name,
                                                                 this.state.entity.group.name,
                                                                 this.state.entityParent.jvmName ? this.state.entityParent.jvmName : "",
                                                                 this.refs.codeMirrorComponent.getText(),
                                                                 this.previewSuccessCallback,
                                                                 this.previewErrorCallback);
                }  else if (this.state.entityType === "webApps" && !this.state.entityParent.jvmName) {
                    try {
                        var parsedMetaData = JSON.parse(this.refs.metaDataEditor.getText().replace(/\\/g, "\\\\"));
                        var deployToJvms = parsedMetaData.entity.deployToJvms;
                        if (deployToJvms !== undefined && (!deployToJvms || deployToJvms === "false")){
                            this.props.groupService.previewGroupAppResourceFile( this.state.entity.group.name,
                                                                                 this.state.resourceTemplateName,
                                                                                 this.state.entity.name,
                                                                                 this.refs.codeMirrorComponent.getText(),
                                                                                 this.previewSuccessCallback,
                                                                                 this.previewErrorCallback);
                        }
                    } catch (e) {
                        $.errorAlert("Unable to preview template until the meta data errors are fixed: " + e.message, "", false);
                        this.setState({metaData: this.refs.metaDataEditor.getText()});
                    }
                } else if (this.state.entityType === "extProperties"){
                    this.props.resourceService.previewResourceFile(this.state.resourceTemplateName,
                                                                   this.refs.codeMirrorComponent.getText(),
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   this.previewSuccessCallback,
                                                                   this.previewErrorCallback);


                }
            } else if (index === 3) {
                if (this.state.entityType === "jvms" || this.state.entityType === "webServers") {
                    this.props.resourceService.previewResourceFile(this.state.resourceTemplateName,
                                                                   this.refs.metaDataEditor.getText(),
                                                                   this.state.entityParent.rtreeListMetaData.parent.name,
                                                                   this.state.entity.name,
                                                                   this.state.entity.jvmName,
                                                                   "",
                                                                   this.previewMetaDataSuccessCallback,
                                                                   this.previewMetaDataErrorCallback);
                } else if (this.state.entityType === "webApps" && this.state.entityParent.jvmName) {
                    this.props.resourceService.previewResourceFile(this.state.resourceTemplateName,
                                                                   this.refs.metaDataEditor.getText(),
                                                                   this.state.entityParent.rtreeListMetaData.parent.name,
                                                                   "",
                                                                   this.state.entityParent.jvmName,
                                                                   this.state.entity.name,
                                                                   this.previewMetaDataSuccessCallback,
                                                                   this.previewMetaDataErrorCallback);
                } else if (this.state.entityType === "webApps") {
                    try {
                        var parsedMetaData = JSON.parse(this.refs.metaDataEditor.getText().replace(/\\/g, "\\\\"));
                        var deployToJvms = parsedMetaData.entity.deployToJvms;
                        if (deployToJvms !== undefined && (!deployToJvms || deployToJvms === "false")){
                            this.props.resourceService.previewResourceFile(this.state.resourceTemplateName,
                                                                       this.refs.metaDataEditor.getText(),
                                                                       this.state.entityParent.rtreeListMetaData.parent.name,
                                                                       "",
                                                                       "",
                                                                       this.state.entity.name,
                                                                       this.previewMetaDataSuccessCallback,
                                                                       this.previewMetaDataErrorCallback);
                       }
                   } catch (e) {
                        $.errorAlert("Unable to preview changes until the meta data errors are fixed: " + e.message, "", false);
                        this.setState({metaData: this.refs.metaDataEditor.getText()});
                   }
                }
            }
        }
    },
    previewSuccessCallback: function(response) {
        this.refs.xmlPreview.refresh(response.applicationResponseContent);
    },
    previewErrorCallback: function(errMsg) {
        this.refs.tabs.setState({activeHash: "#/Configuration/Resources/Template/"});
        $.errorAlert(errMsg, "Error");
    },
    previewMetaDataSuccessCallback: function(response) {
        this.refs.metaDataPreview.refresh(response.applicationResponseContent);
    },
    previewMetaDataErrorCallback: function(errMsg) {
        this.refs.tabs.setState({activeHash: "#/Configuration/Resources/Meta Data/"});
        $.errorAlert(errMsg, "Error");
    },
    /**
     * Uses jquery to take advantage of the fade out effect and to reuse the old code...for now.
     */
    showFadingStatus: function(msg, containerDom) {
        var toolTipId = "saveXmlBtn";
        if (msg !== undefined && $("#" + toolTipId).length === 0) {
            var top = $(containerDom).position().top + 10;
            var left = $(containerDom).position().left + 10;
            $(containerDom).parent().append("<div id='" + toolTipId +
                "' role='tooltip' class='ui-tooltip ui-widget ui-corner-all ui-widget-content' " +
                "style='top:" + top + "px;left:" + left + "px'>" + msg + "</div>");

            $("#" + toolTipId).fadeOut(3000, function() {
                $("#" + toolTipId).remove();
            });

        }
    },
    statics: {
        SPACING_LEVEL: 2
    }
});

var TemplateUploadForm = React.createClass({
    getInitialState: function(){
        return {
            fileName: "",
            entityName: "",
            entityGroupName: ""
        };
    },
    render: function() {
        var entityLabel = "JVM";
        if ("webApps" === this.state.entityType){
            entityLabel = "App";
        } else if ("webServers" === this.state.entityType) {
            entityLabel = "Web Server";
        } else if ("jvmSection" === this.state.entityType) {
            entityLabel = this.state.entityGroupName;
        } else if ("webServerSection" === this.state.entityType){
            entityLabel = this.state.entityGroupName;
        }
        return <div className={this.props.className}>
                 <form ref="templateForm" className="template-upload-form">
                    <div> {entityLabel}: {this.state.entityName} </div>
                    <div>
                        <label> Please select a template file (*.tpl) </label>
                    </div>
                    <div>
                        <label htmlFor="templateFile" className="error"></label>
                    </div>
                    <div>
                        <input type="file" name="templateFile" ref="templateFile"></input>
                    </div>
                 </form>
               </div>

    },
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({
                                                                            rules: {"templateFile": {
                                                                                        required: true
                                                                                        }
                                                                                    },
                                                                            messages: {
                                                                                "templateFile": {
                                                                                    required: "Please select a file for upload"
                                                                                 },
                                                                            }
                                                                    });
        $(this.refs.templateFile.getDOMNode()).focus();
        $(this.refs.templateForm.getDOMNode()).submit(function(e) {
            e.preventDefault();
        });
        this.props.componentDidMountCallback(this);
        var self = this;
        var templateValidator = this.validator;
        this.refs.templateFile.getDOMNode().onchange = function() {
            if (templateValidator) {
                templateValidator.form();
            }
        };
    },
    isValid: function() {
       this.validator.form();
       if (this.validator.numberOfInvalids() === 0) {
           return true;
       }
       return false;
    },
    validator: null
});

var ConfirmUpdateGroupDialog = React.createClass({
    getInitialState: function(){
        return {
            entityType: "",
            entityGroupName: ""
        };
    },
    render: function() {
        var entityType = "JVM";
        var resourceType = this.props.resourceType;
        if (this.state.entityType === "webServerSection"){
            entityType = "Web Server"
        }
        return <div className={this.props.className}>
                 Saving will overwrite all the {entityType} {resourceType} in the group
                 <br/>
                 {this.state.entityGroupName}.
                 <br/><br/>
                 Do you wish to continue?
               </div>
    },
    componentDidMount: function(){
        this.props.componentDidMountCallback(this);
    }
});

/**
 * Lets user select the meta data and template file used to create a resource.
 */
var SelectMetaDataAndTemplateFilesWidget = React.createClass({
    getInitialState: function() {
        // Let's not use jQuery form validation since we only need to check if the user has chosen files to use in creating the resource.
        // Besides, this is doing it the React way. :)
        return {uploadMetaData: this.props.uploadMetaData};
    },
    render: function() {
        var selectedEntity = this.props.parent.refs.resourceEditor.refs.treeList.getSelectedNodeData();

        var groupLevelWebApp = false;
        if (selectedEntity.rtreeListMetaData.entity === "webApps" &&
            selectedEntity.rtreeListMetaData.parent.rtreeListMetaData.entity === "webAppSection") {
            groupLevelWebApp = true;
        }

        var metaDataEntryComponent = this.state.uploadMetaData ? <div>
                                                                     <label>*Meta Data File</label>
                                                                     <label ref="metaDataFileErrorLabel" htmlFor="metaDataFile" className="error"/>
                                                                     <div className="file-input-container">
                                                                         <input type="file" ref="metaDataFile" name="metaDataFile" required accept=".json" onChange={this.onMetaDataFileChange}/>
                                                                     </div>
                                                                 </div>
                                                               : <MetaDataEntryForm ref="metaDataEntryForm" groupLevelWebApp={groupLevelWebApp}/>
        var uploadMetaDataCheckbox = null;
        if (!this.props.hideUploadMetaDataOption) {
            uploadMetaDataCheckbox = this.state.uploadMetaData ?
                <input type="checkbox" onChange={this.onUploadMetaDataCheckboxChange} checked>Upload Meta Data File</input> :
                <input type="checkbox" onChange={this.onUploadMetaDataCheckboxChange}>Upload Meta Data File</input>;
        }

        return <div className="select-meta-data-and-template-files-widget">
                   {uploadMetaDataCheckbox}
                   <form ref="form" className="testForm">
                       {metaDataEntryComponent}
                       <label>*Resource Template File</label>
                       <label ref="templateFileErrorLabel" htmlFor="templateFile" className="error"/>
                       <div>
                           <input ref="templateFile" name="templateFile" type="file" required onChange={this.onTemplateFileChange}/>
                       </div>
                   </form>
               </div>
    },
    onUploadMetaDataCheckboxChange: function() {
        this.setState({uploadMetaData: !this.state.uploadMetaData});
    },
    componentDidMount: function() {
        $(this.refs.form.getDOMNode()).submit(function(e) {
                                                  console.log("Submit!");
                                                  e.preventDefault();
                                              });
    },
    onMetaDataFileChange: function(e) {
        // This is necessary since jquery validate does not clear the error
        // after user specifies a file not unless user clicks on something
        // Note: We are not modifying the DOM here in such a way that will affect React
        if ($(this.refs.metaDataFileErrorLabel.getDOMNode()).hasClass("error")) {
            $(this.refs.metaDataFileErrorLabel.getDOMNode()).removeClass("error");
            $(this.refs.metaDataFileErrorLabel.getDOMNode()).html("");
            $(this.refs.metaDataFile.getDOMNode()).removeClass("error");
        }
    },
    onTemplateFileChange: function(e) {
        // This is necessary since jquery validate does not clear the error
        // after user specifies a file not unless user clicks on something
        // Note: We are not modifying the DOM here in such a way that will affect React
        if ($(this.refs.templateFileErrorLabel.getDOMNode()).hasClass("error")) {
            $(this.refs.templateFileErrorLabel.getDOMNode()).removeClass("error");
            $(this.refs.templateFileErrorLabel.getDOMNode()).html("");
            $(this.refs.templateFile.getDOMNode()).removeClass("error");
        }
    }
});

var MetaDataEntryForm = React.createClass({
    getInitialState: function() {
        return {deployPath: null, deployFilename: null, assignToJvms: false};
    },
    render: function() {
        var assignToJvmsCheckbox = null;
        if (this.props.groupLevelWebApp) {
            assignToJvmsCheckbox = this.state.assignToJvms === false ?
                <input ref="assignToJvms" name="assignToJvms" type="checkbox" onChange={this.onChangeAssignToJvms}>Assign to JVMs</input> :
                <input ref="assignToJvms" name="assignToJvms" type="checkbox" checked onChange={this.onChangeAssignToJvms}>Assign to JVMs</input>;
        }

        return <div className="MetaDataEntryForm">
                   <label>*Deploy Name</label>
                   <label htmlFor="deployFilename" className="error"/>
                   <input ref="deployFilename" name="deployFilename" type="text" required valueLink={this.linkState("deployFilename")}/>
                   <label>Deploy Path</label>
                   <input ref="deployPath" type="text"  valueLink={this.linkState("deployPath")}/>
                   {assignToJvmsCheckbox}
               </div>
    },
    mixins: [React.addons.LinkedStateMixin],
    componentDidMount: function() {
        this.refs.deployFilename.getDOMNode().focus();
    },
    getDeployPath: function() {
        return this.state.deployPath;
    },
    getAssignToJvms: function() {
        return this.state.assignToJvms;
    },
    getDeployFilename: function() {
        return this.state.deployFilename;
    },
    onChangeAssignToJvms: function() {
        this.setState({assignToJvms: !this.state.assignToJvms});
    }
})

var SelectTemplateFilesWidget = React.createClass({
    getInitialState: function() {
        // Let's not use jQuery form validation since we only need to check if the user has chosen files to use in creating the resource.
        // Besides, this is doing it the React way. :)
        return {invalidTemplateFile: false};
    },
    render: function() {
        var warningDisplay = null;
        if (this.props.getResourceOptions().length > 0) {
            warningDisplay = <div className="Warning">
                                 <span className="icon"/>
                                 <span className="msg">Only one external properties file can be uploaded. Any existing ones will be overwritten.</span>
                             </div>;
        }
        return <div className="select-meta-data-and-template-files-widget">
                   <form ref="form">
                       {warningDisplay}
                       <div className={(!this.state.invalidTemplateFile ? "hide " : "") + "error"}>Please select a file</div>
                       <div>
                           <input type="file" ref="templateFile" onChange={this.onTemplateFileChange} name="templateFile">External Properties</input>
                       </div>
                   </form>
               </div>
    },
    componentDidMount: function() {
        $(this.refs.form.getDOMNode()).submit(function(e) {
                                                  console.log("Submit!");
                                                  e.preventDefault();
                                              });
    },
    onTemplateFileChange: function(e) {
        if(this.refs.templateFile.getDOMNode().files[0]) {
            this.setState({invalidTemplateFile: false});
        }
    }
});
