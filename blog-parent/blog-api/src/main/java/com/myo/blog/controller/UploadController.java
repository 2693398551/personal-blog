package com.myo.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.dao.mapper.ArticleMapper;
import com.myo.blog.dao.pojo.Article;
import com.myo.blog.utils.QiniuUtils;
import com.myo.blog.entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {

    @Autowired
    private QiniuUtils qiniuUtils;

    @PostMapping
    public Result upload(@RequestParam("image") MultipartFile file){
        //原始文件名称 比如 aa.png
        String originalFilename = file.getOriginalFilename();
        //唯一的文件名称
        String fileName ="article_body/"+UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //上传文件
        // 降低自身应用服务器的带宽消耗
        //System.out.println("唯一的文件名称1:"+fileName);
        boolean upload = qiniuUtils.upload(file, fileName);
        if (upload){
            return Result.success(QiniuUtils.url + fileName);
        }
        return Result.fail(20001,"上传失败");
    }
    @Autowired
    private ArticleMapper articleMapper;

    @PostMapping("file")
    public Result upcover(@RequestParam("image")  MultipartFile file){
        //原始文件名称 比如 aa.png
        String originalFilename = file.getOriginalFilename();
        //文件名称
        /*LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Article::getId);
        queryWrapper.last("limit 1");
        queryWrapper.orderByDesc(Article::getCreateDate);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        int id=Integer.parseInt(String.valueOf(articles.get(0).getId()));
        String cover="cover/cover"+id;
        System.out.println("cover值："+id);*/
        //UUID.randomUUID().toString()文件名称变成cover.jpg
        String fileName ="cover/"+ UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //上传文件
        // 降低自身应用服务器的带宽消耗

        boolean upload = qiniuUtils.upload(file, fileName);

        if (upload){
            return Result.success(QiniuUtils.url + fileName);
        }
        return Result.fail(20001,"上传失败");
    }
    @PostMapping("file2")
    public Result upcover2( MultipartFile file){
        String originalFilename = file.getOriginalFilename();

        String fileName ="cover/"+ UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //上传文件
        // 降低自身应用服务器的带宽消耗


        boolean upload = qiniuUtils.upload(file, fileName);

        if (upload){
            return Result.success(QiniuUtils.url + fileName);
        }
        return Result.fail(20001,"上传失败");
    }

    @PostMapping("qrcode")
    public Result qrcode(@RequestParam("image") MultipartFile file){
        String originalFilename = file.getOriginalFilename();

        String fileName ="QR_code/"+ UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //上传文件
        // 降低自身应用服务器的带宽消耗

        boolean upload = qiniuUtils.upload(file, fileName);

        if (upload){
            return Result.success(QiniuUtils.url + fileName);
        }
        return Result.fail(20001,"上传失败");
    }
    //修改头像
    @PostMapping("upAvatar")
    public Result upcover4(@RequestParam("image") MultipartFile file){
        String originalFilename = file.getOriginalFilename();

        String fileName ="avatar/"+ UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");


        //上传文件
        // 降低自身应用服务器的带宽消耗

        boolean upload = qiniuUtils.upload(file, fileName);

        if (upload){
            return Result.success(QiniuUtils.url + fileName);
        }
        return Result.fail(20001,"上传失败");
        //return Result.success(fileName);
    }
}
