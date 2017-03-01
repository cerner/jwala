/** @jsx React.DOM */
var AdminTab = React.createClass({

	getInitialState: function() { 
		ServiceFactory.getAdminService().viewProperties(
				this.onPropertiesRx);
        ServiceFactory.getAdminService().viewManifest(
                this.setManifestInfo);

        var theme = AdminTab.getCookie("theme");

		return { toEncrypt:"",
				 encrypted:"",
				 encryptLabel:"",
				 encryptLabelOr:"",
				 encryptProp:"",
				 properties:"",
				 manifest:"",
				 theme:theme === null ? "Redmond" : theme};
	},
	doEncrypt: function() {
		ServiceFactory.getAdminService().encryptServerSide(
				this.state.toEncrypt, 
				this.onEncryption,
				this.onEncryptionFail);
	},
	doReload: function() {
		ServiceFactory.getAdminService().reloadProperties(
				this.onPropertiesRx);
	},
	onPropertiesRx: function(e) {
		var str = "";		
		$.each( e.applicationResponseContent, function(key, value) {
			str = str + (key+"="+value+"\n");
		});
		this.setState({
			properties: str
		});
	},
	setManifestInfo: function (e) {
	    var manifestValue = "";
		$.each( e.applicationResponseContent, function(key, value) {
			manifestValue = manifestValue+ (key+"="+value+"\n");
		});
	    this.setState({
	        manifest: manifestValue
	    });
	},
	onChange: function(event) {
		this.setState({
			toEncrypt:event.target.value,
			encrypted:"",
			encryptProp:"",
			encryptLabel:"",
			encryptLabelOr:""});		
	},
	onEncryption: function(e) {
		this.setState({
			encrypted:e.applicationResponseContent,
			encryptProp:"${enc:"+e.applicationResponseContent+"}",
			encryptLabel:"Encryption Succeeded",
			encryptLabelOr:" or ",
			toEncrypt: ""});
	},
	onEncryptionFail: function(e) {
		this.setState({
			encrypted:"Please enter some data to encrypt.",
			encryptProp:"",
			encryptLabel:"Encryption Failed:",
			encryptLabelOr:"",
			toEncrypt: ""});
	},
    render: function() {

        var themes = ["cerner", "cupertino", "darkness", "dotluv", "eggplant", "redmond", "start", "sunny", "vader"];
        var themeOptions = [];
        var self = this;

        // <options selected> does not work in Chrome and Firefox, the work around is just to add the selected theme first
        themeOptions.push(themeOptions.push(React.createElement("option", {value:self.state.theme}, self.state.theme)));
        themes.forEach(function(theme) {
             if (theme !== self.state.theme) {
                themeOptions.push(React.createElement("option", {value:theme}, theme));
             }
        });

        return <div>
                    <h3>Encryption Tool</h3>
                    <p>
                    <label>Enter data to be secured:</label> <input className="toEncrypt" length="100" value={this.state.toEncrypt} onChange={this.onChange} />
                    		<br/>                    		
                    		<GenericButton label=">>> Encrypt >>>" callback={this.doEncrypt}/>
                    </p>
                    <p>
                    		<h4>{this.state.encryptLabel}</h4> 
                    </p>
                    <p><span>{this.state.encrypted}</span> </p>
                    <p><label>{this.state.encryptLabelOr}</label></p>
                    <p><span>{this.state.encryptProp}</span></p>
                    <br />                    
                    <h3>Properties Management</h3>   
                    <p>
                    <label>Reload vars.properties and logging configuration.</label><br />
                    <GenericButton label=">>> Reload >>>" callback={this.doReload} />                   
                    </p>
                    <p><textarea readonly="true" disabled="true" spellcheck='false' cols="100" rows="15" value={this.state.properties}></textarea></p>
                    <br />
                    <h3>MANIFEST.MF</h3>
                    <p>
                    <p><textarea readonly="true" disabled="true" spellcheck='false' cols="100" rows="8" value={this.state.manifest}></textarea></p>
                    </p>

                    {null
                    /* Uncomment code below and remove the curly braces and null to enable changeable themes.
                    <br />
                        <h3>Themes</h3>
                    <br/>
                    <select ref="themeSelection" onChange={this.onSelectTheme}>
                      {themeOptions}
                    </select>
                    */
                    }

               </div>
    },

    onSelectTheme: function() {
        document.cookie="theme=" + $(this.refs.themeSelection.getDOMNode()).val() + ";";
        // this.setState({theme:$(this.refs.themeSelection.getDOMNode()).val()});
        location.reload();
    },

    statics: {
        getCookie:function(cname) {
            var name = cname + "=";
            var ca = document.cookie.split(';');
            for(var i=0; i<ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1);
                if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
            }
            return null;
        }
    }

	
});