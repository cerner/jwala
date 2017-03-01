/**
 * A tab component which does not remounts its contents.
 *
 * Note: Remounting causes the contents to refresh/reload thus altering the display state.
 */
var RTabs = React.createClass({
    getInitialState: function() {
        return {activeTitle: null, activeHash: null};
    },
    render: function() {
        var self = this;
        var tabArray = [];
        var tabContentArray = [];

        var currentIndex = 0;
        if (this.state.activeHash !== null) {
            currentIndex = this.lookupIndexFromHash(this.state.activeHash, this.props.depth);
        }

        var i = 0;
        this.props.items.forEach(function(item) {
            var keyPostFix = item.title.replace(/ /g , "_"); // replace spaces with '_'
            tabArray.push(React.createElement(Tab, {idx: i, currentIndex: currentIndex,
                                                    key: "tab_" + keyPostFix,
                                                    title: item.title,
                                                    onSelectTabCallback: self.onSelectTabCallback}));
            tabContentArray.push(React.createElement(TabContent, {idx: i, currentIndex: currentIndex, key: "content_" + keyPostFix,
                                                                  className: self.props.contentClassName}, item.content));
            i++;
        });
        return React.createElement("div", {className: this.props.className},
                   React.createElement("ol", {className: "tabs-default"},
                       React.createElement("div", null, tabArray)), tabContentArray);
    },
    componentWillUpdate: function(nextProp, nextState) {
        if (this.state.activeHash !== nextState.activeHash) {
            if (history.pushState) {
                history.pushState(null, RTabs.TITLE_PREFIX + nextState.title, nextState.activeHash);
            } else {
                window.location.hash = nextState.activeHash;
            }
        }
    },
    onSelectTabCallback: function(idx, title) {
        var hash = RTabs.mergeIndexIntoHash(title, window.location.hash, this.props.depth);
        this.setState({activeTitle: title, activeHash: hash});
        if (this.props.onSelectTab !== undefined) {
            this.props.onSelectTab(idx);
        }
    },
    /**
     * If tab selection via windows location is implemented, use this method to get the current tab index.
     */
    lookupIndexFromHash: function(currentHash, depth) {
        if(!RTabs.hashRegex.test(currentHash)) {
            return 0;
        }
        /**
         *  1+depth*2 is a calculation that looks at the nesting level of the tabs
         *  and converts it to an group index for the regular expression
         *  allowing us to extract the window location hash component corresponding
         *  to this particular tab component.
         */
        var hash = RTabs.hashRegex.exec(currentHash)[1 + depth * 2];
        var idx = -1;
        for (var i = 0; i < this.props.items.length; i++) {
            if (this.props.items[i].title === hash) {
                idx = i;
                break;
            }
        }
        return idx;
    },
    statics: {
        TITLE_PREFIX: "Tomcat Operations Center - ",
        hashRegex: /#\/([^/]*)\/(([^/]*)\/)?(([^/]*)\/)?/,
        /* Merge into the hash in the URL by mapping this tab into existing fragments */
        mergeIndexIntoHash: function(title, currentHash, depth) {
            var matches = RTabs.hashRegex.exec(currentHash);
            var newList = "#/";

            if (matches === null) {
                for (var i = 0; i < depth; i++) {
                    newList = newList + "/";
                }
                newList = newList + title + "/"
            } else {
                for (var j = 0; j <= depth; j++) {
                    if(j === depth) {
                        newList = newList + title + "/";
                    } else if (matches[1 + j * 2] === null) {
                        newList = newList + "/"
                    } else {
                        newList = newList + matches[1 + j * 2] + "/"
                    }
                }
            }

            if(matches !== null) {
                return currentHash.replace(RTabs.hashRegex, newList);
            }
            return newList;
        }
    }
});

/**
 * A tab component.
 */
var Tab = React.createClass({
    render: function() {
        var className = this.props.currentIndex === this.props.idx ? "current" : "";
        return React.createElement("li", {className: className},
                   React.createElement("a", {onClick: this.onClick}, this.props.title));
    },
    onClick: function() {
        this.props.onSelectTabCallback(this.props.idx, this.props.title);
    }
});

/**
 * A tab content.
 */
var TabContent = React.createClass({
    render: function() {
        var style = (this.props.currentIndex === this.props.idx ? {} : {position: "absolute", top: -999999});
        return React.createElement("div", {className: this.props.className, style: style}, this.props.children);
    }
});