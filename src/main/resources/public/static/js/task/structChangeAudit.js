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
    var $structChangeAuditForm = $('#struct-change-audit-form');
    initCodemirror('#struct-change-audit-form [name="sql"]');

    $structChangeAuditForm.validator({
        fields: {
            "reason": "审核意见:required;length[1~255]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $structChangeAuditForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var formData = {
                agree: $structChangeAuditForm.find('[name=agree]').val(),
                reason: $structChangeAuditForm.find('[name=reason]').val()
            };
            $structChangeAuditForm.find('.form-msg').text('');

            if (~~formData.agree) {
                bootbox.confirm("你确定要通过结构变更审核吗?", function(ret){
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
    var $structChangeAuditForm = $('#struct-change-audit-form');
    // submit
    $.blockUI({message: ""});
    $.ajax({
        url: base_path + '/task/struct-change/audit/' + $structChangeAuditForm.data('id'),
        data: formData,
        success: function (result) {
            if(result.code == 0) {
                var data = result.data;
                if (data == 0) {
                    bootbox.alert('结构变更审核成功', function() {
                        location.href = base_path + '/process/struct-change/' + $structChangeAuditForm.data('process-id');
                    });
                } else {
                    bootbox.alert({
                        size: 'large',
                        message: '结构变更审核失败'
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