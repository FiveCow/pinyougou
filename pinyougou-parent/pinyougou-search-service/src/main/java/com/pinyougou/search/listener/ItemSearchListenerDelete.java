package com.pinyougou.search.listener;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.listener
 * @company www.itheima.com
 */
public class ItemSearchListenerDelete implements MessageListener {


    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        //获取到对象
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            //获取到之后，调用搜索服务中的删除索引库的服务
            List<Long> longs = Arrays.asList(ids);
            itemSearchService.deleteByGoodsIds(longs);

            System.out.println("成功删除索引库中的记录");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
