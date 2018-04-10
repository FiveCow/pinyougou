var app = angular.module('pinyougou', []);//定义模块

app.filter("trustHtml",['$sce',function ($sce) {
    //信任html标签
        return function (data) {
            return $sce.trustAsHtml(data);
        }
}]);