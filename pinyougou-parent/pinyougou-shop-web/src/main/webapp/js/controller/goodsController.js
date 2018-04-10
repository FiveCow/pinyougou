//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        var id = $location.search()['id'];
        if (id != null && id !=undefined) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    editor.html($scope.entity.goodsDesc.introduction);
                    $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                   // alert($scope.entity.goodsDesc.customAttributeItems);
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                    $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                   // console.info(angular.toJson($scope.entity.itemList));

                    for( var i=0;i<$scope.entity.itemList.length;i++ ){
                        $scope.entity.itemList[i].spec =
                            JSON.parse( $scope.entity.itemList[i].spec);
                    }

                }
            );
        }
    }

    //新增
    $scope.add = function () {
        var serviceObject;//服务层对象
        $scope.entity.goodsDesc.introduction = editor.html();
        serviceObject = goodsService.add($scope.entity);//增加
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert("增加成功");
                    $scope.entity = {};
                    editor.html("");
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //保存：修改和新增
    $scope.save=function(){
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction=editor.html();

        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                   // alert('保存成功');
                    $scope.entity={};
                    editor.html("");
                    location.href="goods.html";

                }else{
                    alert(response.message);
                }
            }
        );
    }



    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //上传图片的
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    //上传成功
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }
        )
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};
    //$scope.entity.goodsDesc.itemImages=[];
    //将上传成功后的图片和颜色信息保存到列表中
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
        console.info($scope.entity.goodsDesc.itemImages);
    }

    //查询一级分类列表
    $scope.selectItemCat1List = function (parentId) {
        itemCatService.findByParentId(parentId).success(
            function (response) {
                $scope.itemCat1List = response;//一级商品分类列表
            }
        )
    }

    //监控函数 监控的是某一个变量的值的改变
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;//二级商品分类列表
            }
        )
    })

    //监控函数 监控的是某一个变量的值的改变
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;//二级商品分类列表
            }
        )
    })

    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId; //更新模板ID
            }
        );
    });

    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {

        //查询品牌列表
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;
                $scope.typeTemplate.brandIds = angular.fromJson($scope.typeTemplate.brandIds);
                if($location.search()['id']==null ||$location.search()['id']==undefined){
                    //获取模板对象中的扩展属性的值
                    $scope.entity.goodsDesc.customAttributeItems = angular.fromJson($scope.typeTemplate.customAttributeItems);
                }

            }
        );

        //获取规格列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                //console.info(">>>>"+angular.toJson(response));
                $scope.specList = response;
            }
        )
    });

    //定义一个方法
    $scope.updateSpecAttribute = function ($event, name, value) {
        //判断要添加的数据是否在于已经有的集合中
        //$scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }

    //新生产一个全新的变量对象 在复选框被点击的时候调用
    $scope.createItemList = function () {

        $scope.entity.itemList = [{spec: {}, price: 0, num: 9999, status: '0', isDefault: '0'}];//初始化

        //[{"attributeName":"网络制式","attributeValue":["移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","4.5寸"]}]
        var items = $scope.entity.goodsDesc.specificationItems;//

        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }


    //循环遍历 拼接字符串
    addColumn = function (list, columnName, columnValues) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                // alert(oldRow);
                // alert(angular.fromJson(oldRow));
                // alert(angular.toJson(angular.fromJson(oldRow)));
                var newRow = angular.fromJson(angular.toJson(oldRow));//先将对象转成字符串 再转成JSON对象 深克隆
                //console.info(newRow);
                // var newRow =JSON.parse( JSON.stringify( oldRow ));
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }


    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];


    $scope.itemCatList = [];//商品分类列表

    //加载商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        );
    }

    $scope.checkAttributeValue=function(specName,optionName){
        var items= $scope.entity.goodsDesc.specificationItems;
        //console.info(">>>>"+angular.toJson(items));

        var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }



});
