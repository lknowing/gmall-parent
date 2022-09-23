package com.atguigu.gmall.task.scheduled;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/20 10:39
 * @FileName: ScheduledTask
 */
@Component
@EnableScheduling
@Slf4j
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void importToRedis() {
        this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_1, "hello");
    }

    @Scheduled(cron = "* * 18 * * ?")
    public void clearRedisData() {
        this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_18, "clear");
    }
}
