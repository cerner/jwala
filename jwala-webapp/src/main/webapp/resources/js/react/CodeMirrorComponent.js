/**
 * A react component that encapsulates and manages a CodeMirror object (the template editor).
 * http://codemirror.net/
 */
var CodeMirrorComponent = React.createClass({
    codeMirror: null,
    getInitialState: function() {
        return {data: null};
    },
    render: function() {
        var metaData = [{ref: "saveBtn", icon: "ui-icon-disk", title: "Save", onClickCallback: this.saveCallback},
                        {ref: "formatBtn", icon: "ui-icon-shuffle", title: "Format", onClickCallback: this.props.formatCallback}];
        return React.createElement("div", {ref:"theContainer", className: this.props.className},
                   React.createElement(RToolbar, {ref: "theToolbar" , className: "toolbar-container", metaData: metaData}),
                   React.createElement("div", {ref: "codeMirrorHost"}));
    },
    componentDidMount: function() {
        var val = this.props.content ? this.props.content : "";
        this.codeMirror = CodeMirror(this.refs.codeMirrorHost.getDOMNode(), {value: val, lineNumbers: true,
                                     mode:  this.props.mode, readOnly: this.props.readOnly});
        this.state.data = this.codeMirror.getValue();
        this.codeMirror.on("change", this.onChanged);
        this.resize();
        this.refs.theToolbar.refs.saveBtn.setEnabled(false);
        if (!this.props.formatCallback) {
            this.refs.theToolbar.refs.formatBtn.setEnabled(false);
        }
    },
    componentWillUpdate: function(nextProps, nextState) {
        if (this.props.readOnly !== nextProps.readOnly) {
            this.codeMirror.setOption("readOnly", nextProps.readOnly)
        }
        this.setData(nextProps.content);
        this.refs.theToolbar.refs.saveBtn.setEnabled(this.isContentChanged());
    },
    saveCallback: function() {
        this.props.saveCallback(this.codeMirror.getValue());
    },
    getText: function() {
        return this.codeMirror.getValue();
    },
    isContentChanged: function() {
        return this.state.data !== this.getText()
    },
    resize: function() {
        var textAreaHeight = $(this.refs.theContainer.getDOMNode()).height() - $(this.refs.theToolbar.getDOMNode()).height() -
                    CodeMirrorComponent.SPLITTER_DISTANCE_FROM_TOOLBAR;
        console.log(textAreaHeight);
        $(".CodeMirror.cm-s-default").css("height", textAreaHeight);
    },
    onChanged: function() {
        this.refs.theToolbar.refs.saveBtn.setEnabled(this.isContentChanged());
        this.props.onChange();
    },
    setData: function(data) {
        this.codeMirror.off("change", this.onChanged);
        this.codeMirror.setValue(data);
        this.codeMirror.on("change", this.onChanged);
        this.state.data = this.codeMirror.getValue();
    },
    statics: {
        SPLITTER_DISTANCE_FROM_TOOLBAR: 19
    }
});