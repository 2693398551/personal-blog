package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myo.blog.config.RabbitConfig;
import com.myo.blog.dao.mapper.WebInformationMapper;
import com.myo.blog.dao.pojo.WebInformation;
import com.myo.blog.entity.Result;
import com.myo.blog.service.WebInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebInformationServiceImpl implements WebInformationService {

    private final WebInformationMapper webInformationMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Result getWebInfo() {
        // 因为全局配置通常只有一条数据，直接查询 ID 为 1 的记录即可
        WebInformation webInfo = webInformationMapper.selectById(1);
        if (webInfo == null) {
            return Result.fail(500, "网站配置信息不存在，请检查数据库");
        }
        return Result.success(webInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateWebInfo(WebInformation webInformation) {
        // 强制设置 ID 为 1，防止新增多条配置数据
        webInformation.setId(1);
        int rows = webInformationMapper.updateById(webInformation);
        if (rows > 0) {
            // 发送消息到 RabbitMQ，内容可以随便传一个标志位，比如 update
            rabbitTemplate.convertAndSend(RabbitConfig.WEB_INFO_EXCHANGE, RabbitConfig.WEB_INFO_ROUTING_KEY, "update");
            return Result.success("更新网站配置成功");
        }
        return Result.fail(500, "更新网站配置失败");
    }
}