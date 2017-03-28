/**
 * A panel widget for web server buttons.
 */
var WebServerControlPanelWidget = React.createClass({
    doneCallback: {},
    render: function() {
        return <div className="web-server-control-panel-widget">

                    <RButton ref="drainBtn"
                             className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                             spanClassName="ui-icon ui-icon-drain-custom"
                             onClick={this.webServerDrain}
                             title="Drain"/>
                    <RButton ref="stopBtn"
                            className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-stop"
                            onClick={this.webServerStop}
                            title="Stop"/>

                    <RButton ref="startBtn"
                             className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                             spanClassName="ui-icon ui-icon-play"
                             onClick={this.webServerStart}
                             title="Start"/>

                    <RButton className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                             spanClassName="ui-icon ui-icon-gear-custom"
                             onClick={this.generateServiceAndHttpdConf}
                             title="Generate the httpd.conf and deploy as a service"
                             disabled = {!MainArea.isAdminRole}
                             disabledTitle="Resource generation is disabled for this version"
                             busyClassName="busy-button"/>

                    <button ref="httpdConfBtn" className="button-link anchor-font-style">httpd.conf</button>

                    <a target="_blank" ref="statusLink" href={"https://" + this.props.data.host + ":" + this.props.data.httpsPort
                            + jwalaVars.loadBalancerStatusMount}>status</a>

               </div>
    },

    componentDidMount: function() {
        $(this.refs.httpdConfBtn.getDOMNode()).click(this.onClickHttpdConf);
        $(this.refs.statusLink.getDOMNode()).click(this.onClickStatusLink);
    },

    webServerStart: function() {
        this.showFadingStatusClickedLabel("Starting...", this.refs.startBtn.getDOMNode(), this.props.data.id.id);
        this.props.webServerStartCallback(this.props.data.id.id,
                                          this.refs.stopBtn.getDOMNode(),
                                          this.props.data,
                                          WebServerControlPanelWidget.getReactId(this.refs.stopBtn.getDOMNode()).replace(/\./g, "-"),
                                          function() { /* cancel callback */ });
    },

    webServerStop: function() {
        this.showFadingStatusClickedLabel("Stopping...", this.refs.stopBtn.getDOMNode(), this.props.data.id.id);
        this.props.webServerStopCallback(this.props.data.id.id,
                                         this.refs.stopBtn.getDOMNode(),
                                         this.props.data,
                                         WebServerControlPanelWidget.getReactId(this.refs.stopBtn.getDOMNode()).replace(/\./g, "-"),
                                         function() { /* cancel callback */ });
    },
    webServerDrain: function(doneCallback) {
          this.showFadingStatusClickedLabel("Draining...", this.refs.drainBtn.getDOMNode(), this.props.data.id.id);
          this.doneCallback[this.props.data.name] = doneCallback;
          this.props.webServerService.drainWebServer(this.props.parentGroup,
                                           this.props.data.name,
                                           this.drainWebServerErrorCallback);
    },
    drainWebServerErrorCallback: function(applicationResponseContent, doneCallback) {
        this.doneCallback[this.props.data.name]();
        $.errorAlert(applicationResponseContent, "Drain " + this.props.data.name, false);
    },

    onClickHttpdConf: function() {
        var url = "webServerCommand?webServerId=" + this.props.data.id.id + "&operation=viewHttpdConf";
        window.open(url);
        return false;
    },

    generateServiceAndHttpdConf: function(doneCallback) {
        this.doneCallback[this.props.data.name] = doneCallback;
        this.props.webServerService.deployServiceAndHttpdConf(this.props.data.name,
                                                    this.generateServiceAndHttpdConfSucccessCallback,
                                                    this.generateServiceAndHttpdConfErrorCallback);
    },

    generateServiceAndHttpdConfSucccessCallback: function(response) {
        this.doneCallback[response.applicationResponseContent.name]();
         $.alert("Successfully installed the service, and generated and deployed configuration file(s).",
                 "Deploy " + response.applicationResponseContent.name, false);
    },

    generateServiceAndHttpdConfErrorCallback: function(applicationResponseContent, errDetails) {
        this.doneCallback[this.props.data.name]();
        $.errorAlert(applicationResponseContent, "Deploy " + this.props.data.name +  "", false, errDetails);
    },

    onClickStatusLink: function(e) {
        return e.stopPropagation();
    },

    /**
     * Uses jquery to take advantage of the fade out effect and to reuse the old code...for now.
     */
    showFadingStatusClickedLabel: function(msg, btnDom, webServerId) {
        var tooTipId = "tooltip" +  WebServerControlPanelWidget.getReactId(btnDom).replace(/\./g, "-") + webServerId;
        if (msg !== undefined && $("#" + tooTipId).length === 0) {
            var top = $(btnDom).position().top - $(btnDom).height()/2;
            var left = $(btnDom).position().left + $(btnDom).width()/2;
            $(btnDom).parent().append("<div id='" + tooTipId +
                "' role='tooltip' class='ui-tooltip ui-widget ui-corner-all ui-widget-content' " +
                "style='top:" + top + "px;left:" + left + "px'>" + msg + "</div>");

            $("#" + tooTipId).fadeOut(3000, function() {
                $("#" + tooTipId).remove();
            });

        }
    },

    statics: {
        getReactId: function(dom) {
            return $(dom).attr("data-reactid");
        }
    }

});
