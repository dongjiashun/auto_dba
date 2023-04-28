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
    var $dataExportAdjustForm = $('#data-export-adjust-form');
    initCodemirror('#data-export-adjust-form [name="sql"]');

    // adjust
    $dataExportAdjustForm.validator({
        fields: {
            sql: "导出SQL:required;length[1~" + SQL_MAX_SIZE + "]",
            reason: "重新申请描述:required;length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dataExportAdjustForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $dataExportAdjustForm.find('.form-msg').text('');
            var formData = {
                apply: $dataExportAdjustForm.find('[name=apply]').val(),
                sql: $dataExportAdjustForm.find('[name=sql]').val(),
                reason: $dataExportAdjustForm.find('[name=reason]').val()
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/data-export/adjust/' + $dataExportAdjustForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('调整数据导出申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else if (data.left == 1) {
                            bootbox.alert('调整数据导出申请失败:不能操作未授权的数据源');
                        } else if (data.left == 2) {
                            bootbox.alert('调整数据导出申请失败:导出SQL语法不正确[' + data.right + ']');
                        } else {
                            bootbox.alert('调整数据导出申请失败');
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
        indentWithTabs: true,
        autofocus: false,
        matchBrackets: true
    });
}