package com.pinyougou;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-*.xml")
public class AppTest
        extends TestCase {

    @Autowired
    private SolrTemplate solrTemplate;

    @org.junit.Test
    public void testAdd(){
        System.out.println("1");

        TbItem item=new TbItem();
        item.setId(1L);
        item.setBrand("华为");
        item.setCategory("手机");
        item.setGoodsId(1L);
        item.setSeller("华为2号专卖店");
        item.setTitle("华为Mate9");
        item.setPrice(new BigDecimal(2000));
        solrTemplate.saveBean(item);
        solrTemplate.commit();
        //solrTemplat.
    }

    @org.junit.Test
    public void testDelete(){

        solrTemplate.deleteById(1+"");
        solrTemplate.commit();
        //solrTemplat.
    }

    @org.junit.Test
    public void testinsertBatch(){
        List<TbItem> list=new ArrayList();

        for(int i=0;i<100;i++){
            TbItem item=new TbItem();
            item.setId(i+1L);
            item.setBrand("华为");
            item.setCategory("手机");
            item.setGoodsId(1L);
            item.setSeller("华为2号专卖店");
            item.setTitle("华为Mate"+i);
            item.setPrice(new BigDecimal(2000+i));
            list.add(item);
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();;
    }

    @org.junit.Test
    public void testQuery(){
        Query query = new SimpleQuery("*:*");
        query.setOffset(0);
        query.setRows(10);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        System.out.println(">>>>>"+tbItems.getNumberOfElements());
        System.out.println(tbItems.getTotalElements());
        List<TbItem> content = tbItems.getContent();

        System.out.println("-----------------------------");
        for (TbItem item : content) {
            System.out.println(item.getTitle());
        }


    }

    @Autowired
    private TbItemMapper mapper;
    @org.junit.Test
    public void testSele(){
        System.out.println(mapper.selectByExample(null).size());
    }





}
