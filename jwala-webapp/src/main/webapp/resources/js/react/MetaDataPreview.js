/** @jsx React.DOM */
var MetaDataPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        return <div ref="theContainer" className="xml-preview-container">
                   <div ref="codeMirrorHost"/>
               </div>
    },
    componentDidUpdate: function() {
        if (this.state.content) {
            $(this.refs.codeMirrorHost.getDOMNode()).empty(); // Remove old code mirror node if there is one to prevent duplicate
            CodeMirror(this.refs.codeMirrorHost.getDOMNode(), {value: this.state.content, lineNumbers: true,
                       mode: this.props.mode, readOnly: true});
            this.resize();
        }
    },
    refresh: function(content) {
        this.setState({content: content});
    },
    resize: function() {
        var textPreviewHeight = $(this.refs.theContainer.getDOMNode()).height() - MetaDataPreview.SPLITTER_DISTANCE_FROM_PREVIEW_COMPONENT;
        $(".CodeMirror.cm-s-default").css("height", textPreviewHeight);
    },
    statics: {
        SPLITTER_DISTANCE_FROM_PREVIEW_COMPONENT: 19
    }
})