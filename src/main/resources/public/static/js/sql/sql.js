require('base-product');
var $ = require('jquery');
var _ = require('underscore');
var _s = require('underscore.string');
var sqlformatter = require('sqlformatter');
var toastr = require('toastr');
var bootbox = require('bootbox');
bootbox.setLocale('zh_CN');
require('jquery.blockUI');
require('bootstrap-table');
require('select2');
require('select2/dist/js/i18n/zh-CN');
var Codemirror = require('codemirror');
require('codemirror/addon/display/fullscreen');
require('codemirror/mode/sql/sql');

// template
var sqlTabTemplate = require("dot!./tab.dot");
var sqlRetTemplate = require("dot!./results.dot");

var $dsEnv, $dsName;
var dbType = 'mysql', dsId;
var $sqlHisTable = null;
$(function() {
    // select
    $dsEnv = $('select[name="ds-env"]').select2({minimumResultsForSearch: Infinity});
    $dsName = $('select[name="ds-name"]').select2();
    $dsEnv.on('change', dsChangeEnv);
    $dsName.on('change', dsChangeName);
    $dsEnv.trigger("change");

    // code mirror
    $('#sql-query').html(sqlTabTemplate());
    initCodemirror('#sql-query .sql-code');

    // sql query tabs
    $('#add-sql-tab').click(addTabPages);
    var $sqlTab = $('#sql-tab');
    $sqlTab.on('click', '>li>a .close', delTabPages);
    $sqlTab.on("click", ">li>a", showTabPages);

    var $sqlTabContent = $('#sql-tab-content');
    // sql format
    $sqlTabContent.on('click', '[name="format"]', formatSql);
    // query
    $sqlTabContent.on('click', '[name="query"]', querySql);
    $sqlTabContent.on('click', '[name="explain"]', explainSql);
    // clean
    $sqlTabContent.on('click', '[name="clean"]', cleanSql);

    // sql his
    $('#sql-his-tab').click(function () {
        if (!$sqlHisTable) {
            $sqlHisTable = $('#sql-his').find('table');
            $sqlHisTable.bootstrapTable({
                responseHandler: function(result) {
                    return {
                        rows: result.data,
                        total: result.pagination.rowCount
                    }
                }
            });
        }
    });
    $('#refresh-sql-his').click(function() {
        $sqlHisTable.bootstrapTable('refresh');
    });
    $('#sql-his').on('click', '[name="resql"]', reSql);
});

function dsChangeEnv() {
    var value = $dsEnv.val();
    if (value) {
        $.get(base_path + '/ds/list?env=' + value, function (data) {
            $('select[name="ds-name"] option').remove();
            $dsName.select2({
                data: _.map(data, function(ds) {
                    return {
                        id: ds.id,
                        text: '[' + ds.type + ']' + ds.name
                    }
                })
            });
            $dsName.trigger("change");
        });
    }
}
function dsChangeName() {
    dsId = $dsName.val();
}
function formatSql() {
    var $pane = $(this).parents('.tab-pane');
    var codeMirror = $pane.find('.CodeMirror')[0].CodeMirror;
    var value = codeMirror.getValue();
    if (!_s.isBlank(value)) {
        $.blockUI({message: ""});
        $.ajax({
            url: base_path + '/sql/format',
            data: {
                type: dbType,
                sql: value
            },
            success: function (result) {
                if(result.code == 0) {
                    if (result.data.left) {
                        codeMirror.setValue(result.data.right);
                    } else {
                        bootbox.alert('SQL格式化失败: ' + result.data.right);
                    }
                }
            },
            complete: function () {
                $.unblockUI();
            }
        });
    }
    codeMirror.focus();
}

function querySql() {
    query($(this).parents('.tab-pane'), false);
}
function explainSql() {
    query($(this).parents('.tab-pane'), true);
}

function query($pane, explain) {
    if (!dsId) {
        bootbox.alert('你必须先选择一个数据源');
        return;
    }

    var codeMirror = $pane.find('.CodeMirror')[0].CodeMirror;
    var value = codeMirror.getValue();

    if (!_s.isBlank(value)) {
        $.blockUI({message: "", timeout: 10000});
        $.ajax({
            url: base_path + '/sql/selects' + (explain ? '' : '?query=true'),
            data: {
                type: dbType,
                sql: value
            },
            success: function (result) {
                var unblock = true;
                if(result.code == 0) {
                    var data = result.data;
                    if (data.left) {
                        if (data.right && data.right.length > 0) {
                            var syntax = true;
                            _.each(data.right, function(sql) {
                                sql.length > SQL_MAX_SIZE && (syntax = false);
                            });
                            if (!syntax) {
                                bootbox.alert('SQL语法错误: select语句长度不能超过64KB');
                            } else {
                                unblock = false;
                                queryData($pane, data.right, explain);
                            }
                        } else {
                            bootbox.alert('SQL语法错误: 至少需要一条select/query语句');
                        }
                    } else {
                        bootbox.alert('SQL语法错误: ' + data.right.join(', '));
                    }
                }
                if (unblock) {
                    $.unblockUI();
                }
            }
        });
    }
}
function queryData($pane, sqls, explain) {
    $pane.find('.results').html(sqlRetTemplate({
        base: base_path,
        dsId: dsId,
        id: $pane.get(0).id,
        explain: explain,
        sqls: sqls
    }));

    $pane.find('table.result').each(function() {
        var $table = $(this);
        $table.bootstrapTable({
            $table: $table,
            queryParams: function (p) {
                return _.extend(p, { sql: decodeURIComponent($table.data('sql')) });
            }
        });
    });
}

