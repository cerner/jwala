/** @jsx React.DOM */
var GroupConfig = React.createClass({
    getColDef: function() {
        return [{title:"Group Name", key:"name", renderCallback: this.renderNameCallback}];
    },
    getInitialState: function() {
        return {showModalFormEditDialog: false, selectedGroup: null};
    },
    render: function() {
        return  <div className={"dataTables_wrapper " + this.props.className}>
                    <table className="group-config-table-type-container">
                        <tr>
                            <td>
                                <div style={{float:"right"}}>
                                    <GenericButton label="Delete" accessKey="d" callback={this.delBtnCallback}/>
                                    <GenericButton label="Add" accessKey="a" callback={this.addBtnCallback}/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div>
                                    <RDataTable ref="groupConfigTable"
                                                colDefinitions={this.getColDef()}
                                                data={this.state.groupData}
                                                selectItemCallback={this.selectItemCallback}
                                                tableIndex="id.id"/>
                                </div>
                            </td>
                        </tr>
                    </table>

                    <ModalDialogBox ref="modalAddGroupDlg"
                                    title="Add Group"
                                    okCallback={this.okAddCallback}
                                    content={<GroupConfigForm ref="groupAddForm" />}/>
                    <ModalDialogBox ref="modalEditGroupDlg"
                                    title="Edit Group"
                                    okCallback={this.okEditCallback}
                                    content={<GroupConfigForm ref="groupEditForm"
                                                              data={this.state.selectedGroupForEditing}/>}/>
                    <ModalDialogBox ref="modalDeleteDlg"
                                    okLabel="Yes"
                                    cancelLabel="No"/>
                </div>
    },
    componentDidMount: function() {
        this.loadTableData();
    },
    selectItemCallback: function(group) {
        this.setState({selectedGroup: group});
    },
    renderNameCallback: function(data) {
        return <button className="button-link" onClick={this.onGroupNameClick.bind(this, data)}>{data}</button>
    },
    onGroupNameClick: function(groupName) {
        var self = this;
        this.props.service.getGroup(groupName,
            function(response){
                self.refs.modalEditGroupDlg.show();
                self.refs.groupEditForm.setState({id: self.state.selectedGroup["str-name"], groupName: groupName});
            },
            true
        )
    },
    addBtnCallback: function() {
        this.refs.modalAddGroupDlg.show();
    },
    delBtnCallback: function() {
        if (this.state.selectedGroup) {
            this.refs.modalDeleteDlg.show("Confirmation Dialog Box",
                                          <div className="text-align-center"><br/><b>Are you sure you want to delete {this.state.selectedGroup["str-name"]} ?</b><br/><br/></div>,
                                          this.confirmDeleteCallback);
        }
    },
    confirmDeleteCallback: function(data) {
        var self = this;
        this.props.service.deleteGroup(this.state.selectedGroup["str-name"],
                                       function() {
                                           self.refs.modalDeleteDlg.close();
                                           self.loadTableData(function(){
                                               self.state.selectedGroup = null;
                                           });
                                       },
                                       function(errMsg) {
                                          $.errorAlert(errMsg, "Error");
                                       });
        
    },
    okAddCallback: function() {
        if (this.refs.groupAddForm.isValid()) {
            var self = this;
            var groupName = this.refs.groupAddForm.state.groupName;
            this.props.service.insertNewGroup(groupName,
                                              function(){
                                                  self.refs.modalAddGroupDlg.close();
                                                  self.loadTableData(function(){
                                                      self.refs.groupConfigTable.deselectAllRows();
                                                      self.state.selectedGroup = null;
                                                  });
                                              },
                                              function(errMsg) {
                                                  $.errorAlert(errMsg, "Error");
                                              });
        }
    },

    loadTableData: function(afterLoadCallback) {
        var self = this;
        this.props.service.getGroups().then(function(response){
                                                self.refs.groupConfigTable.refresh(response.applicationResponseContent);
                                                if ($.isFunction(afterLoadCallback)) {
                                                    afterLoadCallback();
                                                }
                                            });
    },

    okEditCallback: function() {
        if (this.refs.groupEditForm.isValid()) {
            var self = this;
            this.props.service.updateGroup($(this.refs.groupEditForm.getDOMNode().children[0]).serializeArray(),
                                                 function(){
                                                     self.refs.modalEditGroupDlg.close();
                                                     self.loadTableData(function(){
                                                         self.state.selectedGroup = self.refs.groupConfigTable.getSelectedItem();
                                                     });
                                                 },
                                                 function(errMsg) {
                                                     $.errorAlert(errMsg, "Error");
                                                 });
        }
    }
})

/**
* The form that provides data input.
*/
var GroupConfigForm = React.createClass({
    getInitialState: function() {
        return {
            id: "", groupName: ""
        };
    },
    render: function() {
        return <div>
                   <form ref="groupConfigForm">
                       <input type="hidden" name="id" value={this.state.id} />
                           <table>
                               <tr>
                                   <td>Name</td>
                               </tr>
                                <tr>
                                    <td>
                                        <label htmlFor="name" className="error"></label>
                                    </td>
                                </tr>
                                <tr>
                                <td><input ref="groupName"
                                           name="name"
                                           className="group-config-form-name-input"
                                           type="text"
                                           value={this.state.groupName}
                                           onChange={this.onChangeGroupName}
                                           required/></td>
                                </tr>
                   </table>
                   </form>
               </div>
    },
    onChangeGroupName: function(event) {
        this.setState({groupName:event.target.value});
    },
    validator: null,
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({ignore: ":hidden", rules:{name: {nameCheck: true}}});

        /**
         * setTimeout is fixes the problem wherein the input box doesn't get focused in IE8.
         * Strangely it works without the setTimeout in jvm-config. What's more strange is that
         * any other component like a button can get focused except input elements!
         * This is the case whether jQuery or the node element focus is used.
         */
        var groupNameNode = this.refs.groupName.getDOMNode();
        setTimeout(function(){ groupNameNode.focus(); }, 100);

        $(this.refs.groupConfigForm.getDOMNode()).submit(function(e) {
            e.preventDefault();
        });
    },
    isValid: function() {
        this.validator.form();
        if (this.validator.numberOfInvalids() === 0) {
            return true;
        }
        return false;
    }
});
