require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var _s = require('underscore.string');
var moment = require('moment');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('select2');
require('select2/dist/js/i18n/zh-CN');
var Codemirror = require('codemirror');
require('codemirror/mode/sql/sql');
require('nice-validator');

var $structChangeForm = $('#struct-change-form');
$(function() {
    var $dsName = $structChangeForm.find('select[name="ds-name"]').select2();
    $structChangeForm.find('select[name="ds-change-type"]').select2({minimumResultsForSearch: Infinity});
    initCodemirror($structChangeForm.find('[name="sql"]'));

    // form
    $structChangeForm.validator({
        fields: {
            "title": "标题:required;length[1~100]",
            "ds-name": "数据源:required",
            "sql": "结构变更SQL:required;length[1~64000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $structChangeForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            // pass validate
            $structChangeForm.find('.form-msg').text('');

            var formData = {
                title: $structChangeForm.find('[name="title"]').val(),
                ds: $structChangeForm.find('[name="ds-name"]').val(),
                type: $structChangeForm.find('[name="ds-change-type"]').val(),
                sql: $structChangeForm.find('[name="sql"]').val()
            };

            // submit
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/caas/stash',
                data: formData,
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 1) {
                            bootbox.alert('结构变更暂存失败:未知的数据源');
                        } else if (data.left == 2) {
                            bootbox.alert('结构变更暂存失败:' + data.right);
                        } else {
                            bootbox.alert('结构变更暂存成功', function() {
                                location.href = base_path + '/';
                            });
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