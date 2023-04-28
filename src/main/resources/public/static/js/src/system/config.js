require('base-product');
var $ = require('jquery');
var Codemirror = require('codemirror');
require('codemirror/mode/properties/properties');

$(function() {
    initCodemirror('#sys-config');
});

// Codemirror
function initCodemirror(selecter) {
    Codemirror.fromTextArea($(selecter).get(0), {
        mode: 'text/x-properties',
        lineNumbers: true,
        readOnly: true,
        indentWithTabs: true,
        autofocus: false,
        matchBrackets: true
    });
}