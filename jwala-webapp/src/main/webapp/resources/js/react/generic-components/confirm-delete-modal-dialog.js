/** @jsx React.DOM */
var ConfirmDeleteModalDialog = React.createClass({
    render: function() {
        if (this.props.show === true) {
            return <div><h3>Are you sure you want to delete the selected item ?</h3></div>
        }
        return <div/>
    },
    componentDidUpdate: function () {
        if (this.props.show === true) {
            this.show();
        }
    },
    show: function() {
        var dialogConfirm = this;
        var thisDomNode = dialogConfirm.getDOMNode();
        
        // Define the Dialog and its properties.
        $(thisDomNode).dialog({
            resizable: false,
            modal: true,
            title: "Confirmation Dialog Box",
            height: "auto",
            width: "auto",
            close: function () {
                dialogConfirm.props.btnClickedCallback("no");
            },
            buttons: {
                "Yes": function () {
                    $(thisDomNode).dialog("destroy");
                    dialogConfirm.props.btnClickedCallback("yes");
                },
                    "No": function () {
                    $(thisDomNode).dialog("destroy");
                    dialogConfirm.props.btnClickedCallback("no");
                }
            }
        });
    }
})