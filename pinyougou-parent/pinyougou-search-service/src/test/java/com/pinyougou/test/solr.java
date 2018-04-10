package com.pinyougou.test;

import com.pinyougou.search.service.ItemSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.test
 * @company www.itheima.com
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring/*.xml")
public class solr {

    @Autowired
    private ItemSearchService itemSearchService;

    @Test
    public void testsearch(){
        Map map = new HashMap();
        map.put("keywords","三星");
        Map<String, Object> search = itemSearchService.search(map);


    }
}
