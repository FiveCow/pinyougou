package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.sellergoods.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.manager.controller
 * @company www.itheima.com
 */
@Controller
public class TestController {

    @Reference
    private TestService testService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayhello(){
        return testService.sayhello();
    }
}
