package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.listener
 * @company www.itheima.com
 */
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        //获取manager-web中批量更新的商品列表（JSON字符串）
        try {
            TextMessage textMessage =(TextMessage)message;
            String text = textMessage.getText();
            List<TbItem> lists = JSON.parseArray(text, TbItem.class);

            
            //注意：此时获取的List中的Item 中的规格域 并没有值 需要向其添加数据索引

            for (TbItem item : lists) {
                String spec = item.getSpec();//json字符串
                //转成MAP
                Map map = JSON.parseObject(spec, Map.class);
                //设置回map属性中
                item.setSpecMap(map);

            }
            
            itemSearchService.importList(lists);


        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
