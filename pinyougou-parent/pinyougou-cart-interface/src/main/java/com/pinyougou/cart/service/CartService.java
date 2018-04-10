package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.service
 * @company www.itheima.com
 */
public interface CartService {

    //添加商品到购物车中 //需要注意的时候 传递原来的cookie中的购物车列表 然后判断要添加的商品是否在购物车列表中的购物车对象中的购物明细列表中

    /**
     *
     * @param cartList 原购物车的列表
     * @param itemId 要添加的商品的ID
     * @param num 要添加的商品的数量 （+1 或者是-1）都可以  正数是加 负数是－就可以了。
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    public void saveRedisCartList(String username,List<Cart> cartList);

    public List<Cart> findRedisCartList(String username);

    //合并购物车 （将redis中的购物车合并到redis中的购物车中）
    public List<Cart> mergeCartList(List<Cart>cartCookieList,List<Cart>cartRedisList);


}
