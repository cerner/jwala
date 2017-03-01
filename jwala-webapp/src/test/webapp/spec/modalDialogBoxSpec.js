describe("ModalDialogBox test suite", function() {
    var testUtils = React.addons.TestUtils;

    it("Can initialize the dialog box", function() {
        var dialog = React.createElement(ModalDialogBox, {title:"Test Dialog",
                                                          show:true,
                                                          content:React.createElement("input")});
        var dlgInstance = testUtils.renderIntoDocument(dialog);
        expect($.isEmptyObject(dlgInstance.refs)).toBe(false);
    });

    it("Can position dialog box at the center of the screen", function() {
        var width = 200;
        var height = 200;
        var dialog = React.createElement(ModalDialogBox, {title:"Test Dialog",
                                                          show:true,
                                                          content:React.createElement("input"),
                                                          width:width + "px",
                                                          height:height + "px"});
        var dlgInstance = testUtils.renderIntoDocument(dialog);

        var expectedLeftVal = $(window).width()/2 - (width/2);
        var expectedTopVal = $(window).height()/2 - (height/2);

        expect(dlgInstance.state.left).toBe(expectedLeftVal);
        expect(dlgInstance.state.top).toBe(expectedTopVal);
    });

    it("Can call cancel callback on 'X' click", function() {
        var cancelled = false;
        var dialog = React.createElement(ModalDialogBox, {title:"Test Dialog",
                                                                 show:true,
                                                                 cancelCallback:function() {cancelled = true},
                                                                 content:React.createElement("input")});
        var dlgInstance = testUtils.renderIntoDocument(dialog);
        testUtils.Simulate.click(dlgInstance.refs.xBtn.getDOMNode());
        expect(cancelled).toBe(true);
    });

    it('Can call cancel callback on "Cancel" button click', function() {
        var cancelled = false;
        var dialog = React.createElement(ModalDialogBox, {title:"Test Dialog",
                                                                 show:true,
                                                                 cancelCallback:function() {cancelled = true},
                                                                 content:React.createElement("input")});
        var dlgInstance = testUtils.renderIntoDocument(dialog);
        testUtils.Simulate.click(dlgInstance.refs.cancelBtn.getDOMNode());
        expect(cancelled).toBe(true);
    });

    it('Can call cancel callback on "Cancel" button click', function() {
        var okClicked = false;
        var dialog = React.createElement(ModalDialogBox, {title:"Test Dialog",
                                                                 show:true,
                                                                 okCallback:function() {okClicked = true},
                                                                 content:React.createElement("input")});
        var dlgInstance = testUtils.renderIntoDocument(dialog);
        testUtils.Simulate.click(dlgInstance.refs.okBtn.getDOMNode());
        expect(okClicked).toBe(true);
    });

    // TODO Unit tests above are just the initial tests and is no way comprehensive. Write additional unit tests.

});