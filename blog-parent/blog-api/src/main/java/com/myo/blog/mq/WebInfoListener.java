package com.myo.blog.mq;

import com.myo.blog.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebInfoListener {

    private final RedisTemplate<String, String> redisTemplate;

    @RabbitListener(queues = RabbitConfig.WEB_INFO_QUEUE)
    public void updateWebInfoCache(String msg) {
        log.info("接收到网站配置更新消息，准备清理 Redis 缓存: {}", msg);
        // 这里的 webInfo* 对应 Controller 中 @Cache 注解的 name 属性
        // acheAspect 生成的 key 会以该 name 为前缀
        Set<String> keys = redisTemplate.keys("webInfo*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("网站配置 Redis 缓存清理完成，共清理了 {} 条缓存", keys.size());
        }
    }
}