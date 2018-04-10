package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.CookieUtil;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.controller
 * @company www.itheima.com
 */
@RestController
@RequestMapping("/cart")
public class CartController  {

    @Reference(timeout = 60000)
    private CartService cartService;

    private static final String ANONYMOUS_USER = "anonymousUser";

    //查询购物车

    @RequestMapping("/findCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response){
        //判断如果当前的用户是匿名用户那么就是 用户并没有登录
        //如果不是 意味着 这个用户登录了。需要从redis中获取数据
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if(ANONYMOUS_USER.equals(username)) {//匿名用户 从cookie中获取数据
            //获取到购物车列表 从cookie中获取数据。
            String cookieCartListString = CookieUtil.getCookieValue(request, "cartList", true);//是否要解码 true 表示要解码 因为有中文所以在添加的时候需要转码
            if (StringUtils.isBlank(cookieCartListString)) {
                cookieCartListString = "[]";
            }
            List<Cart> cartList = JSON.parseArray(cookieCartListString, Cart.class);
            return cartList;
        }else{
            //获取cookie中的购物车列表
            String cookieCartListString = CookieUtil.getCookieValue(request, "cartList", true);//是否要解码 true 表示要解码 因为有中文所以在添加的时候需要转码
            if (StringUtils.isBlank(cookieCartListString)) {
                cookieCartListString = "[]";
            }
            List<Cart> cookieCartList = JSON.parseArray(cookieCartListString, Cart.class);
            //获取redis中的购物车列表
            List<Cart> redisCartList = cartService.findRedisCartList(username);//返回的一定是不为空的 因为是 new arrayList
            if(cookieCartList!=null&& cookieCartList.size()>0){
                //合并
                List<Cart> cartList = cartService.mergeCartList(cookieCartList, redisCartList);
                //清除cookie中的购物车列表
                CookieUtil.deleteCookie(request,response,"cartList");
                //将合并后的数据存入redis中
                cartService.saveRedisCartList(username,cartList);

                return cartList;
            }
            //如果cookie是空 那么直接返回redis中的数据 不需要合并
            return redisCartList;
        }
    }


    //添加购物车(删除 更新数量)
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("username>>>"+username);
            if(ANONYMOUS_USER.equals(username)) {//如果是匿名用户那么就是没登录的用户
                //1.从cookie中获取购物车的列表
                List<Cart> cartList = findCartList(request,response);
                //2.添加商品到获取到的列表中  然后再添加到cookie中去
                List<Cart> cartsList = cartService.addGoodsToCartList(cartList, itemId, num);
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartsList),3600*24,true);
                return new Result(true,"添加成功");
            }else{
                List<Cart> redisCartList = cartService.findRedisCartList(username);
                redisCartList = cartService.addGoodsToCartList(redisCartList, itemId, num);
                cartService.saveRedisCartList(username,redisCartList);
                return new Result(true,"添加redis成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }


}
