package com.atguigu.gmall.common.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Boolean sendMsg(String exchange, String routingKey, Object msg) {
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
        return true;
    }
}
