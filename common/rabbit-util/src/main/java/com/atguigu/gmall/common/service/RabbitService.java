package com.atguigu.gmall.common.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/15 16:22
 * @FileName: RabbitService
 */
@Service
public class RabbitService {

    // RabbitTemplate 使用的是模板设计模式
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    public Boolean sendMsg(String exchange, String routingKey, Object msg) {
        // 发送之前将交换机，路由键，消息内容，重发次数等 属性封装到实体类中
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String correlationDataId = UUID.randomUUID().toString();
        gmallCorrelationData.setId(correlationDataId);
        gmallCorrelationData.setExchange(exchange);
        gmallCorrelationData.setRoutingKey(routingKey);
        gmallCorrelationData.setMessage(msg);
        // 发送消息的时候，将对象放入缓存
        redisTemplate.opsForValue().set(correlationDataId,
                JSON.toJSONString(gmallCorrelationData), 2, TimeUnit.MINUTES);
        // 调用发送消息方法
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, gmallCorrelationData);
        return true;
    }
}
