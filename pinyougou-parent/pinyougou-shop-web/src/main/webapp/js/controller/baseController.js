 //品牌控制层 
app.controller('baseController' ,function($scope){	
	
    //重新加载列表 数据
    $scope.reloadList=function(){
    	//切换页码  
    	$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);	   	
    }
    
	//分页控件配置 
	$scope.paginationConf = {
         currentPage: 1,
         totalItems: 10,
         itemsPerPage: 10,
         perPageOptions: [10, 20, 30, 40, 50],
         onChange: function(){
        	 $scope.reloadList();//重新加载
     	 }
	}; 
	
	$scope.selectIds=[];//选中的ID集合 

	//更新复选
	$scope.updateSelection = function($event, id) {		
		if($event.target.checked){//如果是被选中,则增加到数组
			$scope.selectIds.push( id);			
		}else{
			var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
		}
	}

	//将JSON字符串 提取 出来显示指定的key的值 并以逗号隔开

    /**
	 *
     * @param jsonString 原来的JSON字符串
     * @param key 要显示的JSON中的对象的key 比如：显示text对应的值
     */
	$scope.jsonToString = function(jsonString,key){
		//先把JSON字符串转换成JSON对象（数组）
		var fromJsonObjArry = angular.fromJson(jsonString);
		var value="";

		//循环遍历 数组 获取里面的JSON对象
        for( var i=0;i<fromJsonObjArry.length;i++){
            // if(i>0){
        		// value=value+",";
			// }
            //根据key 获取JSON对象中key所对应的值 并以逗号分隔 返回即可。
             value += fromJsonObjArry[i][key]+",";//就相当于object.text
		}
		if(value.length>=1){
            value = value.substring(0,value.length-1);
        }
		return value;
	}

	//根据key 和值查询对应对象是否存在 存在就返回
    /**
	 *
     * @param list 传递过来的集合[]
     * @param key 需要查询的集合中的对象中的属性的名称
     * @param keyValue 属性名称所对应的值
     * @returns {*}
     */
    $scope.searchObjectByKey=function (list,key,keyValue) {
		for(var i=0;i<list.length;i++){
			var object = list[i];
			if(object[key]==keyValue){
				return object;
			}
		}
		return null;
    }
	
});	