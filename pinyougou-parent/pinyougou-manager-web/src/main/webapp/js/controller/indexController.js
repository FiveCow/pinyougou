
app.controller('indexController' ,function($scope,$controller,loginService){

    $controller('baseController',{$scope:$scope});

    //读取当前登录人
    $scope.showLoginName=function(){
        loginService.loginName().success(
            function(response){
                $scope.loginName=response.loginName;
            }
        );
    }
});
