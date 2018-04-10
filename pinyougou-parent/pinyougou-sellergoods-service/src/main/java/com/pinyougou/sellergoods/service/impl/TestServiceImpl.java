package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.sellergoods.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.sellergoods.service.impl
 * @company www.itheima.com
 */
@Service
public class TestServiceImpl implements TestService {
    @Override
    public String sayhello() {
        return "hello";
    }
}
