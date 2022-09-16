package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/15 18:15
 * @FileName: MqController
 */
@RestController
@RequestMapping("mq")
public class MqController {
    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendMsg")
    public Result sendMsg() {
        rabbitService.sendMsg("exchange.confirm", "routing.confirm", "天王盖地虎，宝塔镇河妖！");
        return Result.ok();
    }

    @GetMapping("sendDeadLetter")
    public Result sendDeadLetter() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()) + " Delay sent.");
        rabbitService.sendMsg(DeadLetterMqConfig.exchange_dead, DeadLetterMqConfig.routing_dead_1, "等一下：天王盖地虎，宝塔镇河妖！");
        return Result.ok();
    }

    @GetMapping("sendDelayMsg")
    public Result sendDelayMsg() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //        rabbitTemplate.convertAndSend(DelayedMqConfig.exchange_delay,
        //                DelayedMqConfig.routing_delay,
        //                "atguigu",
        //                (message) -> {
        //                    System.out.println(sdf.format(new Date()) + " Delay sent.");
        //                    message.getMessageProperties().setDelay(10000);
        //                    return message;
        //                });
        rabbitService.sendDelayMsg(DelayedMqConfig.exchange_delay, DelayedMqConfig.routing_delay, "atguigu", 3);
        return Result.ok();
    }
}
