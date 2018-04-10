 //控制层 
app.controller('userController' ,function($scope,userService){
	
	// $controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=userService.update( $scope.entity ); //修改  
		}else{
			serviceObject=userService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}



	//注册用户添加

	$scope.reg=function () {
		//先进行判断

		// 如果两次输入的密码不一致  需要提示   前端的非空校验
		// 业务逻辑：当校验没通过的时候 不能点击发送验证码.
		// 还有 发送验证码 在一天只能同一个手机号 只能发送5次
		// 先校验手机号是否已经被注册了。

		console.info($scope.confirmpassword);
		console.info($scope.entity);

		if($scope.confirmpassword!=$scope.entity.password){
			alert("密码不一致");
			return ;
		}

		userService.add($scope.entity,$scope.smscode).success(
			function (response) {
				if(response.success){
					alert("添加成功");
				}else{
					alert(response.message);
				}
            }
		);
    }


    $scope.sendCode=function(){
        if($scope.entity.phone==null){
            alert("请输入手机号！");
            return ;
        }
        userService.sendCode($scope.entity.phone).success(
            function(response){
                alert(response.message);
            }
        );
    }



});	
