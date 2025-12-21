package com.myo.blog.utils;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/9/29 10:33
 */
public class ArticleUtils {

    //文章加密
    public static String keys(int length){
        String content="";
        for (int i = 0; i < length; i++) {
            content=content+"■";
        }
        return content;
    }


}
