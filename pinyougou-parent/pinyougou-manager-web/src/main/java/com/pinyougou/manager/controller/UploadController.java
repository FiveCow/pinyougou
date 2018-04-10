package com.pinyougou.manager.controller;

import com.pinyougou.common.FastDFSClient;
import entity.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author ljh
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.shop.controller
 * @company www.itheima.com
 */
@RestController
public class UploadController {

    @RequestMapping("/upload")
    public Result uploadFile(MultipartFile file){//要求页面中的input type 类型中名字要一致
        //第一个：加入file-upload jar包
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            byte[] bytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
            String uploadFilePath = fastDFSClient.uploadFile(bytes, extName);// group1/m00/iuuouo.jpg
            String wholePath = "http://192.168.25.133/"+uploadFilePath;
            return new Result(true,wholePath);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
