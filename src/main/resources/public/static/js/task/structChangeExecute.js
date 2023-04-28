require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var _s = require('underscore.string');
var moment = require('moment');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
var Codemirror = require('codemirror');
require('codemirror/mode/sql/sql');
require('nice-validator');

var $structChangeExecuteForm;
$(function() {
    $structChangeExecuteForm = $('#struct-change-execute-form');
    initCodemirror('#struct-change-execute-form [name="sql"]');

    $structChangeExecuteForm.validator({
        fields: {
            "reason": "执行意见:required;length[1~255]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $structChangeExecuteForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var formData = {
                agree: $structChangeExecuteForm.find('[name=agree]').val(),
                reason: $structChangeExecuteForm.find('[name=reason]').val()
            };
            $structChangeExecuteForm.find('.form-msg').text('');

            if (~~formData.agree) {
                bootbox.confirm("你确定要执行结构变更吗?", function(ret){
                    if (ret) {
                        submit(formData);
                    }
                });
            } else {
                submit(formData);
            }
        }
    });

    running && window.setTimeout(checkRunning, 1000);
});

// submit audit data
function submit(formData) {
    var $structChangeExecuteForm = $('#struct-change-execute-form');
    // submit
    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/task/struct-change/execute/' + $structChangeExecuteForm.data('id'),
        data: formData,
        success: function (result) {
            if(result.code == 0) {
                var data = result.data;
                if (data.left == 0) {
                    bootbox.alert('结构变更执行成功', function() {
                        location.href = base_path + '/process/struct-change/' + $structChangeExecuteForm.data('process-id');
                    });
                } else if (data.left == -1) {
                    // 异步执行
                    bootbox.alert('结构变更提交成功, 现在进行异步执行, 点击确定查看', function() {
                        location.reload();
                    });
                } else if (data.left == 1) {
                    bootbox.alert('结构变更执行失败:未知的数据源');
                } else if (data.left == 2) {
                    bootbox.alert('结构变更执行失败:<br/><pre>' + data.right + '</pre>');
                } else {
                    bootbox.alert('结构变更执行失败');
                }
            }
        },
        complete: function () {
            $.unblockUI();
        }
    });
}

var checkRunningTimes = 0;
function checkRunning() {
    $.get(base_path + '/task/struct-change/progress/' + $structChangeExecuteForm.data('id'), function (resp) {
        var data = resp.data;

        var $execMsg = $('#exec-message');
        $execMsg.append('\n\n[' + (++checkRunningTimes) + '] ' + (data.right || 'reload status...'));
        $execMsg.scrollTop($execMsg.prop("scrollHeight"));

        data.left && window.setTimeout(checkRunning, 5000);
    });
}

// Codemirror
function initCodemirror(selecter) {
    Codemirror.fromTextArea($(selecter).get(0), {
        mode: 'text/x-mysql',
        lineNumbers: false,
        readOnly: true,
        indentWithTabs: true,
        autofocus: false,
        matchBrackets: true
    });
}