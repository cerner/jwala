var rTreeListData = [
    {name: "a", childrenGen1:[{childName: "a.1"}, {childName: "a.2"}, {childName: "a.3"}]},
    {name: "b", childrenGen1:[{childName: "b.1", childrenGen2:[{childName: "b.1.1"}, {childName: "b.1.2"}]}, {childName: "b.2"}]}
];

var rTreeListDataHtmlOutput = '<ul class="tree-list-style" data-reactid=".0.0"><li class="li-style " data-reactid=".0.0.$node-na-0-0"><img src="css/images/minus.png" class="expand-collapse-padding" data-reactid=".0.0.$node-na-0-0.0"><span class="tree-list-style " data-reactid=".0.0.$node-na-0-0.1">a</span></li><li class="tree-list-style " data-reactid=".0.0.$node-na-0-0.2"><ul class="tree-list-style" data-reactid=".0.0.$node-na-0-0.2.0"><li class="tree-list-style li-style selectable" data-reactid=".0.0.$node-na-0-0.2.0.$node-node-na-0-0-1-0"><span class="" data-reactid=".0.0.$node-na-0-0.2.0.$node-node-na-0-0-1-0.0">a.1</span></li><li class="tree-list-style li-style selectable" data-reactid=".0.0.$node-na-0-0.2.0.$node-node-na-0-0-1-1"><span class="" data-reactid=".0.0.$node-na-0-0.2.0.$node-node-na-0-0-1-1.0">a.2</span></li><li class="tree-list-style li-style selectable" data-reactid=".0.0.$node-na-0-0.2.0.$node-node-na-0-0-1-2"><span class="" data-reactid=".0.0.$node-na-0-0.2.0.$node-node-na-0-0-1-2.0">a.3</span></li></ul></li><li class="li-style " data-reactid=".0.0.$node-na-0-1"><img src="css/images/minus.png" class="expand-collapse-padding" data-reactid=".0.0.$node-na-0-1.0"><span class="tree-list-style " data-reactid=".0.0.$node-na-0-1.1">b</span></li><li class="tree-list-style " data-reactid=".0.0.$node-na-0-1.2"><ul class="tree-list-style" data-reactid=".0.0.$node-na-0-1.2.0"><li class="li-style selectable" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0"><img src="css/images/minus.png" class="expand-collapse-padding" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.0"><span class="tree-list-style " data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.1">b.1</span></li><li class="tree-list-style " data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.2"><ul class="tree-list-style" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.2.0"><li class="tree-list-style li-style " data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.2.0.$node-node-node-na-0-1-1-0-2-0"><span class="" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.2.0.$node-node-node-na-0-1-1-0-2-0.0">b.1.1</span></li><li class="tree-list-style li-style " data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.2.0.$node-node-node-na-0-1-1-0-2-1"><span class="" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-0.2.0.$node-node-node-na-0-1-1-0-2-1.0">b.1.2</span></li></ul></li><li class="tree-list-style li-style selectable" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-1"><span class="" data-reactid=".0.0.$node-na-0-1.2.0.$node-node-na-0-1-1-1.0">b.2</span></li></ul></li></ul>';

describe("RTreeList test suite", function() {
    var testUtils = React.addons.TestUtils;
    var treeMetaData = [{propKey: "name"}, {entity: "childrenGen1", propKey: "childName", selectable: true}, {entity: "childrenGen2", propKey: "childName", selectable: false}];
    var treeList = React.createElement(RTreeList, {title: "JVMs",
                                                   data: rTreeListData,
                                                   treeMetaData: treeMetaData,
                                                   expandIcon: "css/images/plus.png",
                                                   collapseIcon: "css/images/minus.png",
                                                   selectNodeCallback: function() {}});

    var treeList = testUtils.renderIntoDocument(treeList);

    it("Can display data in a tree like structure", function() {
        expect($(treeList.getDOMNode()).html()).toBe(rTreeListDataHtmlOutput);
    });

    it("Can collapse and drill down a node", function() {
        expect($(treeList.refs["node-na-0-1"].getDOMNode()).next().attr("class").indexOf("li-display-none")).toBe(-1);
        testUtils.Simulate.click(treeList.refs["node-na-0-1"].refs.expandCollapseIcon.getDOMNode());
        expect($(treeList.refs["node-na-0-1"].getDOMNode()).next().attr("class").indexOf("li-display-none")).not.toBe(-1);
        testUtils.Simulate.click(treeList.refs["node-na-0-1"].refs.expandCollapseIcon.getDOMNode());
        expect($(treeList.refs["node-na-0-1"].getDOMNode()).next().attr("class").indexOf("li-display-none")).toBe(-1);
    });

    it("Can select a \"selectable\" node", function() {
        var level = treeList.refs["node-na-0-1"].refs["node-node-na-0-1-1-1"].props.level;
        expect(treeList.refs["node-na-0-1"].refs["node-node-na-0-1-1-1"].props.treeMetaData[level]["selectable"]).toBe(true);
        expect(treeList.refs["node-na-0-1"].refs["node-node-na-0-1-1-1"].refs.nodeLabel.getDOMNode().className).toBe("");
        testUtils.Simulate.click(treeList.refs["node-na-0-1"].refs["node-node-na-0-1-1-1"].refs.nodeLabel.getDOMNode());
        expect(treeList.refs["node-na-0-1"].refs["node-node-na-0-1-1-1"].refs.nodeLabel.getDOMNode().className).toBe("ui-state-highlight");
    });

    it("Cannot select an \"unselectable\" node", function() {
        var level = treeList.refs["node-na-0-1"].props.level;
        expect(treeList.refs["node-na-0-1"].props.treeMetaData[level]["selectable"]).toBe(undefined);
        expect(treeList.refs["node-na-0-1"].refs.nodeLabel.getDOMNode().className.indexOf("selectable")).toBe(-1);
        testUtils.Simulate.click(treeList.refs["node-na-0-1"].refs.nodeLabel.getDOMNode());
        expect(treeList.refs["node-na-0-1"].refs.nodeLabel.getDOMNode().className.indexOf("selectable")).toBe(-1);
    });
});