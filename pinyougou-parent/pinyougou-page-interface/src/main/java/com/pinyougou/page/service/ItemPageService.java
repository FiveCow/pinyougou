package com.pinyougou.page.service;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.page.service
 * @company www.itheima.com
 */
public interface ItemPageService {

    /**
     * 生成(SKU)商品详情页面
     * @param goodsId   SPU的id
     * @return
     */
    public boolean genItemHtml(Long goodsId);


    //批量删除静态网页
    public boolean deleteItemHtml(Long[] goodsIds);
}
