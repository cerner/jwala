/**
 * sources: http://coding.abel.nu/2012/01/jquery-ui-replacement-for-alert/
 */
$.extend({ alert: function (message, aTitle, aModal) {
  $("<div></div>").dialog( {
    buttons: { "Ok": function () { $(this).dialog("close"); } },
    close: function (event, ui) { $(this).remove(); },
    resizable: true,
    title: aTitle === undefined ? "Message" : aTitle,
    modal: aModal === undefined ? true : aModal
  }).text(message);
}
});