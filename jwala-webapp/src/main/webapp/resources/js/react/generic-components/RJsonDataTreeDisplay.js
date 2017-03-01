/**
 * Displays Json data in a tree like structure.
 *
 * Usages: <RJsonDataTreeDisplay refs="jsonTree" data={someJsonData} />
 *         <RJsonDataTreeDisplay refs="jsonTree" data={someJsonData} displayValueOnly="true" />
 *         <RJsonDataTreeDisplay refs="jsonTree" data={someJsonData} onShowToolTipCallback={function(hierarchy){return <div>hierarchy</div>}}>
 *
 * Parameters:
 *
 * 1. data - the tree's data in JSON format
 * 2. displayValueOnly - displays the node without the attribute name, just the value please see illustration below.
 *
 *      - Employees
 *          - Dept X
 *               Alice
 *               John
 *               Anne
 *          + Dept Y
 *          + Dept Z
 *
 * 3. onShowToolTipCallback - provides a facility to show a custom tooltip. The callback passes the hierarchy
 *                            of the current node in case it is needed like displaying it while providing copy/paste
 *                            functionality.
 *
 * Created by Jedd Cuison on 5/03/2016.
 */
var RJsonDataTreeDisplay = React.createClass({
    getInitialState: function() {
        return {data: this.props.data};
    },
    render: function() {
        var nodeArray = [];

        for (var key in this.state.data) {
            // prop key is required by React internally and if used inside node will result to undefined
            // that is why we need nodeKey
            var domKey = "node_" + (this.props.hierarchy ? this.props.hierarchy + "." + key : key);
            nodeArray.push(React.createElement(RJsonTreeNode, {key: domKey, nodeKey: key, val: this.state.data[key],
                displayValueOnly: this.props.displayValueOnly, hierarchy: this.props.hierarchy,
                onShowToolTipCallback: this.props.onShowToolTipCallback}));
        }
        var title = this.props.title ? this.props.title : null;
        return React.createElement("ul", {className: "RJsonDataTreeDisplay"}, title, nodeArray);
    },
    componentWillReceiveProps: function(nextProps) {
        if (nextProps.data) {
            this.refresh(nextProps.data);
        }
    },
    /**
     * Refreshes data and regenerates the tree.
     * Can be called using refs e.g. this.refs.jsonTree.refresh(someJsonData);
     */
    refresh: function(data) {
        this.setState({data: data});
    }
});

/**
 * The node.
 */
