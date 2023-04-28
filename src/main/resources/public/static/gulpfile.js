'use strict';

var gulp = require('gulp');
var concat = require('gulp-concat');
var minifyCss = require('gulp-minify-css');
var less = require('gulp-less');
var clean = require('gulp-clean');
var webpack = require("webpack");

var baseCssList = [
    //'node_modules/bootstrap/dist/css/bootstrap.min.css',
    //'node_modules/bootstrap-table/dist/bootstrap-table.min.css',
    'css/lib/bootstrap.min.css',
    'css/lib/bootstrap-table.css',
    'css/lib/select2.css',
    'node_modules/font-awesome/css/font-awesome.min.css',
    'node_modules/metismenu/dist/metisMenu.min.css',
    'node_modules/toastr/build/toastr.min.css',
    'node_modules/codemirror/lib/codemirror.css',
    'node_modules/codemirror/addon/display/fullscreen.css',
    'node_modules/eonasdan-bootstrap-datetimepicker/build/css/bootstrap-datetimepicker.min.css',
    'node_modules/highlightjs/styles/github-gist.css',
    'css/module/base-product.css'
];
var webpackConfig = require("./webpack.config.js");

// task
gulp.task('default', ['css', 'webpack']);
gulp.task('debug', ['css', "webpack:dev"]);
gulp.task('dev', ['css', "webpack:dev", 'watch']);

gulp.task('watch', function() {
    gulp.watch('css/less/**/*.less', ['less']);
    gulp.watch(baseCssList, ['css']);
    gulp.watch(["js/lib/**/*", "js/src/**/*"], ["webpack:dev"]);
});

gulp.task('clean', function () {
    return gulp.src(['css/base.css', 'css/module', 'js/base.js', 'js/module'])
        .pipe(clean());
});

// css
gulp.task('less', function() {
    return gulp.src('css/less/**/*.less')
        .pipe(less())
        .pipe(minifyCss())
        .pipe(gulp.dest('css/module'));
});

gulp.task('css', ['less'], function() {
    return gulp.src(baseCssList)
        .pipe(concat('base.css'))
        .pipe(minifyCss())
        .pipe(gulp.dest('css/'));
});

// js
gulp.task('webpack', function(callback) {
    var config = Object.create(webpackConfig);
    config.plugins = config.plugins.concat(
        new webpack.optimize.DedupePlugin(),
        new webpack.optimize.UglifyJsPlugin()
    );
    webpack(config, function() { callback(); });
});

gulp.task('webpack:dev', function(callback) {
    var config = Object.create(webpackConfig);
    config.debug = true;
    webpack(config, function() { callback(); });
});
