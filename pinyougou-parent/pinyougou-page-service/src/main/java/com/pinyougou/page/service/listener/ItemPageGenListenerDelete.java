package com.pinyougou.page.service.listener;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.page.service.listener
 * @company www.itheima.com
 */
public class ItemPageGenListenerDelete implements MessageListener {

    @Autowired
    private ItemPageService service;
    @Override
    public void onMessage(Message message) {
        //获取消息 删除
        try {
            ObjectMessage objectMessage = (ObjectMessage)message;
            Long[] ids = (Long[]) objectMessage.getObject();
            service.deleteItemHtml(ids);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