var RJsonTreeNode = React.createClass({
    getInitialState: function() {
        return {collapsed: true};
    },
    render: function() {
        if (this.props.val && this.props.val.constructor === Array) {
            var treeArray = [];
            if (!this.state.collapsed) {
                for (var key in this.props.val) {
                    var object = {};
                    object[this.props.nodeKey + "[" + key + "]"] = this.props.val[key];
                    var treeKey = "tree_" + (this.props.hierarchy ? this.props.hierarchy + "." : "") + this.props.nodeKey + "[" + key + "]";
                    treeArray.push(React.createElement(RJsonDataTreeDisplay, {key: treeKey,
                                                                              data: object,
                                                                              displayValueOnly: this.props.displayValueOnly,
                                                                              hierarchy: this.props.hierarchy,
                                                                              onShowToolTipCallback: this.props.onShowToolTipCallback}));
                }
            }

            var openCollapseIcon = this.props.val.length > 0 ? React.createElement(RJsonTreeNodeOpenCollapseWidget, {onClickCallback: this.onOpenCollapseWidgetClick}) : null;
            var liClassName =  this.props.val.length > 0 ? null : "valNode";
            return React.createElement("li", {className: liClassName}, openCollapseIcon, React.createElement("span", {className: "nodeKey"},
                       this.props.nodeKey), ":  Array[" + this.props.val.length + "]", treeArray);
        } else if (this.props.val && this.props.val.constructor === Object) {
            var tree = this.state.collapsed ? null : React.createElement(RJsonDataTreeDisplay,
                           {data: this.props.val, displayValueOnly: this.props.displayValueOnly, hierarchy: this.createHierarchy(),
                            onShowToolTipCallback: this.props.onShowToolTipCallback});
            return React.createElement("li", null, React.createElement(RJsonTreeNodeOpenCollapseWidget,
                {onClickCallback: this.onOpenCollapseWidgetClick}), React.createElement("span", {className: "nodeKey"},
                this.props.nodeKey), tree);
        }

        if (!this.props.displayValueOnly) {
            return React.createElement("li", {className: "valNode"},
                       React.createElement("span", {className: "nodeKey"}, this.props.nodeKey),
                       React.createElement("span", {className: "nodeVal", onMouseEnter: this.onMouseEnter,
                                                    onMouseOut: this.onMouseOut, onMouseMove: this.onMouseMove},
                                                    ":  " + this.props.val),
                       React.createElement(RJsonDataTreeDisplayToolTip, {ref: "toolTip", onCloseCallback: this.onToolTipClose}));
        }
        return React.createElement("li", {className: "valNode"}, this.props.val);
    },
    createHierarchy: function() {
        return this.props.hierarchy ? this.props.hierarchy + "." + this.props.nodeKey : this.props.nodeKey;
    },
    onOpenCollapseWidgetClick: function(collapsed) {
        this.setState({collapsed: collapsed});
    },

    timeOutFuncHandle: null,
    /**
     * Show a tooltip after x milliseconds.
     */
    onMouseEnter: function(e) {
        if (this.props.onShowToolTipCallback && !this.refs.toolTip.isShown() && this.timeOutFuncHandle === null) {
            var self = this;
            this.timeOutFuncHandle = setTimeout(function(x, y){
                var content = self.props.onShowToolTipCallback(self.props.hierarchy + "." + self.props.nodeKey);
                self.timeOutFuncHandle = self.refs.toolTip.show(x, y, content);
            }.bind(this, e.clientX, e.clientY), 500);
        }
        e.stopPropagation();
    },
    /**
     * Prevents the tooltip from appearing when the user just moves over the item.
     */
    onMouseOut: function(e) {
        if (this.timeOutFuncHandle !== null) {
            clearTimeout(this.timeOutFuncHandle);
            this.timeOutFuncHandle = null;
        }
        e.stopPropagation();
    },
    /**
     * Solves the problem wherein the mouse cursor can be placed outside the tooltip when the user moves the mouse
     * without moving out of the item.
     */
    onMouseMove: function(e) {
        if (!this.refs.toolTip.isShown() && this.timeOutFuncHandle !== null) {
            clearTimeout(this.timeOutFuncHandle);
            this.timeOutFuncHandle = null;
            this.onMouseEnter(e);
        }
        e.stopPropagation();
    },
    onToolTipClose: function() {
        this.timeOutFuncHandle = null;
    }
});

/**
 * The open/collapse widget.
 */
var RJsonTreeNodeOpenCollapseWidget = React.createClass({
    getInitialState: function() {
        return {collapsed: true};
    },
    render: function() {
        if (this.state.collapsed) {
            return React.createElement("span", {className: "openCollapseWidget ui-icon ui-icon-plus",
                                                onClick: this.onClick});
        }
        return React.createElement("span", {className: "openCollapseWidget ui-icon ui-icon-minus",
                                            onClick: this.onClick});
    },
    onClick: function() {
        var collapsed = !this.state.collapsed;
        this.setState({collapsed: collapsed});
        if (this.props.onClickCallback) {
            this.props.onClickCallback(collapsed);
        }
    }
});

/**
 * A tooltip component.
 */
var RJsonDataTreeDisplayToolTip = React.createClass({
    getInitialState: function() {
        return {show: false, x: 0, y: 0, content: "A tooltip!"};
    },
    render: function() {
        var style = this.state.show ? {display: "block", top: this.state.y - 25, left: this.state.x - 25, position: "fixed"} :
                                      {display: "none"};
        return React.createElement("div", {className: "tooltip container ui-tooltip ui-widget ui-widget-content", style: style,
                                           onMouseOut: this.onMouseOut}, this.state.content);
    },
    show: function(x, y, content) {
        this.setState({show: true, x: x, y: y, content: content});
    },
    close: function() {
        this.setState({show: false});
        if (this.props.onCloseCallback) {
            this.props.onCloseCallback();
        }
    },
    isShown: function() {
        return this.state.show;
    },
    onMouseOut: function(e) {
        if (this.getDOMNode() != e.relatedTarget) {
           // Prevent closing the tooltip if mouse out was triggered when hovering over child elements.
           return;
        }
        this.close();
        e.stopPropagation();
    }
});