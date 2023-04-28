require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var _s = require('underscore.string');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');

$(function() {
    $('#close-process').click(function() {
        $.blockUI({message: ""});
        $.ajax({
            url: base_path + '/task/struct-change/result/' + $('#close-process').data('id'),
            success: function (result) {
                if(result.code == 0) {
                    var data = result.data;
                    if (data == 0) {
                        location.href = base_path + '/';
                    } else {
                        bootbox.alert('结构变更流程关闭失败:该流程已不存在');
                    }
                }
            },
            complete: function () {
                $.unblockUI();
            }
        });
    });
});