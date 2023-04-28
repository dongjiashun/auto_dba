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

var structChangeDsTemplate = require("dot!./structChangeDs.dot");

var $structChangeForm = $('#struct-change-form');
$(function() {
    // render
    addStructChangeDs();
    $('#add-ds').click(addStructChangeDs);
    $(document).on('click', '.del-ds', function() {
        $(this).parents('.well').remove();
    });

    // form
    $structChangeForm.validator({
        fields: {
            "title": "标题:required;length[1~100]",
            "reason": "描述:length[1~1000]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $structChangeForm.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            var formData = {
                title: $structChangeForm.find('[name="title"]').val(),
                reason: $structChangeForm.find('[name="reason"]').val(),
                changes: $structChangeForm.find('.struct-change-ds').map(function() {
                    var $this = $(this);
                    var codeMirror = $this.find('.CodeMirror')[0].CodeMirror;
                    return {
                        ds: $this.find('[name="ds-name"]').val(),
                        type: $this.find('[name="ds-change-type"]').val(),
                        sql: codeMirror.getValue()
                    };
                }).get()
            };
            if (!validateChangeSqls($structChangeForm, formData.changes)) {
                return;
            }

            // pass validate
            $structChangeForm.find('.form-msg').text('');

            // submit
            $.blockUI({message: ""});
            $.ajax({
                url: base_path + '/task/struct-change/apply',
                contentType: 'application/json',
                data: JSON.stringify(formData),
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('结构变更申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else if (data.left == 1) {
                            bootbox.alert('结构变更申请失败:未知的数据源');
                        } else if (data.left == 2) {
                            bootbox.alert('结构变更申请失败:结构变更SQL语法不正确<br/><pre>' + data.right + '</pre>');
                        } else if (data.left == 3) {
                            bootbox.alert('结构变更申请失败:你的变更过于频繁，请撤回上次提交后一起合并提交');
                        } else {
                            bootbox.alert('结构变更申请失败');
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

function validateChangeSqls($form, changes) {
    var dsArray = _.map(changes, function(item) { return item.ds; });
    if (_.uniq(dsArray).length != dsArray.length) {
        $form.find('.form-msg').text('数据源不能重复');
        return false;
    }

    return _.every(_.map(changes, function(data) {
        if (_s.isBlank(data.ds) || _s.isBlank(data.sql)) {
            $form.find('.form-msg').text('数据源和结构变更SQL不能为空');
            return false;
        } else if (data.sql.length > SQL_MAX_SIZE) {
            $form.find('.form-msg').text('结构变更SQL语句长度不能超过64KB');
            return false;
        } else {
            return true;
        }
    }));
}

function addStructChangeDs() {
    var $addDs = $('#add-ds');
    var id = _.uniqueId('struct-change-ds-');
    $addDs.before(structChangeDsTemplate({
        id: id
    }));

    var $dsEnv = $('#' + id + ' select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    var $dsName = $('#' + id + ' select[name="ds-name"]').select2();
    $('#' + id + ' select[name="ds-change-type"]').select2({minimumResultsForSearch: Infinity});
    $dsEnv.on('change', function() {
        var value = $dsEnv.val();
        if (value) {
            $.get(base_path + '/ds/list2?env=' + value, function (data) {
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