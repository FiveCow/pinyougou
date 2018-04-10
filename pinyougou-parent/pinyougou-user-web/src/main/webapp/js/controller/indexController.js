app.controller('indexController',function ($scope,loginService) {

    $scope.showName = function () {
        loginService.showName().success(
            function (response) {
                alert(11);
                console.info(response);
                alert(response.loginName);
                $scope.loginName= response.loginName;
            }
        )
    }


});