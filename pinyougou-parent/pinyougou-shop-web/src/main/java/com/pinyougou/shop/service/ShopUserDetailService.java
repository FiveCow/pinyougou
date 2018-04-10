package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.shop.service
 * @company www.itheima.com
 */
public class ShopUserDetailService implements UserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(ShopUserDetailService.class);

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //从数据库中获取用户的名称和密码与传递过来的数据做匹配匹配成功就返回成功（框架自带的）
        System.out.println("用户名为："+username);
        //获取数据即可
        //获取数据库中用户名为username的用户的数据的密码

        TbSeller tbSeller = sellerService.findOne(username);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+tbSeller);
        if(tbSeller==null){
            return  null;
        }

        //判断用户是否已经被审核
        String status = tbSeller.getStatus();
        if(!"1".equals(status)){//如果不是审核的状态就返回空
            return null;
        }
        logger.debug("状态："+status);
        return new User(username,tbSeller.getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_SELLER"));
    }
}
