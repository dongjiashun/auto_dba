require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');

$(function() {
    $('[data-process-cancel]').click(function() {
        var url = $(this).data('process-cancel');
        bootbox.confirm("你确定要取消该流程吗?", function(result) {
            if (result) {
                $.blockUI({message: ""});
                $.ajax({
                    url: base_path + url,
                    success: function (result) {
                        if(result.code == 0) {
                            if (result.data == 0) {
                                bootbox.alert('流程取消成功', function() {
                                    location.href = base_path + '/';
                                });
                            } else {
                                bootbox.alert('流程取消失败');
                            }
                        }
                    },
                    complete: function () {
                        $.unblockUI();
                    }
                });
            }
        });
    });
});
