var exec = require('cordova/exec');

exports.honeywell_api = function (action, args, successCallBack, errorCallBack) {
    exec(successCallBack, errorCallBack, 'HoneywellBarcodePlugin', action, args);
};
exports.honeywell_callback = function (args, successCallBack, errorCallBack) {
    exec(successCallBack, errorCallBack, 'HoneywellBarcodePlugin', 'honeywell_callback', args);
};

