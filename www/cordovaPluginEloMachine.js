var exec = require('cordova/exec');

exports.openCashDrawer = function (success, error) {
    exec(success, error, 'cordovaPluginEloMachine', 'openCashDrawer');
};

exports.print = function (printText, success, error) {
    exec(success, error, 'cordovaPluginEloMachine', 'print', [printText]);
};
