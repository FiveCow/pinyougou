package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.service.impl
 * @company www.itheima.com
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {


    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        String keywords = (String) searchMap.get("keywords");
        //将中间的空格替换
        searchMap.put("keywords", keywords.replace(" ", ""));


        Map<String, Object> resultmap = new HashMap<>();

        //获取高亮
        Map map = searchHightingList(searchMap);
        resultmap.putAll(map);

        //索引库中分组查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);

        resultmap.put("categoryList", categoryList);


        //判断如果页面选中了商品分类 那么规格和品牌应当根据商品分类来查询
        Object category = searchMap.get("category");

        if (category != null && !"".equals(category)) {
            //有值
            String categorystr = (String) category;
            resultmap.putAll(searchBrandAndSpecList(categorystr));
        } else {
            //查询品牌和规格 查询第一个分类的品牌和规格
            if (categoryList != null && categoryList.size() > 0) {
                resultmap.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return resultmap;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        //long也可以吗
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    private Map searchHightingList(Map searchMap) {

        Map map = new HashMap();
        //高亮查询主查询条件
        HighlightQuery query = new SimpleHighlightQuery();

        HighlightOptions options = new HighlightOptions();
        //关键字搜索
        options.addField("item_title");
        options.setSimplePrefix("<em style='color:red'>");
        options.setSimplePostfix("</em>");
        query.setHighlightOptions(options);
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //添加过滤条件 商品分类
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery query1 = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            query1.addCriteria(criteria1);
            query.addFilterQuery(query1);
        }

        //添加过滤条件 品牌
        if (!"".equals(searchMap.get("brand"))) {
            FilterQuery query1 = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            query1.addCriteria(criteria1);
            query.addFilterQuery(query1);
        }

        //添加过滤条件 规格
        if (searchMap.get("spec") != null) {
            Map<String, Object> spcMap = (Map<String, Object>) searchMap.get("spec");
            for (String key : spcMap.keySet()) {
                FilterQuery query2 = new SimpleFilterQuery();//查询对象
                Criteria criteria2 = new Criteria("item_spc_" + key).is(spcMap.get(key));
                query.addFilterQuery(query2);
            }
        }

        //添加价格区间过滤
        if (searchMap.get("price") != null && !"".equals(searchMap.get("price"))) {
            FilterQuery query3 = new SimpleFilterQuery();
            String pricestr = (String) searchMap.get("price");
            String[] split = pricestr.split("-");
            boolean flag = true;
            Criteria criteria3 = null;
            if (!split[1].equals("*")) {
                criteria3 = new Criteria("item_price").between(split[0], split[1], true, true);
            } else {
                //为了避免报错
                criteria3 = new Criteria("item_price").greaterThanEqual(split[0]);
            }
            query3.addCriteria(criteria3);
            query.addFilterQuery(query3);
        }

        //排序过滤  根据传递过来的域的字段和要排序的类型 来动态的排序


        String sortType = (String) searchMap.get("sort");//ASC DESC

        String sortField = (String) searchMap.get("sortField");// price category


        if (sortType != null && sortType != "") {
            Sort sort = null;
            if (sortType.equals("ASC")) {
                sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            } else if (sortType.equals("DESC")) {
                sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }


        //添加分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        //设置开始分页的位置
        if (pageNo == null) {
            pageNo = 1;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        //设置每页显示的行数

        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);

        //查询完成之后需要 返回相关分页的信息给前端

        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<HighlightEntry<TbItem>> highlighted = tbItems.getHighlighted();
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {

            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
            //只有一个域获取第一个域  如果有多个，则获取到的是对应的下标对应的值
//            for (HighlightEntry.Highlight highlight : highlights) {//只有一个域
//                System.out.println(highlight.getSnipplets());
//            }
            if (highlights != null && highlights.size() > 0 && highlights.get(0).getSnipplets() != null && highlights.get(0).getSnipplets().size() > 0) {
                TbItem entity = tbItemHighlightEntry.getEntity();
                entity.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }

        List<TbItem> content = tbItems.getContent();
        map.put("rows", content);

        map.put("totalPages", tbItems.getTotalPages());//设置总页数
        map.put("total", tbItems.getTotalElements());//设置总记录数
        return map;
    }

    //搜索商品分类 这个分类是根据搜索到的数据来查询出来的 是从索引库中查出来的

    private List<String> searchCategoryList(Map searchMap) {
        List<String> categoryList = new ArrayList<>();
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);


        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        GroupPage<TbItem> tbItemsObject = solrTemplate.queryForGroupPage(query, TbItem.class);

        GroupResult<TbItem> categoryResult = tbItemsObject.getGroupResult("item_category");

        Page<GroupEntry<TbItem>> groupEntries = categoryResult.getGroupEntries();

        List<GroupEntry<TbItem>> content = groupEntries.getContent();

        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            Page<TbItem> result = tbItemGroupEntry.getResult();
            // System.out.println(result.getTotalElements()+">>>"+result.getContent());//获取组的名称以及每组对应的查询到的结果集合
            String groupValue = tbItemGroupEntry.getGroupValue();
            //System.out.println("值对象"+groupValue);
            categoryList.add(groupValue);
            System.out.println("hhhh");
        }

        return categoryList;
    }


    @Autowired
    private RedisTemplate redisTemplate;

    //根据分类名称查询品牌列表 和规格列表  这个不需要通过solr来实现
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        if (typeId != null) {
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

    public static void main(String[] args) {
        Object oj = null;

        Integer a = (Integer) null;
        if (a == null) {
            System.out.println("asdfa");
        }
    }
}