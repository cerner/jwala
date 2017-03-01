var ModalButton = React.createClass({
    render: function() {
        return React.DOM.div(null,
                             this.props.modalDialog,
                             React.DOM.input({type:"button", onClick:this.handleClick, value:this.props.label}));
    },
    handleClick: function() {
        var view = this.props.view;

        var checklistOk = true;
        if (this.props.checklistCallback !== undefined) {
            checklistOk = this.props.checklistCallback();
        }

        if (checklistOk) {
            this.props.modalDialog.show(function(){
                if (view !== undefined) {
                    view.refresh();
                }
            });
        }

    }
});