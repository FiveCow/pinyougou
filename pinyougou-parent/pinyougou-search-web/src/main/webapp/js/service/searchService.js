app.service('searchService',function ($http) {
    //搜索
    this.search = function (searhMap) {
        return $http.post('/itemsearch/search.do',searhMap);
    }
})