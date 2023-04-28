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


// var $structChangeAuditForm;
$(function() {
    $structChangeAuditForm = $('#struct-change-wait-form');

    var $datetimepicker = $('#datetimepicker');
    $datetimepicker.datetimepicker({
        sideBySide: true,
        locale: 'zh-cn',
        minDate: moment().startOf('m').add(3, 'm').toDate(),
        format: 'YYYY-MM-DD HH:mm:ss'
    });


    $('#change-execTime').click(function() {
        var formData = {
            execTime: $structChangeAuditForm.find('[name="exec-time"]').val()
        };
        // validate
        try {
            formData.execTime = moment(formData.execTime, 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD HH:mm:ss');
        } catch (e) {
            $structChangeAuditForm.find('.form-msg').text('请填写正确的定时执行时间');
            return;
        }

         bootbox.confirm("你确定要更新执行时间吗?", function(ret){
            if (ret) {
                $.blockUI({message: ""});
                $.ajax({
                    url: base_path + '/task/struct-change/waiting/changeExecuteTime/' + $structChangeAuditForm.data('id'),
                    data: formData,
                    success: function (result) {
                        if(result.code == 0) {
                            var data = result.data;
                            bootbox.alert(data.right,function(){
                                location.reload();
                            });
                        }
                    },
                    complete: function () {
                        $.unblockUI();
                    }
                });
            }
        });

        $structChangeAuditForm.find('.form-msg').text('');
    });
});



// submit audit data
/*function submit(formData) {
    var $structChangeAuditForm = $('#struct-change-audit-form');
    // submit
    $.blockUI({message: ""});
    $.ajax({
        type:'POST',
        url: base_path + '/task/struct-change/waiting/changeExecuteTime/' + $structChangeAuditForm.data('id'),
        data: formData,
        success: function (result) {
            if(result.code == 0) {
                location.reload();
            }else{
                location.reload();
            }
        },
        complete: function () {
            $.unblockUI();
        }
    });
}*/




// Codemirror
// function initCodemirror(selecter) {
//     Codemirror.fromTextArea($(selecter).get(0), {
//         mode: 'text/x-mysql',
//         lineNumbers: false,
//         readOnly: true,
//         indentWithTabs: true,
//         autofocus: false,
//         matchBrackets: true
//     });
// }