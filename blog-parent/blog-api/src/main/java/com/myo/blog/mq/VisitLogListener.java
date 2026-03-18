package com.myo.blog.mq;

import com.alibaba.fastjson.JSON;
import com.myo.blog.config.RabbitConfig;
import com.myo.blog.dao.mapper.VisitLogMapper;
import com.myo.blog.dao.pojo.VisitLog;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitLogListener {

    private final VisitLogMapper visitLogMapper;

    @RabbitListener(queues = RabbitConfig.VISIT_LOG_QUEUE)
    public void consumeVisitLog(Message message, Channel channel) throws IOException {
        // 获取这条消息在队列中的唯一投递序号
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            String msgBody = new String(message.getBody());
            VisitLog visitLog = JSON.parseObject(msgBody, VisitLog.class);

            if (visitLog != null) {
                visitLogMapper.insert(visitLog);
            }

            // 数据库 insert 彻底成功后，手动向 MQ 发送确认收到(ACK)的信号
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("消费访问日志异常，准备将消息退回队列重试: {}", e.getMessage());

            // 如果数据库操作失败，发送拒绝(NACK)信号，并且让消息重新回到队列排队，确保明细绝对不丢
            // basicNack 参数解释：投递序号，是否批量拒绝，是否重新入队(true)
            channel.basicNack(deliveryTag, false, true);
        }
    }
}