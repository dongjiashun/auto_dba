var validator = require('validator');
var _ = require('underscore');
var _s = require('underscore.string');

function toggleFullscreen(elem) {
    // can fullscreen any element
    if ((document.fullScreenElement !== undefined && document.fullScreenElement === null) || (document.msFullscreenElement !== undefined && document.msFullscreenElement === null) || (document.mozFullScreen !== undefined && !document.mozFullScreen) || (document.webkitIsFullScreen !== undefined && !document.webkitIsFullScreen)) {
        if (elem.requestFullScreen) {
            elem.requestFullScreen();
        } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
        } else if (elem.webkitRequestFullScreen) {
            elem.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
        } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
        }
    } else {
        if (document.cancelFullScreen) {
            document.cancelFullScreen();
        } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        } else if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
    }
}

function prettyDelimiter(str, delimiter) {
    return _.filter(_.map(str.split(delimiter || ';'), function(str) {
        return _s.trim(str);
    }), function(str) {
        return !_.isEmpty(str);
    }).join(';');
}

var regex = {
};

var validate =  {
};

module.exports = {
    toggleFullscreen: toggleFullscreen,
    prettyDelimiter: prettyDelimiter,
    regex: regex,
    validate: validate
};