/**
 * A panel widget for web app buttons
 */
var WebAppControlPanelWidget = React.createClass({
    doneCallback: {},
    render: function() {
        return <div className="WebAppControlPanelWidget">
                   <RButton className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-gear-custom"
                            onClick={this.generateConf}
                            title="Generate and deploy the webapp resources."
                            disabled = {!MainArea.isAdminRole}
                            disabledTitle="Resource generation is disabled for this version"
                            busyClassName="busy-button"/>
               </div>
    },
    generateConf: function(doneCallback) {
        var self = this;
        this.doneCallback[this.props.data.name] = doneCallback;
        this.props.webAppService.deployConf(this.props.data.name)
            .then(this.generateConfSuccessCallback)
            .caught(function(response){
                var parsedJsonResponse = JSON.parse(response.responseText);
                self.generateConfErrorCallback(parsedJsonResponse.applicationResponseContent ? parsedJsonResponse.applicationResponseContent : parsedJsonResponse.message, doneCallback, parsedJsonResponse.content);
            });
    },
    generateConfSuccessCallback: function(response) {
        this.doneCallback[this.props.data.name]();
        $.alert(this.props.data.name + " resource files deployed successfully", false);
    },
    generateConfErrorCallback: function(applicationResponseContent, doneCallback, errDetails) {
        this.doneCallback[this.props.data.name]();
        $.errorAlert(applicationResponseContent, "Deploy " + this.props.data.name +  "", false, errDetails);
    },
    statics: {
        getReactId: function(dom) {
            return $(dom).attr("data-reactid");
        }
    }
});
