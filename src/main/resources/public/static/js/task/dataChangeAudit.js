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

$(function() {
    var $dataChangeAuditForm = $('#data-change-audit-form');
    initCodemirror('#data-change-audit-form [name="sql"]');

    $dataChangeAuditForm.validator({
        fields: {
            "reason": "审核意见:required;length[1~255]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dataChangeAuditForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var formData = {
                agree: $dataChangeAuditForm.find('[name=agree]').val(),
                backup: $dataChangeAuditForm.find('[name=backup]').val(),
                reason: $dataChangeAuditForm.find('[name=reason]').val()
            };
            $dataChangeAuditForm.find('.form-msg').text('');

            if (~~formData.agree) {
                bootbox.confirm("你确定要执行数据变更吗?", function(ret){
                    if (ret) {
                        submit(formData);
                    }
                });
            } else {
                submit(formData);
            }
        }
    });
});

// submit audit data
function submit(formData) {
    var $dataChangeAuditForm = $('#data-change-audit-form');
    // submit
    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/task/data-change/audit/' + $dataChangeAuditForm.data('id'),
        data: formData,
        success: function (result) {
            if(result.code == 0) {
                var data = result.data;
                var msg = data.right ? (': <br/><pre>' + data.right) : '</pre>';
                if (data.left == 0) {
                    bootbox.alert('数据变更审核成功' + msg, function() {
                        location.href = base_path + '/process/data-change/' + $dataChangeAuditForm.data('process-id');
                    });
                } else {
                    bootbox.alert({
                        size: 'large',
                        message: '数据变更审核失败' + msg
                    });
                }
            }
        },
        complete: function () {
            $.unblockUI();
        }
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