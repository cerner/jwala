/** @jsx React.DOM */

/**
 * Shows the properties and values of a JVM, Web Server or Web Application.
 *
 * TODO: Unit tests.
 */
var ResourceAttrPane = React.createClass({
    getInitialState: function() {
        return {attributes: null};
    },

    render: function() {
        var attributes = this.state.attributes;
        if (attributes === null) {
            return <div>Loading attributes...</div>;
        }
        var reactAttributeElements = [];
        for (var key in attributes) {
            reactAttributeElements.push(<Attribute entity={key} value={attributes[key]}/>);
        }
        return <RJsonDataTreeDisplay ref="attrTree" data={attributes} title="Data Tree" onShowToolTipCallback={this.onShowAttrTreeToolTipCallback}/>
    },
    onShowAttrTreeToolTipCallback: function(hierarchy) {
        return <ResourceAttrPaneCopyPropValComponent hierarchy={hierarchy} />;
    },
    componentDidMount: function() {
        // TODO: Decide whether we should call the service directly or pass it as a property.
        var self = this;
        var attributes;
        ServiceFactory.getResourceService().getResourceAttrData()
        .then(function(response) {
            attributes = response.applicationResponseContent;
            return ServiceFactory.getAdminService().viewProperties();
        }).then(function(response){
            attributes["vars"] = response.applicationResponseContent;
            self.setState({attributes: attributes})
            return ServiceFactory.getResourceService().getExternalProperties();
        }).then(function(response){
            attributes["ext"] = response.applicationResponseContent;
            self.setState({attributes: attributes})
        }).caught(function(e){
            alert(e);
        });
    },
    setCurrentlySelectedEntityData: function(data, entityName, parent) {
        if (this.state.attributes) {
            var newAttributes = {}; // we need this since we're mutating data
            for (key in this.state.attributes) {
                newAttributes[key] = this.state.attributes[key];
            }

            var newEntityName;

            // we need to clone data since we need to mutate it (e.g. add parent data)
            var newData = ResourceAttrPane.sanitizeAndCloneData(data);
            switch (entityName) {
                case "jvmSection":
                    newAttributes["jvms"] = newData.jvms;
                    newAttributes["jvms"].forEach(function(item) {
                        item["parentGroup"] = ResourceAttrPane.sanitizeAndCloneData(parent);
                    });
                    break;
                case "webServerSection":
                    newAttributes["webServers"] = newData.webServers;
                    newAttributes["webServers"].forEach(function(item) {
                        item["parentGroup"] = ResourceAttrPane.sanitizeAndCloneData(parent);
                    });
                    break;
                case "webAppSection":
                    newAttributes["webApps"] = newData.webApps;
                    newAttributes["webApps"].forEach(function(item) {
                        item["parentGroup"] = ResourceAttrPane.sanitizeAndCloneData(parent);
                    });
                    break;
                case "webApps":
                     newData["parentJvm"] = ResourceAttrPane.sanitizeAndCloneData(parent);
                     newAttributes["webApp"] = newData;
                    break;
                case "jvms":
                     newData["parentGroup"] = ResourceAttrPane.sanitizeAndCloneData(parent.rtreeListMetaData.parent);
                     newAttributes["jvm"] = newData;
                    break;
                case "webServers":
                     newData["parentGroup"] = ResourceAttrPane.sanitizeAndCloneData(parent.rtreeListMetaData.parent);
                     newAttributes["webServer"] = newData;
            }

            this.refs.attrTree.refresh(newAttributes);
        }
    },
    statics : {
        sanitizeAndCloneData: function(data) {
            var newData = {};
            for (var key in data) {
                // Note: We wouldn't be filtering the key if the RTreeList didn't mutate the topology data!
                // TODO: Remove this if statement when RTreeList has been refactored not to mutate data passed to it.
                if (key !== "rtreeListMetaData" && key !== "jvmSection" && key !== "webServerSection" && key !== "webAppSection") {
                    newData[key] = data[key];
                }
            }
            return newData;
        }
    }
})

var Attribute = React.createClass({
    render: function() {
        if (this.props.property === undefined) {
            return <tr><td>{"+" + this.props.entity}</td><td>{"Array[" + this.props.value.length + "]"} </td></tr>;
        }
        return <tr><td>{"${" + this.props.entity + "." + this.props.property + "}"}</td><td>{this.props.value.toString()}</td></tr>;
    }
});

var WebServerTable = React.createClass({
    getInitialState: function() {
        return {isCollapsed: true};
    },
    render: function() {
        if (this.state.isCollapsed) {
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${webServers}"}</div>
        }

        var reactAttributeElements = [];
        for (var i = 0; i < this.props.attributes.length; i++) {
            for (attr in this.props.attributes[i]) {
                if (typeof(this.props.attributes[i][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "webServers[" + i + "]", key: attr + i, property: attr,
                                                         value: this.props.attributes[i][attr]}));
                }
            }
        }

        return <div className="ws-table-container">
                   <div style={{cursor: "pointer"}} onClick={this.onClick}>{"- ${webServers}"}</div>
                   <table className="ws-table">
                      <tbody>
                          {reactAttributeElements}
                      </tbody>
                  </table>
               </div>

    },
    onClick: function() {
        this.setState({isCollapsed: !this.state.isCollapsed});
    }
});

