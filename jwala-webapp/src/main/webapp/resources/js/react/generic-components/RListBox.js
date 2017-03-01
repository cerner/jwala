/**
 * A list box component.
 *
 * TODO: Unit tests
 *
 */
var RListBox = React.createClass({
    getInitialState: function() {
        return {selectedValue: null, checkedItems: []};
    },
    render: function() {
        return React.createElement("ul", {className: "RListBox container"}, this.createResourceList());
    },
    createResourceList: function() {
        var self = this;
        var resourceList = [];
        var i = 0;
        this.props.options.forEach(function(option) {
            resourceList.push(React.createElement(Option, {key: option.value,
                                                           value: option.value,
                                                           selectedValue: self.state.selectedValue,
                                                           checkBoxEnabled: self.props.multiSelect,
                                                           label: option.label,
                                                           onClick: self.onOptionClick,
                                                           onContextMenu: self.props.onContextMenu,
                                                           checkCallback: self.optionCheckCallback}));
        });
        return resourceList;
    },
    componentWillReceiveProps: function(nextProps) {
      this.setState({selectedValue: null});
    },
    onOptionClick: function(value) {
        if (this.state.selectedValue !== value) {
            if (this.props.selectCallback !== undefined) {
                if (!this.props.selectCallback(value)) {
                    return;
                }
            }

            this.setState({selectedValue: value});
        }
    },
    getSelectedValue: function() {
        return this.state.selectedValue;
    },
    optionCheckCallback: function(val, checked) {
        if (checked) {
            this.state.checkedItems.push(val);
        } else {
            var i;
            for (i = 0; i < this.state.checkedItems.length; i++) {
                if (this.state.checkedItems[i] === val) {
                    break;
                }
            }
            this.state.checkedItems.splice(i, 1);
        }
    },
    getCheckedItems: function() {
        return this.state.checkedItems;
    }
});

var Option = React.createClass({
    getInitialState: function() {
        return {mouseOver: false, checked: false};
    },
    isSelected: function() {
        return this.props.selectedValue === this.props.value;
    },
    render: function() {
        var stateClassName = "";
        if (this.isSelected()) {
            stateClassName = "ui-state-active";
        } else if (this.state.mouseOver) {
            stateClassName = "ui-state-focus";
        }

        var listItems = [];
        if (this.props.checkBoxEnabled === true) {
            listItems.push(React.createElement("input", {key: "checkBox", type: "checkbox", className: "noSelect", onChange: this.onCheckBoxChange, checked: this.state.checked}));
        }
        listItems.push(React.createElement("span", {key: "label"}, this.props.label));

        return React.createElement("li", {className: stateClassName,
                                          onContextMenu: this.onContextMenu,
                                          onMouseOver: this.onMouseOver,
                                          onMouseOut: this.onMouseOut, onClick: this.onClick}, listItems);
    },
    onClick: function() {
        this.props.onClick(this.props.value);
    },
    onContextMenu: function(e) {
        if ($.isFunction(this.props.onContextMenu)) {
            this.props.onContextMenu(e, this.props.value);
        }
        return false;
    },
    onMouseOver: function() {
        this.setState({mouseOver: true});
    },
    onMouseOut: function() {
        this.setState({mouseOver: false});
    },
    onCheckBoxChange: function() {
        var checked = !this.state.checked;
        this.setState({checked: checked});
        this.props.checkCallback(this.props.value, checked);
    }
});
