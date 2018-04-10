app.controller('cartController', function ($scope, cartService) {
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.sumMoney();
            }
        )
    }

    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList()
                } else {
                    alert(response.message);
                }
            }
        )
    }

    $scope.sumMoney = function () {
        var sumObjct = {totalNum: 0, totalMoney: 0};
        var cartList = $scope.cartList;
        for (var i = 0; i < cartList.length; i++) {
            //获取到每一个cart实体对象
            var cart = cartList[i];
            //循环遍历里面的List
            var orderItemList = cart.orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                //获取每一个OrderItem元素
                var orderItem = orderItemList[j];
                sumObjct.totalNum += orderItem.num;
                sumObjct.totalMoney += orderItem.totalFee;
            }
        }

        $scope.sumValue=sumObjct;
    }


    /**
     * 获取用户的地址列表
     */
    $scope.findAddressList=function(){
        cartService.findAddressList().success(
            function(response){
                $scope.addressList=response;
                //循环遍历 如果遍历到了isDefault唯一的就是当前应当默认选中的地址  给他赋予绑定的变量address中
                for(var i=0;i< $scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }

            }
        );
    }

    //写个方法 去绑定 一个变量 点击之后调用并设置为当前的地址对象
    //选择的是哪一个地址对象 赋予 address;
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

    //然后写方法 是用于判断当前的地址对象和变量是否相等，如果相等就意味着是同一个地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }


    $scope.order={paymentType:'1'};
    //选择支付方式
    $scope.selectPayType=function(type){
        $scope.order.paymentType= type;
    }


    $scope.submitOrder=function(){
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder( $scope.order ).success(
            function(response){
                if(response.success){
                    //页面跳转
                    if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
                        location.href="pay.html";
                    }else{//如果货到付款，跳转到提示页面
                        location.href="paysuccess.html";
                    }
                }else{
                    alert(response.message);	//也可以跳转到提示页面
                }
            }
        );
    }





})