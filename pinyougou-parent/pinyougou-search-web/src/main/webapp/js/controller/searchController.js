app.controller('searchController', function ($scope,$location, searchService) {


    //定义搜索选项值  key是一个字符串
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 20,
        'sortField': '',
        'sort': ''
    };

    $scope.addSearchItem = function (key, value) {
        if ('category' == key || 'brand' == key || 'price' == key) {
            $scope.searchMap[key] = value;//设置对象中的属性值
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    $scope.removeSearchItem = function (key) {
        if ('category' == key || 'brand' == key || 'price' == key) {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];//javascript的东西
        }
        $scope.search();
    }


    //搜索 参数是在此处建立
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//获取到的是一个map 查询到的列表所在的key 为rows
                buildPageLabel();
                console.info($scope.resultMap)
            }
        ).error(
            function () {
                alert('错误');
            }
        )
    }


    //构建分页标签


    buildPageLabel = function () {
        $scope.pageLable = [];
        var maxPage = $scope.resultMap.totalPages;//最大的页面
        var firstPage = 1;//显示的开始页
        var lastPage = maxPage;//显示的截止页码

        $scope.firstDot = false;
        $scope.lastDot = false;
        if (maxPage > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;
                $scope.lastDot = true;
            } else if ($scope.searchMap.pageNo >= maxPage - 2) {
                firstPage = maxPage - 4;//
                // lastPage = maxPage;//可以不写
                $scope.firstDot = true;
                $scope.lastDot = false;
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        }
        //否则都没点
        //遍历所有的总页数
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLable.push(i);//从1开始
        }
    }

    //目的就修改搜索的条件 并且 执行搜索

    $scope.queryByPage = function (pageNo) {
        pageNo = parseInt(pageNo);//先转换成数字
        //
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            alert("点击非法的页码");
            return;
        }

        $scope.searchMap.pageNo = pageNo;//点击哪一个页码传递过来即可 每页显示的行数不需要动
        $scope.search();
    }

    //判断当前页码是否为第一个页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }
    }
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    }

    //排序搜索
    $scope.sortSearch = function (sort, sortField) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();

    }


    //判断搜索的关键字是否为品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                $scope.searchMap.brand=$scope.searchMap.keywords;
                return true;
            }
        }
        return false;
    }


    //接收首页传递过来的值进行查询
    $scope.loadKeyWords=function () {
        var keywords = $location.search()['keywords'];
        if(keywords!=null && keywords!=undefined){
            $scope.searchMap.keywords=keywords;
            $scope.search();
        }
    }


})