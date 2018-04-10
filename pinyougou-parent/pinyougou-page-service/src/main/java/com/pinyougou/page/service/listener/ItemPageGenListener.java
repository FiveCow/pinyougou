package com.pinyougou.page.service.listener;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.page.service.listener
 * @company www.itheima.com
 */
public class ItemPageGenListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        //获取对象

        try {
            ObjectMessage objectMessage =(ObjectMessage)message;
            Long[] ids = (Long[])objectMessage.getObject();
            for (Long id : ids) {
                itemPageService.genItemHtml(id);
            }
            //生成成功。

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
