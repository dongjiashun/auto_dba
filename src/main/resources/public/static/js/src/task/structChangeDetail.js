require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var _s = require('underscore.string');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');

$(function() {
    $('#progress-detail').click(function() {
        $.blockUI({message: ""});
        $.ajax({
            url: base_path + '/task/struct-change/progress/' + $('#progress-detail').data('id'),
            success: function (result) {
                if(result.code == 0) {
                    var data = result.data;
                    if (data.left == 1) {
                        bootbox.alert('任务执行进度信息:<br/><pre>' + data.right + '</pre>');
                    } else {
                        bootbox.alert('任务已经执行结束,结果信息:<br/><pre>'+data.right+ '</pre>');
                    }
                }
            },
            complete: function () {
                $.unblockUI();
            }
        });
    });

    $('#cancel-progress').click(function() {

        bootbox.confirm("你确定要中止正在运行的结构变更吗?", function(ret){
            if (ret) {
                $.blockUI({message: ""});
                $.ajax({
                    url: base_path + '/task/struct-change/cancel-progress/' + $('#cancel-progress').data('id'),
                    success: function (result) {
                        if(result.code == 0) {
                            var data = result.data;
                            if (data.left == 0) {
                                bootbox.alert('任务中止成功:<br/><pre>' + data.right + '</pre>');
                            } else {
                                bootbox.alert('任务中止异常,结果信息:<br/><pre>'+data.right+ '</pre>');
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