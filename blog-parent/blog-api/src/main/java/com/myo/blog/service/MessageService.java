package com.myo.blog.service;

import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.MessageParam;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author : myo
 * @create 2023/8/8 15:59
 */
public interface MessageService {
    Result message(MessageParam messageParam);
}
