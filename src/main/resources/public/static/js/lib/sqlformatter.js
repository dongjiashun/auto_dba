var _ = require('underscore');
var _s = require('underscore.string');
var moment = require('moment');

var maxTextLength = 20;
module.exports = {
    getFormatter: function(type) {
        type = type.toUpperCase();
        return typeFormatterMap[type];
    }
};

var typeFormatterMap = {
    "YEAR" : "sqlYearFormatter",
    "DATETIME" : "sqlDateTimeFormatter",
    "TIMESTAMP" : "sqlDateTimeFormatter",

    "TINYINT" : "sqlTinyIntFormatter",

    "VARCHAR" : "sqlTextFormatter",
    "VARCHAR-SEC" : "sqlSecFormatter",
    "LONGVARCHAR" : "sqlTextFormatter",
    "NVARCHAR" : "sqlTextFormatter",
    "LONGNVARCHAR" : "sqlTextFormatter",
    "CLOB" : "sqlTextFormatter",
    "NCLOB" : "sqlTextFormatter",
    "BLOB" : "sqlTextFormatter"
};

window.sqlTextFormatter = function(value) {
    return textFormatter(value, maxTextLength);
};

window.sqlBigTextFormatter = function(value) {
    return textFormatter(value, maxTextLength * 5);
};

window.sqlDateFormatter = function(value) {
    return value
        ? moment(value).format('YYYY-MM-DD')
        : value;
};

window.sqlTimeFormatter = function(value) {
    return value
        ? moment(value).format('HH:mm:ss')
        : value;
};

window.sqlYearFormatter = function(value) {
    return value
        ? moment(value).format('YYYY')
        : value;
};

window.sqlDateTimeFormatter = function(value) {
    return value
        ? moment(value).format('YYYY-MM-DD HH:mm:ss.SSS')
        : value;
};

window.sqlTinyIntFormatter = function(value) {
    if (value === false) {
        return 0;
    } else if (value === true) {
        return 1;
    } else {
        return value;
    }
};

window.sqlSecFormatter = function (value) {
    var newVal;
    if (value.length > maxTextLength) {
        newVal = _s.truncate(value, maxTextLength);
    } else {
        newVal = value;
    }

    return '<span class="text-warning" role="button" title="点击查看解密数据" data-sec="'
        + value + '">' + newVal + '</span>';
};

function textFormatter(value, maxTextLength) {
    value = value && _.escape(value);

    if (value && value.length > maxTextLength) {
        var newVal = _s.truncate(value, maxTextLength);
        newVal = '<span role="button" title="查看完成数据" data-info-large="true" data-info="'
            + value + '">' + newVal + '</span>';
        return newVal
    }

    return value;
}
