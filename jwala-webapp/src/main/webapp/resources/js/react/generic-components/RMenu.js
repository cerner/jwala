/**
 * A menu component.
 *
 * Properties:
 *
 * 1. menuItems - an array that contains menu item objects
 *        menuItem:
 *            a. key - the menu item key that uniquely identifies it
 *            b. label - the label of the menu item
 *
 * 2. className - css class applied to the menu
 *
 * Sample Usage:
 *
 * <RMenu menuItems={[{key: "file", label: "File"},
 *                    {key: "edit", label: "Edit"},
 *                    {key: "view", label: "View", menuItems: [{key: "viewZoomIn", label: "Zoom In"},
 *                                                             {key: "viewZoomOut", label: "Zoom Out"}]}]}/>
 */
var RMenu = React.createClass({
    getInitialState: function() {
        return {currentlyFocusedItem: null, isShown: this.props.showByDefault, top: 0, left: 0};
    },
    interval: null,
    render: function() {
        var self = this;
        var listItemElementArray = [];

        if (this.state.isShown) {
            this.props.menuItems.forEach(function(menuItem){
                var isFocused = self.state.currentlyFocusedItem === menuItem.key;
                if (menuItem.menuItems && self.refs.divContainer) {
                    var left = ($(self.refs.divContainer.getDOMNode()).width() - 1) + "px";
                    listItemElementArray.push(React.createElement(RMenuItem, {key: menuItem.key, itemKey: menuItem.key, onMouseOver: self.onMenuItemMouseOver, onMouseOut: self.onMenuItemMouseOut,
                        isFocused: isFocused}, React.createElement("span", {className: "ui-menu-icon ui-icon ui-icon-carat-1-e"}),
                        menuItem.label, isFocused ? React.createElement(RMenu, {className: "ui-front subMenu", left: left, showByDefault: true, menuItems: menuItem.menuItems, onItemClick: self.onItemClick}) : null));
                } else {
                    listItemElementArray.push(React.createElement(RMenuItem, {key: menuItem.key, itemKey: menuItem.key, onMouseOver: self.onMenuItemMouseOver, onMouseOut: self.onMenuItemMouseOut,
                                                                              isFocused: isFocused, onClick: self.onItemClick}, menuItem.label));
                }
            });

            var className = this.props.className ? this.props.className : "";
            var style = this.props.left ? {left: this.props.left} : null;
            var ul = React.createElement("ul", {className: "ui-menu ui-widget ui-widget-content RMenu " + className,
                                         style: style}, listItemElementArray);

            if (this.props.className && this.props.className.indexOf("subMenu") > -1) {
                return ul;
            }

            return React.createElement("div", {ref: "divContainer",
                                       style:{zIndex: "999", display: "block", position: "fixed", top: this.state.top, left: this.state.left}},
                                       ul, React.createElement(ActivityMonitor, {ref: "activityMonitor", activeDefaultValue: true, inactivityCallback: this.inactivityCallback}));
        }
        return null;
    },
    onItemClick: function(val) {
        if (this.props.className && this.props.className.indexOf("subMenu") > -1) {
            this.props.onItemClick(val);
        } else {
            this.refs.activityMonitor.clearInterval();
            this.props.onItemClick(val);
            this.close();
        }
    },
    onMenuItemMouseOver: function(menuItem) {
        if (this.refs.activityMonitor) {
            this.refs.activityMonitor.setActive(true);
        }
        this.setState({currentlyFocusedItem: menuItem});
    },
    onMenuItemMouseOut: function(menuItem) {
        if (this.refs.activityMonitor) {
            this.refs.activityMonitor.setActive(false);
        }
    },
    inactivityCallback: function() {
        this.close();
    },
    show: function(top, left) {
        this.setState({isShown: true, top: top, left: left});
    },
    close: function() {
        this.setState({isShown: false});
    }
});

/**
 * A menu item.
 */
var RMenuItem = React.createClass({
    render: function() {
        var className = this.props.isFocused ? "ui-menu-item ui-state-focus" : "ui-menu-item";
        return React.createElement("li", {className: className, onClick: this.onClick},
                                   React.createElement("span", {className: "menuItem", onMouseOver: this.onMouseOver, onMouseOut: this.onMouseOut}, this.props.children));
    },
    onMouseOver: function() {
        this.props.onMouseOver(this.props.itemKey);
    },
    onMouseOut: function() {
        this.props.onMouseOut();
    },
    onClick: function() {
        if (this.props.onClick) {
            this.props.onClick(this.props.itemKey);
        }
    }
});

var ActivityMonitor = React.createClass({
    getInitialState: function() {
        return {active: this.props.activeDefaultValue};
    },
    render: function() {
        var self = this;
        if (this.interval === null) {
            this.interval = setInterval(function(){
                self.intervalCallback();
            }, 250);
        }
        return null;
    },
    interval: null,
    setActive: function(val) {
        this.state.active = val;
    },
    intervalCallback: function() {
//        console.log("intervalCallback...");
        if ($.isFunction(this.props.inactivityCallback)) {
            if (!this.state.active) {
                this.clearInterval();
                this.props.inactivityCallback();
            }
        }
    },
    clearInterval: function() {
        clearInterval(this.interval);
        this.interval = null;
    }
});
