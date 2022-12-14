package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/16 16:21
 * @FileName: DelayedMqConfig
 */
@Configuration
public class DelayedMqConfig {
    public static final String exchange_delay = "exchange.delay";
    public static final String routing_delay = "routing.delay";
    public static final String queue_delay_1 = "queue.delay.1";

    @Bean
    public Queue delayQueue() {
        // 不需要在队列中设置延迟时间
        return new Queue(queue_delay_1, true, false, false);
    }

    @Bean
    public CustomExchange delayExchange() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x-delayed-type", "direct");
        return new CustomExchange(exchange_delay, "x-delayed-message", true, false, map);
    }

    // 绑定关系
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(routing_delay).noargs();
    }
}