window.queryResponseHandler = function(result) {
    $.unblockUI();
    var data;
    if (result.code == 0 && result.data.code == 0) {
        // convert data
        var retData = result.data;
        data = {
            rows: retData.page.data,
            total: retData.page.pagination.rowCount
        };

        // handle first request
        if (this.columns.lenght < 1 || this.columns[0].length < 1) {
            var columns;
            if (data.rows.length > 0) {
                var headers = _.zip(retData.page.header.columnLabels, retData.page.header.columnTypeNames);
                columns = _.map(headers, function (header) {
                    var col = {
                        field: header[0],
                        title: header[0]
                    };
                    var formatter = sqlformatter.getFormatter(header[1]);
                    if (formatter) {
                        col.formatter = formatter;
                    }

                    return col;
                });
            } else {
                columns = [{
                    field: 'no result',
                    title: 'no result'
                }];
            }

            // table refresh options
            // hack cache first ajax data
            var hackData = result;
            var url = this.url.replace('&record=1', '');
            this.$table.bootstrapTable('refreshOptions', {
                columns: columns,
                onlyInfoPagination: hackData.code ==0  && ~~hackData.data.page.pagination.pageSize >= 1000,
                url: url,
                ajax: function (request) {
                    if (hackData) {
                        // first request
                        request.success(hackData);
                        hackData = null;
                    } else {
                        $.ajax(request);
                    }
                }
            });
        }
    } else {
        // handle error
        data = [{
            error: result.error ? result.error : result.data.error
        }];
        this.$table.bootstrapTable('refreshOptions', {
            columns: [{
                field: 'error',
                title: 'error'
            }],
            data: data,
            sidePagination: 'client',
            url: ''
        });
    }

    return data;
};
function cleanSql() {
    var $pane = $(this).parents('.tab-pane');
    var codeMirror = $pane.find('.CodeMirror')[0].CodeMirror;
    codeMirror.setValue('');
    codeMirror.focus();
}

function reSql() {
    var id = $(this).data('id');
    var line = $sqlHisTable.bootstrapTable('getRowByUniqueId', id);
    var dsId = line.dataSource.id;
    $dsName.find('[value=' + dsId + ']').length > 0 && $dsName.val(dsId).trigger("change");
    var codeMirror = $('#sql-query').find('.CodeMirror')[0].CodeMirror;
    $('#sql-tab').find("li:first a").tab('show');
    codeMirror.setValue(line.sql);
    codeMirror.focus();
}

window.sqlHisOpFormatter = function(value, row, index) {
    return '<a data-id="' + value + '" name="resql" href="javascript:;" title="使用该SQL进行查询"><i class="fa fa-search fa-fw"></i></a>';
};

// Codemirror
function initCodemirror(selecter) {
    Codemirror.fromTextArea($(selecter).get(0), {
        mode: 'text/x-mysql',
        lineNumbers: true,
        indentWithTabs: true,
        autofocus: true,
        matchBrackets: true,
        extraKeys: {
            "F11": function(cm) {
                cm.setOption("fullScreen", !cm.getOption("fullScreen"));
            },
            "Esc": function(cm) {
                if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
            }
        }
    });
}

// tab add and remove
var tabPageNum = 1;
function addTabPages(e) {
    e.stopPropagation();

    var $sqlTab = $('#sql-tab');
    if ($sqlTab.find('>li').length >= 6) {
        bootbox.alert('你最多只能创建五个查询');
        return;
    }

    tabPageNum++;
    var id = 'sql-query' + tabPageNum;
    var $id = '#' + id;

    var $tabLi = $('<li role="presentation"><a href="' + $id + '" role="tab">SQL查询' + tabPageNum +
        '<button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>');

    $sqlTab.find('>li:last').before($tabLi);
    $('#sql-tab-content').append($('<div role="tabpanel" class="tab-pane" id="' + id + '">'
        + sqlTabTemplate()
        + '</div>'));
    $tabLi.find('a').tab('show');
    initCodemirror($id + ' .sql-code');
}

function delTabPages(e) {
    e.stopPropagation();
    var tabId = $(this).parents('li').children('a').attr('href');
    $(this).parents('li').remove('li');
    $(tabId).remove();
    $('#sql-tab').find('a:first').tab('show');
}

function showTabPages(e) {
    e.preventDefault();
    $(this).tab('show');
}
