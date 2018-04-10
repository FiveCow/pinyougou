package com.pinyougou.pay.service;

import java.util.Map;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.pay.service
 * @company www.itheima.com
 */
public interface WeixinPayService {
    /**
     *
     * @param out_trade_no  订单号
     * @param total_fee  总金额
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);
    public Map queryPayStatus(String out_trade_no);
}
