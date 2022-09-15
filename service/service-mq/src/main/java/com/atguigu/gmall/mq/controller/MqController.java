package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("sendMsg")
    public Result sendMsg() {
        rabbitService.sendMsg("exchange.confirm", "routing.confirm", "天王盖地虎，宝塔镇河妖！");
        return Result.ok();
    }
}
