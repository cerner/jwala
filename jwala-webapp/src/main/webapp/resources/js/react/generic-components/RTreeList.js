/**
 * Displays hierarchical data in a single branch tree like list.
 *
 * Properties:
 *
 * 1. data
 * 2. treeMetaData = describes (in JSON) how data will be presented in a tree structure.
 *
 *    Example:
 *
 *     {propKey: "name",
 *      children:[{entity: "webServers",
 *                 propKey: "name",
 *                 selectable: true},
 *                {entity: "jvms",
 *                 propKey: "jvmName",
 *                 selectable: true, children:[{entity: "webApps",
 *                                              propKey: "name",
 *                                              selectable: true}]
 *                }]
 *     }
 *
 * 3. expandIcon
 * 4. collapseIcon
 * 5. selectNodeCallback - Callback that is called when a node is clicked.
 *
 * TODO: Write unit tests.
 */
var RTreeList = React.createClass({
    getInitialState: function() {
        return {selectedNode: null};
    },
    render: function() {
        var nodes = this.createTreeNodes(this.props.data, this.props.treeMetaData, 0, null, "");
        return React.createElement("ul", {className: "RTreeList root-node-ul", onClick: this.onClickUl}, nodes);
    },
    onClickUl: function() {
        if (this.props.selectNodeCallback(null, null, null)) {
            this.setState({selectedNode: null});
        }
    },
    onSelectNode: function(data, entityName, parent) {
        return this.props.selectNodeCallback(data, entityName, parent);
    },
    createTreeNodes: function(data, meta, level, parent, parentLabel) {
        var self = this;
        var nodes = [];

        for (var i = 0; i < data.length; i++) {
            var childNodes = [];
            if (meta.children !== undefined) {
                meta.children.forEach(function(child){
                    if (data[i][child.entity] !== undefined && data[i][child.entity].length > 0) {
                        childNodes.push(self.createTreeNodes(data[i][child.entity], child, level + 1, data[i], data[i][meta.propKey]));
                    }
                });
            }

            var key = parentLabel + data[i][meta.propKey] + level;
            var label = data[i][meta.label] === undefined ? data[i][meta.propKey] : data[i][meta.label];
            nodes.push(React.createElement(Node, {label: label,
                                                  collapsedByDefault: this.props.collapsedByDefault,
                                                  expandIcon: this.props.expandIcon,
                                                  collapseIcon: this.props.collapseIcon,
                                                  selectable: meta.selectable,
                                                  data: data[i],
                                                  theTree: this,
                                                  key: key /* React use */,
                                                  nodeKey: key,
                                                  selectedNode: this.state.selectedNode,
                                                  parent: parent,
                                                  entity: meta.entity,
                                                  icon: meta.icon}, childNodes));
        }
        return nodes;
    },
    getSelectedNodeData: function() {
        return this.state.selectedNode === null ? null : this.state.selectedNode.props.data;
    },
    setSelectedNode: function(node) {
        this.setState({selectedNode: node});
    }
});

/**
 * A tree node.
 */
var Node = React.createClass({
    getInitialState: function() {
        return {
            isCollapsed: this.props.collapsedByDefault,
            mouseOver: false
        }
    },
    isSelected: function() {
        var selectedNodeKey = this.props.selectedNode === null ? null : this.props.selectedNode.props.nodeKey;
        return this.props.nodeKey === selectedNodeKey;
    },
    render: function() {
        var children;
        var expandCollapseIcon = null;
        var liClassName = this.props.children.length === 0 ? "rtreelist-list-item no-children" : "rtreelist-list-item";

        var spanClassName = "rtreelist-list-item";
        if (this.isSelected()) {
            spanClassName = spanClassName + " " + Node.HIGHLIGHT_CLASS_NAME;
        } else if (this.state.mouseOver && this.props.selectable) {
            spanClassName = spanClassName + " " + Node.FOCUS_CLASS_NAME;
        }

        if (this.props.children.length > 0) {
            expandCollapseIcon = React.createElement("img", {ref: "expandCollapseIcon",
                                                             src: (this.state.isCollapsed ? this.props.expandIcon : this.props.collapseIcon),
                                                             onClick:this.onClickIconHandler, className: "expand-collapse-padding"});
            if (!this.state.isCollapsed) {
                children = React.createElement("ul", {className: "rtreelist-list"}, this.props.children);
            }
        }

        var nodeIcon;
        if (this.props.icon !== undefined) {
            nodeIcon = React.createElement("img", {src: this.props.icon});
        }

        return React.createElement("li", {className: liClassName}, expandCollapseIcon, nodeIcon,
                                          React.createElement("span", {onClick: this.onClickNodeHandler,
                                                                       onMouseOver: this.onMouseOver,
                                                                       onMouseOut: this.onMouseOut,
                                                                       className: spanClassName}, this.props.label), children);
    },
    componentDidMount: function() {
        // Add RTreeList specific data
        this.props.data["rtreeListMetaData"] = {parent: this.props.parent, entity: this.props.entity};
    },
    onMouseOver: function(event) {
        event.preventDefault();
        this.setState({mouseOver: true});
    },
    onMouseOut: function(event) {
        event.preventDefault();
        this.setState({mouseOver: false});
    },
    onClickIconHandler: function() {
        this.setState({isCollapsed:!this.state.isCollapsed});
    },
    onClickNodeHandler: function(e) {
        e.stopPropagation();
        if (this.props.theTree.state.selectedNode !== null) {
            // Check if node is clicked is the selected node, if it is, do nothing because it's already selected.
            if (this.props.theTree.state.selectedNode.isMounted() &&
                $(this.props.theTree.state.selectedNode.getDOMNode()).attr("data-reactid") === $(this.getDOMNode())
                    .attr("data-reactid")) {
                        return;
            }
        }

        if (this.props.selectable === true) {
            var oldNode = this.props.theTree.state.selectedNode;
            if (!this.props.theTree.onSelectNode(this.props.data, this.props.entity, this.props.parent)) {
                this.props.theTree.setState({selectedNode: oldNode});
            } else {
                this.props.theTree.setState({selectedNode: this});
            }
        }
    },
    statics: {
            HIGHLIGHT_CLASS_NAME: "ui-state-active",
            FOCUS_CLASS_NAME: "ui-state-focus"
    }
});