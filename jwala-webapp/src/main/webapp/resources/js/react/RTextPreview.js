 /**
  * A component that shows text content on a read-only basis.
  * The content is formatted and colors applied to it.
  * Line numbers are shown at the left side as well.
  */
 var RTextPreview = React.createClass({
    render: function() {
        return React.createElement("div", {className: "text-preview-container"},
                   React.createElement(LineNumber, {ref:"lineNumber", count: RTextPreview.getLineCount(this.props.children)}, this.props.children),
                   React.createElement(TextViewer, {ref:"textViewer", val: this.props.children}));
    },
    statics: {
        getLineCount: function(text) {
            if (text === undefined) {
                return 0;
            }
            return text.split(/\r\n|\r|\n/).length;
        }
    }
 });

 var LineNumber = React.createClass({
    render: function() {
        if (this.props.count === 0) {
            return React.createElement("div", {ref: "n1", className: "number-container"}, "1");
        }

        var numbers = [];
        for (var i = 1; i <= this.props.count; i++) {
            numbers.push(React.createElement("div", {ref: "n" + i, className: "number-container"}, i));
        }

        return React.createElement("div", {className: "line-number-container"}, numbers);
    }
 });

 var TextViewer = React.createClass({
    render: function() {
        return React.createElement("div", {ref:"theContent", className: "text-preview-content text-preview-font"});
    },
    componentDidMount: function() {
        $(this.refs.theContent.getDOMNode()).html(this.formatText());
    },
    componentDidUpdate: function() {
        $(this.refs.theContent.getDOMNode()).html(this.formatText());
    },
    formatText: function() {
        var formattedText = "";
        if (this.props.val !== undefined) {
            formattedText = TextViewer.escapeHtml(this.props.val);
            formattedText = TextViewer.applyColors(formattedText);
            formattedText = formattedText.replace(/(?:\r\n|\r|\n)/g, "<br/>");
        }
        return formattedText;
    },
    statics: {
        escapeHtml: function(text) {
            return text.replace(/&/g, "&amp;")
                       .replace(/</g, "&lt;")
                       .replace(/>/g, "&gt;")
                       .replace(/"/g, "&quot;")
                       .replace(/'/g, "&#039;")
                       .replace(/ /g, "&nbsp;");
        },
        /**
         * A simple method that applies color.
         * TODO: Enhance to allow different coloring schemes to be applied.
         */
        applyColors: function(text) {
            return text.replace(/=&quot;(\w+)&quot;/g, function(x) {return "=&quot;<span class='val'>" + x.substring(7, x.length - 6)  + "</span>&quot;"})
                       .replace(/&nbsp;(\w*)=&quot;/g, function(x) {return "<span class='attr'>" + x.substring(0, x.length - 7) + "</span>=&quot;"})
                       .replace(/&lt;!--/g, "<span class='xml-comment'>&lt;!--")
                       .replace(/--&gt;/g, "--&gt;</span>")
                       .replace(/&lt;[^!--]/g, function(x) {return "<span class='xml-less-than'>&lt;" + x.charAt(x.length - 1)})
                       .replace(/[^--]&gt;/g, function(x) {;return x.charAt(0) + "&gt;</span>"});
        }
    }
 });