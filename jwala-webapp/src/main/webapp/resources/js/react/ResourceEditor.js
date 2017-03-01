/** @jsx React.DOM */

/**
 * The component that lets user edit resource files.
 *
 * TODO: Unit tests.
 */
var ResourceEditor = React.createClass({
    getInitialState: function() {
        return {
            groupData: null,
            resourceAttrData: null,
            entity: null
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading data...</div>
        }

        var treeMetaData = {entity: "groups",
                            propKey: "name",
                            icon: "public-resources/img/icons/group.png",
                            children:[{entity: "webServerSection", propKey: "key" , label: "name", icon: "public-resources/img/icons/webserver.png", selectable: true,
                                       children:[{entity: "webServers", propKey: "name", selectable: true}]},
                                      {entity: "jvmSection", propKey: "key", label: "name", icon: "public-resources/img/icons/appserver.png", selectable: true,
                                       children:[{entity: "jvms", propKey: "jvmName", selectable: true,
                                                  children:[{entity: "webApps", propKey: "name", selectable: true}]}]},
                                       {entity: "webAppSection", propKey: "key", label: "name", icon: "public-resources/img/icons/webapp.png", selectable: false,
                                        children:[{entity: "webApps", propKey: "name", selectable: true}]}]
                            };

        var groupJvmTreeList = <RStaticDialog key="groupsDlg"  ref="groupsDlg" title="Topology" defaultContentHeight="283px">
                                   <ExternalPropertiesNode ref="externalPropertiesNode" onClick={this.handleExtPropertiesClick}/>
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectTreeNodeCallback}
                                              collapsedByDefault={true}/>
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog key="resourceFileDlg" ref="resourceFileDlg" title="Resources" defaultContentHeight="283px">
                                <ResourcePane ref="resourcePane"
                                              jvmService={this.props.jvmService}
                                              wsService={this.props.wsService}
                                              webAppService={this.props.webAppService}
                                              groupService={this.props.groupService}
                                              resourceService={this.props.resourceService}
                                              selectCallback={this.selectResourceCallback}
                                              createResourceCallback={this.props.createResourceCallback}
                                              deleteResourceCallback={this.props.deleteResourceCallback}/>
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog key="resourceAttrPane" ref="resourceAttrDlg" title="Properties and Values" defaultContentHeight="283px">
                                   <ResourceAttrPane ref="resourceAttrPane" />
                               </RStaticDialog>

        var splitterComponents = [];
        splitterComponents.push(groupJvmTreeList);
        splitterComponents.push(resourcesPane);
        splitterComponents.push(resourceAttrPane);

        return <RSplitter ref="mainSplitter"
                          components={splitterComponents}
                          orientation={RSplitter.HORIZONTAL_ORIENTATION}
                          onSplitterChange={this.onChildSplitterChangeCallback}
                          panelDimensions={[{width:"44%", height:"100%"},
                                            {width:"12%", height:"100%"},
                                            {width:"44%", height:"100%"}]} />
    },
    componentDidMount: function() {
        var self = this;
        this.props.resourceService.getResourceTopology().then(function(response){
            self.setGroupData(response.applicationResponseContent.groups);
        });
    },
    setGroupData: function(groupData) {
        // Transform group data to contain a jvm and a web server section so that jvms and web server data will show up
        // under the said sections.
        if (groupData !== null) {
            var groupDataClone = $.extend(true, [], groupData); // it's good practice not to mutate the source
            groupDataClone.forEach(function(theGroupData) {
                theGroupData["jvmSection"] = [{key: theGroupData.name + "JVMs", name: "JVMs", jvms: theGroupData.jvms}];
                theGroupData["webServerSection"] = [{key: theGroupData.name + "WebServers", name: "Web Servers",
                    webServers: theGroupData.webServers}];
                theGroupData["webAppSection"] = [{key: theGroupData.name + "WebApps", name: "Web Apps", webApps: theGroupData.applications}];
            });

            this.setState({groupData:groupDataClone});
        } else {
            this.setState({groupData:{}});
        }
    },
    selectTreeNodeCallback: function(data, entity, parent) {
        this.refs.externalPropertiesNode.setActive(false);
        return this.selectNodeCallback(data, entity, parent);
    },
    selectNodeCallback: function(data, entity, parent) {
         // setState was not used so that the currently selected attribute will be displayed in the attributes tree
         // setState triggers a re-rendering of the resource editor which resets the attribute tree to the default
         // state thus the selected entity attribute will not be displayed (which we don't want)
        this.state.entity = entity;

        if (this.props.selectEntityCallback(data, entity, parent)) {
            this.refs.resourcePane.getData(data);
            this.refs.resourceAttrPane.setCurrentlySelectedEntityData(data, entity, parent);
            return true;
        }
        this.refs.resourcePane.getData(null);
        this.refs.resourceAttrPane.setCurrentlySelectedEntityData(null, null, null);
        return false;
    },
    selectResourceCallback: function(value, groupJvmEntityType) {
        // extProperties isn't part of the resources tree, so we have to fake that it was selected since it won't ever be returned in getSelectedNodeData
        return this.props.selectResourceTemplateCallback(this.state.entity === "extProperties" ? this.state.entity : this.refs.treeList.getSelectedNodeData(), value, groupJvmEntityType);
    },
    onParentSplitterChange: function(dimensions) {
        this.refs.groupsDlg.recomputeContentContainerSize(dimensions[0]);
        this.refs.resourceFileDlg.recomputeContentContainerSize(dimensions[0]);
        this.refs.resourceAttrDlg.recomputeContentContainerSize(dimensions[0]);
    },
    onChildSplitterChangeCallback: function(dimensions) {
        if (dimensions[0]) {
            this.refs.groupsDlg.recomputeContentContainerSize(dimensions[0]);
        }

        if (dimensions[1]) {
            this.refs.resourceFileDlg.recomputeContentContainerSize(dimensions[1]);
        }

        if (dimensions[2]) {
            this.refs.resourceAttrDlg.recomputeContentContainerSize(dimensions[2]);
        }
    },
    handleExtPropertiesClick: function() {
        // leverage the resources API's by faking out the ext properties tree node
        var entity="extProperties"
        var rtreeListMetaData = {
            entity: entity,
            parent:{
                name:"Ext Properties parent",
                key:"extPropertiesParent"
            }
        };
        var data = {
            rtreeListMetaData: rtreeListMetaData,
            name: "External Properties"
        };
        var parent = {
            key:"extPropertiesParent",
            name:"Ext Properties parent",
            rtreeListMetaData: rtreeListMetaData
        };

        // Clear any selected node in the tree list since the external properties node has been selected at this point
        this.refs.treeList.setSelectedNode(null);

        this.selectNodeCallback(data, entity, parent);
    }
});

/**
 * A component specifically made to display an "External Properties" node.
 */
var ExternalPropertiesNode = React.createClass({
    getInitialState: function() {
        return {focus: false, active: false}
    },
    render: function() {
        var spanClassName = this.state.active ? "ui-state-active" : "";

        if (!this.state.active) {
            spanClassName = this.state.focus ? "ui-state-focus" : "";
        }

        return <div className="root-node-ul rtree-list-item ext-properties-container"
                    onClick={this.onClick} onMouseEnter={this.onMouseEnter} onMouseOut={this.onMouseOut}>
                    <img src="public-resources/img/icons/props.png"/>
                    <span className={spanClassName}>Ext Properties</span>
               </div>
    },
    onClick: function() {
        this.setState({active: true}    );
        this.props.onClick();
    },
    onMouseEnter: function() {
        this.setState({focus: true});
    },
    onMouseOut: function() {
        this.setState({focus: false});
    },
    setActive: function(val) {
        this.setState({active: val});
    }
});