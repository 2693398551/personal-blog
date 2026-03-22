package com.myo.blog.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 配置类，定义交换机、队列、路由键
@Configuration
public class RabbitConfig {

    // 定义交换机名称
    public static final String BLOG_EXCHANGE = "blog_topic_exchange";
    // 定义队列名称
    public static final String ARTICLE_QUEUE = "article_cache_queue";
    // 定义路由键 (Routing Key)
    public static final String ROUTING_KEY = "article.#";

    // 定义网站配置的交换机、队列和路由键
    public static final String WEB_INFO_EXCHANGE = "web_info_exchange";
    public static final String WEB_INFO_QUEUE = "web_info_queue";
    public static final String WEB_INFO_ROUTING_KEY = "web_info.#";


    // 定义流量日志的交换机名称
    public static final String VISIT_LOG_EXCHANGE = "visit_log_exchange";
    // 定义流量日志的队列名称
    public static final String VISIT_LOG_QUEUE = "visit_log_queue";
    // 定义流量日志的路由键
    public static final String VISIT_LOG_ROUTING_KEY = "visit_log.insert";

    //===流量日志队列配置===
    // 声明流量日志的直连交换机
    @Bean
    public DirectExchange visitLogExchange() {
        return new DirectExchange(VISIT_LOG_EXCHANGE, true, false);
    }

    // 声明流量日志的持久化队列
    @Bean
    public Queue visitLogQueue() {
        return new Queue(VISIT_LOG_QUEUE, true);
    }

    // 将流量日志的队列与交换机进行绑定
    @Bean
    public Binding visitLogBinding() {
        return BindingBuilder.bind(visitLogQueue()).to(visitLogExchange()).with(VISIT_LOG_ROUTING_KEY);
    }
    //===流量日志队列配置end===


    //===网站配置队列配置===
    @Bean
    public TopicExchange webInfoExchange() {
        return new TopicExchange(WEB_INFO_EXCHANGE, true, false);
    }

    @Bean
    public Queue webInfoQueue() {
        return new Queue(WEB_INFO_QUEUE, true);
    }


    @Bean
    public Binding webInfoBinding() {
        return BindingBuilder.bind(webInfoQueue()).to(webInfoExchange()).with(WEB_INFO_ROUTING_KEY);
    }
    //===网站配置队列绑定end===


    //===文章队列配置===
    // 1. 声明交换机 (Topic类型，灵活)
    @Bean("blogExchange")
    public Exchange blogExchange() {
        return ExchangeBuilder.topicExchange(BLOG_EXCHANGE).durable(true).build();
    }

    // 2. 声明队列
    @Bean("articleQueue")
    public Queue articleQueue() {
        return QueueBuilder.durable(ARTICLE_QUEUE).build();
    }

    // 3. 绑定队列到交换机
    @Bean
    public Binding bindArticleQueue(Exchange blogExchange, Queue articleQueue) {
        return BindingBuilder.bind(articleQueue).to(blogExchange).with(ROUTING_KEY).noargs();
    }
    //===文章队列配置end===
}