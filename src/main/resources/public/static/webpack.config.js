'use strict';

var webpack = require("webpack");
var fs = require("fs");
var path = require("path");

var staticDir =  __dirname;
var srcDir =  path.resolve(staticDir, 'js/src');
var outDir =  path.resolve(staticDir, 'js/module');
var entries = genEntries(srcDir);

module.exports = {
    entry: entries,
    output: {
        path: outDir,
        filename: '[name].js'
    },
    resolve: {
        alias: {
            "nice-validator": "nice-validator/local/zh-CN.js",
            'handlebars': 'handlebars/dist/handlebars.min.js',
            'highlightjs': 'highlightjs/highlight.pack.min.js'
        },
        modulesDirectories: ["web_modules", 'node_modules', 'bower_components', 'lib']
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin("../base.js", 2),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery"
        })
    ]
};

function walk(path, names){
    var files = fs.readdirSync(path);
    files.forEach(function(item){
        if(fs.statSync(path + '/' + item).isDirectory()){
            walk(path + '/' + item, names);
        }else{
            names.push(path + '/' + item);
        }
    });
}

function genEntries(srcDir) {
    var map = {};
    var names = [];

    walk(srcDir, names);
    names.forEach(function(name) {
        var m = name.match(/(.+)\.js$/);
        if (m) {
            var entry = m[1].substring(srcDir.length + 1);
            map[entry] = name;
        }
    });
    return map;
}
