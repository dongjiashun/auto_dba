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

var timelineTemplate = require('dot!./structChangeOnline.dot');
var formTemplate = require('dot!./structChangeOnlineForm.dot');

var $onlineForm = $('#online-form');
var ds = null;
var task = null;
$(function() {
    var $timeline = $('#timeline');
    var $ds = $onlineForm.find('select[name="ds"]').select2();

    $('#timeline-btn').click(function () {
        ds = $ds.val();
        if (ds && ~~ds > 0) {
            clean();
            $.get(base_path + '/task/struct-change/online/' + ds, function (data) {
                if (data && data.length > 0) {
                    $timeline.html(timelineTemplate({
                        data: data,
                        moment: moment
                    }));
                } else {
                    $.get(base_path + '/task/struct-change/online-process/' + ds, function (tasks) {
                        if (tasks && tasks.length > 0) {
                            var tasksString = _.map(tasks, function(task) {
                                return '<li>'
                                    + '<input type="radio" name="task" value="' + task.id + '" disabled="disabled">&nbsp;&nbsp;'
                                    + moment(task.startTime).format('YYYY-MM-DD hh:mm') + '&nbsp;&nbsp;['
                                    + task.startUser.username + ']&nbsp;&nbsp;'
                                    + '<a target="_blank" href="' + base_path + '/process/struct-change/' + task.processInstanceId + '" title="点击查看详情">' + task.explain + '</a>'
                                    + '</li>';
                            }).join('');
                            $timeline.html('该数据源暂时无法提交上线变更，因为有如下变更正在处理中：<br/><ul class="list-unstyled">' + tasksString + '</ul>');
                        } else {
                            $timeline.html('该数据源暂无需要上线的变更');
                        }
                    });
                }
            });
        }
    });

    $(document).on('click', '#online-btn', function () {
        task = $timeline.find(':radio:checked ').val();
        if (task && ~~task > 0) {
            $.get('/task/struct-change/online/' + ds + '/' + task, function (data) {
                if (data) {
                    initChangeForm(data);
                } else {
                    $('#change').html('该数据源暂无需要上线的变更');
                }
            });
        }
    });
});

function initChangeForm(data) {
    var $change = $('#change');
    $change.html(formTemplate(data));
    initCodemirror('#change [name="sql"]');

    var $form = $change.find('form');
    $form.validator({
        fields: {
            "title": "标题:length[1~50]",
            "sql": "描述:required;length[1~65535]"
        },
        stopOnError: true,
        timely: 0,
        msgMaker: false,
        invalid: function(form, errors){
            $form.find('.form-msg').text(errors[0]);
        },
        valid: function() {
            // pass validate
            $form.find('.form-msg').text('');

            var formData = {
                ds: ds,
                task: task,
                title: $form.find('[name="title"]').val(),
                sql: $form.find('[name="sql"]').val()
            };

            // submit
            $.blockUI({message: ""});
            $.ajax({
                url: '/task/struct-change/online/',
                contentType: 'application/json',
                data: JSON.stringify(formData),
                success: function (result) {
                    if(result.code == 0) {
                        var data = result.data;
                        if (data.left == 0) {
                            bootbox.alert('结构变更上线申请成功', function() {
                                location.href = base_path + '/';
                            });
                        } else if (data.left == 1) {
                            bootbox.alert('结构变更上线申请失败:未知的数据源');
                        } else if (data.left == 2) {
                            bootbox.alert('结构变更上线申请失败:结构变更SQL语法不正确<br/><pre>' + data.right + '</pre>');
                        } else {
                            bootbox.alert('结构变更上线申请失败');
                        }
                    }
                },
                complete: function () {
                    $.unblockUI();
                }
            });
        }
    });
}

function clean() {
    $('#timeline').html('');
    $('#change').html('');
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