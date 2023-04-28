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
    var $dataChangeAdjustForm = $('#data-change-adjust-form');
    initCodemirror('#data-change-adjust-form [name="sql"]');

    // adjust
    $dataChangeAdjustForm.validator({
        fields: {
            sql: "数据变更SQL:required;length[1~" + SQL_MAX_SIZE + "]",
            reason: "重新申请描述:required;length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dataChangeAdjustForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            $dataChangeAdjustForm.find('.form-msg').text('');
            var formData = {
                apply: $dataChangeAdjustForm.find('[name=apply]').val(),
                sql: $dataChangeAdjustForm.find('[name=sql]').val(),
                reason: $dataChangeAdjustForm.find('[name=reason]').val()
            };
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/data-change/adjust/' + $dataChangeAdjustForm.data('id'),
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('调整数据变更申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else if (data.left == 1) {
                            bootbox.alert('调整数据变更申请失败:不能操作未授权的数据源');
                        } else if (data.left == 2) {
                            bootbox.alert('调整数据变更申请失败:导出SQL语法不正确[' + data.right + ']');
                        } else {
                            bootbox.alert('调整数据变更申请失败');
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