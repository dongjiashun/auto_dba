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
require('eonasdan-bootstrap-datetimepicker');

$(function() {
    var $dataExportAuditForm = $('#data-export-audit-form');
    initCodemirror('#data-export-audit-form [name="sql"]');
    var $datetimepicker = $('#datetimepicker');
    $datetimepicker.datetimepicker({
        sideBySide: true,
        locale: 'zh-cn',
        minDate: moment().startOf('m').add(10, 'm').toDate(),
        format: 'YYYY-MM-DD HH:mm:ss'
    });
    $datetimepicker.data('DateTimePicker').disable();

    $dataExportAuditForm.find('[name="agree"]').change(function() {
        var value = $(this).val();
        if (value && value == 2) {
            $datetimepicker.data('DateTimePicker').enable();
        } else {
            $datetimepicker.data('DateTimePicker').disable();
        }
    });

    $dataExportAuditForm.validator({
        fields: {
            "reason": "审核意见:required;length[1~255]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dataExportAuditForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var formData = {
                agree: $dataExportAuditForm.find('[name=agree]').val(),
                reason: $dataExportAuditForm.find('[name=reason]').val(),
                execTime: $dataExportAuditForm.find('[name="exec-time"]').val()
            };
            // validate
            if (formData.agree == 2) {
                try {
                    formData.execTime = moment(formData.execTime, 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD HH:mm:ss');
                } catch (e) {
                    $dataExportAuditForm.find('.form-msg').text('请填写正确的定时执行时间');
                    return false;
                }
            } else {
                formData.execTime = null;
            }
            $dataExportAuditForm.find('.form-msg').text('');

            // submit
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/data-export/audit/' + $dataExportAuditForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data == 0) {
                            bootbox.alert('数据导出审核成功', function() {
                                location.href = base_path + '/process/data-export/' + $dataExportAuditForm.data('process-id');
                            });
                        } else {
                            bootbox.alert('数据导出审核失败:你审核的任务已不存在');
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