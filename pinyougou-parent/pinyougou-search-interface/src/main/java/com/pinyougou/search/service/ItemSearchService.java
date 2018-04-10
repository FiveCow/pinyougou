package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * 商品搜索
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.service
 * @company www.itheima.com
 */
public interface ItemSearchService {

    /**
     *
     * @param searchMap 搜索条件 包括很多的搜索的条件
     * @return 结果集 key 是对应的相关模块的key  value是相关模块查询的结果集合
     */
    public Map<String,Object> search(Map searchMap);


    public void importList(List list);

    //删除索引库中的数据
    public void deleteByGoodsIds(List goodsIdList);





}