var JvmTable = React.createClass({
    getInitialState: function() {
        return {isCollapsed: true};
    },
    render: function() {
        if (this.state.isCollapsed) {
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${jvms}"}</div>
        }

        var reactAttributeElements = [];
        for (var i = 0; i < this.props.attributes.length; i++) {
            for (attr in this.props.attributes[i]) {
                if (typeof(this.props.attributes[i][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "jvms[" + i + "]", key: attr + i, property: attr,
                                                         value: this.props.attributes[i][attr]}));
                }
            }
        }

        return <div className="jvm-table-container">
                   <div style={{cursor: "pointer"}} onClick={this.onClick}>{"- ${jvms}"}</div>
                   <table className="jvm-table">
                      <tbody>
                          {reactAttributeElements}
                      </tbody>
                  </table>
               </div>

    },
    onClick: function() {
        this.setState({isCollapsed: !this.state.isCollapsed});
    }
});

var WebAppTable = React.createClass({
    getInitialState: function() {
        return {isCollapsed: true};
    },
    render: function() {
        if (this.state.isCollapsed) {
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${webApps}"}</div>
        }

        var reactAttributeElements = [];

        var webAppIdx = 0;
        for (key in this.props.attributes) {
            for (attr in this.props.attributes[key]) {
                if (typeof(this.props.attributes[key][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "webApps[" + webAppIdx + "]", key: attr + webAppIdx, property: attr, value: this.props.attributes[key][attr]}));
                }
            }
            webAppIdx++;
        }

        return <div className="webapp-table-container">
                   <div style={{cursor: "pointer"}} onClick={this.onClick}>{"- ${webApps}"}</div>
                   <table className="webapp-table">
                       <tbody>
                           {reactAttributeElements}
                       </tbody>
                   </table>
               </div>

    },
    onClick: function() {
        this.setState({isCollapsed: !this.state.isCollapsed});
    }
});

ResourceAttrPaneCopyPropValComponent = React.createClass({
    getInitialState: function() {
        return {copyIconHover: false, showTextCopiedMsg: false};
    },
    render: function() {
        var className = "copyPropValBtn ui-state-default ui-corner-all" + (this.state.copyIconHover ? " ui-state-hover" : "");
        var propsHierarchy = this.props.hierarchy;
        if (propsHierarchy.indexOf("vars") === 0){
            propsHierarchy = "vars['" + propsHierarchy.substring("vars".length + 1) + "']";
        } else if (propsHierarchy.indexOf("ext") === 0){
            propsHierarchy = "ext['" + propsHierarchy.substring("ext".length + 1) + "']";
        }
        return <div className="ResourceAttrPaneCopyPropValComponent">
                   <div style={!this.state.showTextCopiedMsg ? {display: "none"} : {}} ref="textCopiedMsg"
                        className="ui-tooltip ui-widget ui-widget-content">Text copied....</div>
                   <button className={className} onClick={this.onClick}>
                       <span style={{display: "inline-block"}} className="ui-icon ui-icon-clipboard" onMouseEnter={this.onMouseEnter}
                             onMouseOut={this.onMouseOut} title="copy" />
                   </button>
                   <span className="propValStyle">{"${" + propsHierarchy + "}"}</span>
                   <div style={{position: "fixed", top: -9999, left: -9999}}>
                       <textarea ref="textArea" />
                   </div>
               </div>;
    },
    onMouseEnter: function() {
        this.setState({copyIconHover: true});
    },
    onMouseOut: function() {
        this.setState({copyIconHover: false});
    },
    onClick: function() {
        var self = this;

        if (this.props.hierarchy.indexOf("vars") === 0) {
            // Property keys with '.' should be written as vars['some.prop']
            $(this.refs.textArea.getDOMNode()).val("${vars['" + this.props.hierarchy.substring("vars".length + 1) + "']}");
        } else if (this.props.hierarchy.indexOf("ext") === 0) {
            // Property keys with '.' should be written as vars['some.prop']
            $(this.refs.textArea.getDOMNode()).val("${ext['" + this.props.hierarchy.substring("ext".length + 1) + "']}");
        } else {
            $(this.refs.textArea.getDOMNode()).val("${" + this.props.hierarchy + "}");
        }

        $(this.refs.textArea.getDOMNode()).select();
        document.execCommand("copy");
        this.setState({showTextCopiedMsg: true});
        setTimeout(function() {
            self.setState({showTextCopiedMsg: false});
        }, 200);
    }
});