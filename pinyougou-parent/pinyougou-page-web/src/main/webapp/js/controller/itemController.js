app.controller('itemController',function($scope,$http){
	//数量操作
	$scope.addNum=function(x){
        $scope.num = parseInt($scope.num);
		x=parseInt(x);		
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
	}	


    


	$scope.specificationItems={};

    //用户点击时选择规格时调用 ，将数据绑定到对象中
	$scope.selectSpecification=function(name,value){	
		$scope.specificationItems[name]=value;
		searchSku();
	}

	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}		
	}


	$scope.loadSku=function(){
		//获取默认被选中的一个SKU 赋值给对应的变量
		$scope.sku=skuList[0];	
		//深克隆  因为从SPU查询的SKU列表是不变的，不能因为页面选择的规格改变而改变原来的数据，我们只是使用它而已。所以需要深克隆	
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}


    matchObject=function(map1,map2){		
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}
		return true;		
	}


	searchSku=function(){
		for(var i=0;i<skuList.length;i++ ){
			if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
				$scope.sku=skuList[i];
				return ;
			}			
		}	
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的		
	}


    $scope.addToCart=function(){
		alert($scope.sku.id);
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
            + $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
            function(response){
                if(response.success){
                    location.href='http://localhost:9107/cart.html';//跳转到购物车页面
                }else{
                    alert(response.message);
                }
            }
        );
    }



});
