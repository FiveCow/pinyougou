package com.pinyougou.solrutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.solruti
 * @company www.itheima.com
 */
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemData() throws Exception{
        ObjectMapper mapper = new ObjectMapper();

        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//已审核的商品数据
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        for (TbItem tbItem : tbItems) {
            Map map = mapper.readValue(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
            System.out.println(map.keySet());
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
        SolrUtil bean = context.getBean(SolrUtil.class);
        bean.importItemData();
    }

}
