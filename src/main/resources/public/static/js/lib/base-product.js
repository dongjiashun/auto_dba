var $ = require('jquery');
var _ = require('underscore');
var _s = require('underscore.string');
var moment = require('moment');
require('bootstrap');
require('metismenu');
var hljs = require('highlightjs');
var utility = require('utility');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');

// Constants
window.SQL_MAX_SIZE = 65535;

// ajax settings
$.ajaxSetup({
    type: "POST",
    dataType: "json"
});
$(document).ajaxError(function(e, jqxhr){
    var d;
    if (jqxhr.status == 0) {
        // network or timeout
    } else if (jqxhr.status == 400) {
        bootbox.alert('你的输入格式不合法~');
    } else if (jqxhr.status == 403) {
        bootbox.alert('你没有相应的操作权限，请联系管理员~');
    } else {
        // 其他全局错误处理
        bootbox.alert('服务器访问发生错误，请联系管理员~');
    }
});

$(function() {
    // main layout
    $('#side-menu').metisMenu();
    $('.toggle-fullscreen').click(function () {
        utility.toggleFullscreen(document.documentElement);
    });

    // Loads the correct sidebar on window load,
    // collapses the sidebar on window resize.
    // Sets the min-height of #page-wrapper to window size
    $(window).bind('load resize', function() {
        topOffset = 50;
        width = (this.window.innerWidth > 0) ? this.window.innerWidth : this.screen.width;
        if (width < 768) {
            $('div.navbar-collapse').addClass('collapse');
            topOffset = 100; // 2-row-menu
        } else {
            $('div.navbar-collapse').removeClass('collapse');
        }

        // footer + page-bar (35 + 40)
        topOffset += 75;
        height = ((this.window.innerHeight > 0) ? this.window.innerHeight : this.screen.height);
        height = height - topOffset;
        if (height < 1) height = 1;
        if (height > topOffset) {
            $('#page-wrapper').css('min-height', (height) + 'px');
        }
    });

    // refer menu
    var url = typeof _referMenu != 'undefined' ? _referMenu : window.location.host + window.location.pathname;
    var element = $('.sidebar ul.nav a').filter(function () {
        return this.href == url || _s.endsWith(this.href, url);
    }).addClass('active').parent().parent().addClass('in').parent();
    if (element.is('li')) {
        element.addClass('active');
    }

    $('.navbar-top-links .userinfo').click(function() {
        bootbox.alert({
            title: '我的信息',
            message: $('#userinfo').html()
        });
    });

    // coming soon
    var $body = $('body');
    $body.on('click', '.toggle-soon', function() {
        toastr.info('客官别急，功能即将上线~');
    });

    $body.on('click', '[danger=danger]', function() {
        return !!confirm('这个操作很危险, 你确定要进行吗?');
    });

    $(document).on('click', '[data-info]', function() {
        var $this = $(this);
        var title = $this.attr('title');
        var large = $this.data('info-large');
        var data = $this.attr('data-info');
        data = _.escape(data);
        bootbox.alert({
            size: large ? 'large' : null,
            title: title ? title : '消息提示',
            message: '<pre>' + data + '</pre>'
        })
    });

    $(document).on('click', '[data-sec]', function() {
        var $this = $(this);
        var sec = $this.attr('data-sec');

        $.get(base_path + '/decrypt?sec=' + encodeURIComponent(sec), {}, function(data) {
            bootbox.alert({
                size: 'large',
                title: "解密数据",
                message: '<pre>' + data + '</pre>' +
                '<div class="small text-warning">系统会做相应的审计和调用次数限制,请谨慎使用</div>'
            })
        }, 'text');
    });

    $(document).on('click', '[show-sql]', function() {
        var $this = $(this);
        $.get(base_path + '/datasource/' + $this.data('id') + '/sct/' + $this.text(), function(ret) {
            bootbox.alert({
                size: 'large',
                title: "SHOW CREATE TABLE",
                message: ret.data ? '<pre class="code">' + hljs.highlightAuto(ret.data).value + '</pre>' : 'none'
            });
        });
    });

    // bootstrap
    $('[data-toggle="popover"]').popover();
    $('[data-toggle="tooltip"]').tooltip();

    // highlight js
    $('pre code').each(function(i, block) {
        hljs.highlightBlock(block);
    });

    try {
        // audit count
        if ($('#notify').length) {
            $.get(base_path + '/task/pending/count', function (resp) {
                if (resp.code == 0 && resp.data > 0) {
                    $('#notify').append('<span class="label label-danger">' + resp.data + '</span>');
                }
            });
        }

        // broadcasts
        $.get(base_path + '/broadcasts', function (broadcasts) {
            setTimeout(function () {
                showBroadcast(broadcasts, 0);
            }, 3000);

        });
    } catch (e) {
        console.log(e);
    }
});

function showBroadcast(broadcasts, index) {
    if (index >=0 && index < broadcasts.length) {
        toastr.info(broadcasts[index].message, null, {
            "closeButton": true,
            "progressBar": true,
            "positionClass": "toast-top-full-width",
            "timeOut": "15000",
            "extendedTimeOut": "5000",
            "onHidden": function () {
                setTimeout(function () {
                    showBroadcast(broadcasts, index + 1 >= broadcasts.length ? 0 : index + 1);
                }, (index == broadcasts.length - 1) ? 60000 : 3000);
            }
        });

    }
}

// bootstrap table formatter
function _textFormatter(value, maxTextLength) {
    value = value && _.escape(value);

    if (value && value.length > maxTextLength) {
        var newVal = _s.truncate(value, maxTextLength);
        newVal = '<span role="button" title="查看完成数据" data-info-large="true" data-info="'
            + value + '">' + newVal + '</span>';
        return newVal
    }

    return value;
}
window.textFormatter = function(value) {
    return _textFormatter(value, 20);
};

window.bigTextFormatter = function(value) {
    return _textFormatter(value, 100);
};
window.timeFormatter = function(value) {
    return value ? moment(value).format('YYYY-MM-DD HH:mm') : value;
};
window.simpleTimeFormatter = function(value) {
    return value ? moment(value).format('MM-DD HH:mm') : value;
};
window.taskTypeFormatter = function(value) {
    if (value == 'ds-apply') {
        return '数据源申请';
    } else if (value == 'data-export') {
        return '数据导出';
    } else if (value == 'data-change') {
        return '数据变更';
    } else if (value == 'struct-change') {
        return '结构变更';
    } else if (value == 'schema-apply') {
        return '创建数据源';
    } else if (value == 'canal-apply') {
        return 'Canal同步';
    }
    return value;
};

window.taskStatusFormatter = function(value) {
    if (value == 'process') {
        return '处理中';
    } else if (value == 'end') {
        return '已完成';
    } else if (value == 'cancel') {
        return '已完成(取消)';
    }
    return value;
};

window.envFormatter = function(value) {
    if (value == 'prod') {
        return '正式环境';
    } else if (value == 'test') {
        return '测试环境';
    } else if (value == 'dev') {
        return '开发环境';
    }
    return value;
};