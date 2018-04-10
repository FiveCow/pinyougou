package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.IdWorker;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.cart.controller
 * @company www.itheima.com
 */
@RestController
@RequestMapping("/pay")
public class WeixinPayController {
    private Logger logger = LoggerFactory.getLogger(WeixinPayController.class);

    @Reference
    private WeixinPayService weixinPayService;


    @Autowired
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
//        IdWorker idWorker = new IdWorker();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(userId);
        if(tbPayLog==null) {
            return new HashMap();
        }
        Map map = weixinPayService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee()+"");
        return map;
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int count =0;
        while(true){
            //调用查询接口
            Map<String,String>map = weixinPayService.queryPayStatus(out_trade_no);
            if(map==null){//出错
                result=new  Result(false, "支付出错");
                break;
            }
            if(map.get("trade_state").equals("SUCCESS")){//如果成功

                //如果一旦支付成功 那么就需要做这么几件事情：
                //1.更新支付日志的状态修改为已支付 更新支付的时间 更新支付返回的微信的唯一交易流水号
                //2.更新订单表中的支付状态为已支付  还有更新支付时间
                //3.清除缓存中的payLog日志

                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));

                result=new  Result(true, "支付成功");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if(count>=4){
                result = new Result(false,"二维码超时");
                break;
            }
        }
        return result;
    }

}
