package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.service.impl
 * @company www.itheima.com
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    //cartList ---->cart---->List<orderItem>

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品的ID获取商品的数据 商品的数据
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            return new ArrayList<>();
        }
        if(!"1".equals(item.getStatus())){
            return new ArrayList<>();
        }
        //2.判断商品是否在购物车列表中的购物对象中的购物明细中
        //3.如果不存在 就直接添加
        //4.如果存在就数量相加即可
        Cart cart = findCartBySellerId(cartList, item.getSellerId());



        if(cart==null){
            //说明没找到 直接添加即可
            cart = new Cart();
            cart.setSellerId(item.getSellerId());
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();

            TbOrderItem orderItem = createOrderItem(num, item);

            orderItemList.add(orderItem);

            cart.setOrderItemList(orderItemList);

            cartList.add(cart);

        }else{
            //说明找到了
            TbOrderItem tbOrderItem = findTbOrderItem(cart.getOrderItemList(), itemId);
            if(tbOrderItem==null){
                //说明有购物车 但是这个商家的购物车没有你要添加的商品
                //直接添加商品（orderItem）到OrderItemList中即可
                TbOrderItem orderItem = createOrderItem(num, item);
                List<TbOrderItem> orderItemList = cart.getOrderItemList();
                orderItemList.add(orderItem);//直接添加
            }else{
                //说明要添加的商品 已经在明细列表中存在，现在还要加一个商品那么也就是需要将商品的数量相加即可。
                tbOrderItem.setNum(tbOrderItem.getNum()+num);
                //金额修改
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue()*tbOrderItem.getNum()));
                if(tbOrderItem.getNum()<=0){
                    cart.getOrderItemList().remove(tbOrderItem);//删除
                }

                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);//删除掉
                }

            }


        }
        return cartList;
    }


    private static Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void saveRedisCartList(String username, List<Cart> cartList) {
        logger.info("用户："+username+":已经存入至redis中了");
        redisTemplate.boundHashOps("cartList").put(username,cartList);//cartList-----zhangsan----list<cart>
    }

    @Override
    public List<Cart> findRedisCartList(String username) {
        logger.info("用户："+username+":已经从redis获取到list集合了");
        List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            return new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartCookieList, List<Cart> cartRedisList) {

        //循环遍历listcookie 和 redis中的值做对比 ，如果有 则 数量相加  如果没有则直接添加即可

        for (Cart cart : cartCookieList) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                cartRedisList = addGoodsToCartList(cartRedisList, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartRedisList;
    }

    /**
     * 创建商品对象 添加到对应的购物车对象的商品明细列表中  List<TbOrderItem> itemList;
     * @param num
     * @param item
     * @return
     */
    private TbOrderItem createOrderItem(Integer num, TbItem item) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));//单件商品的总金额（数量*单价）
        return orderItem;
    }


    /**
     * 根据商家的ID 查询
     * @param cartList
     * @param sellerId
     * @return
     */
   private Cart findCartBySellerId(List<Cart> cartList, String sellerId){
       for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
       }
       return null;
   }

    /**
     * 根据要添加的商品的Id 去购物车明细列表中查询是否有要添加的商品
     * @param orderItemList
     * @param itemId
     * @return
     */
   private TbOrderItem findTbOrderItem(List<TbOrderItem> orderItemList,Long itemId){
       for (TbOrderItem orderItem : orderItemList) {
           if(orderItem.getItemId().longValue()==itemId){
               return orderItem;
           }
       }
       return null;
   }
}
