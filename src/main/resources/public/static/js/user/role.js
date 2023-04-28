require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');


window.privilegesFormatter = function(value, row, index) {
    return '<ul>'
        + _.map(value, function(privilege) {
            return '<li title="' + privilege.desc + '">' + privilege.code + ' [' + privilege.name + ']' + '</li>'
        }).join('')
        + '</ul>';
};