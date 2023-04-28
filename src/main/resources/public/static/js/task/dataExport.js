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

var dataExportDsTemplate = require("dot!./dataExportDs.dot");

var $dataExportForm = $('#data-export-form');
$(function() {
    // render
    addDataExportDs();
    $('#add-ds').click(addDataExportDs);
    $(document).on('click', '.del-ds', function() {
        $(this).parents('.well').remove();
    });

    // form
    $dataExportForm.validator({
        fields: {
            "title": "标题:required;length[1~100]",
            "reason": "描述:length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $dataExportForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var formData = {
                title: $dataExportForm.find('[name="title"]').val(),
                security: true,
                reason: $dataExportForm.find('[name="reason"]').val(),
                exports: $dataExportForm.find('.data-export-ds').map(function() {
                    var $this = $(this);
                    var codeMirror = $this.find('.CodeMirror')[0].CodeMirror;
                    return {
                        ds: $this.find('[name="ds-name"]').val(),
                        sql: codeMirror.getValue()
                    };
                }).get()
            };
            if (!validateExportSqls($dataExportForm, formData.exports)) {
                return;
            }

            // pass validate
            $dataExportForm.find('.form-msg').text('');

            // submit
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/data-export/apply',
                contentType: 'application/json',
                data: JSON.stringify(formData),
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('数据导出申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else if (data.left == 1) {
                            bootbox.alert('数据导出申请失败:未知的数据源');
                        } else if (data.left == 2) {
                            bootbox.alert('数据导出申请失败:导出SQL语法不正确[' + data.right + ']');
                        } else {
                            bootbox.alert('数据导出申请失败');
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

function validateExportSqls($form, exports) {
    var dsArray = _.map(exports, function(item) { return item.ds; });
    if (_.uniq(dsArray).length != dsArray.length) {
        $form.find('.form-msg').text('数据源不能重复');
        return false;
    }

    return _.every(_.map(exports, function(data) {
        if (_s.isBlank(data.ds) || _s.isBlank(data.sql)) {
            $form.find('.form-msg').text('数据源和导出SQL不能为空');
            return false;
        } else if (data.sql.length > SQL_MAX_SIZE) {
            $form.find('.form-msg').text('导出SQL语句长度不能超过64KB');
            return false;
        } else {
            return true;
        }
    }));
}

function addDataExportDs() {
    var $addDs = $('#add-ds');
    var id = _.uniqueId('data-export-ds-');
    $addDs.before(dataExportDsTemplate({
        id: id
    }));

    var $dsEnv = $('#' + id + ' select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    var $dsName = $('#' + id + ' select[name="ds-name"]').select2();
    $dsEnv.on('change', function() {
        var value = $dsEnv.val();
        if (value) {
            $.get(base_path + '/ds/list?env=' + value, function (data) {
                $('#' + id + ' select[name="ds-name"] option').remove();
                $dsName.select2({
                    data: _.map(data, function(ds) {
                        return {
                            id: ds.id,
                            text: '[' + ds.type + ']' + ds.name
                        }
                    })
                });
                $dsName.val(null).trigger("change");
            });
        }
    });
    $dsEnv.trigger("change");
    initCodemirror('#' + id + ' [name="sql"]');
}

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