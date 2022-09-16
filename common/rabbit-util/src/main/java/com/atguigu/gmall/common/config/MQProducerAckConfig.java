package com.atguigu.gmall.common.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.model.GmallCorrelationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * title:监听消息配置类
 *
 * @Author xu
 * @Date 2022/09/16 09:10
 * @FileName: MQProducerAckConfig
 */
@Configuration
@Slf4j
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    // 修饰一个非静态的void（）方法,在服务器加载Servlet的时候运行，并且只会被服务器执行一次，在构造函数之后执行，init（）方法之前执行。
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    // 交换机
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息到达交换机！");
            System.out.println(cause);
        } else {
            log.error("消息没有到达交换机！");
            System.out.println(cause);

            // 调用重试方法
            this.retrySendMsg(correlationData);
        }
    }

    // 队列 -- 消息没到队列才会走这个方法！
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);

        String correlationDataId = (String) message.getMessageProperties().getHeaders().get("spring_returned_message_correlation");
        String strJson = (String) redisTemplate.opsForValue().get(correlationDataId);
        GmallCorrelationData gmallCorrelationData = JSON.parseObject(strJson, GmallCorrelationData.class);
        // 调用重试方法
        this.retrySendMsg(gmallCorrelationData);
    }

    private void retrySendMsg(CorrelationData correlationData) {
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;

        int retryCount = gmallCorrelationData.getRetryCount();
        System.out.println("重试次数 = " + retryCount);

        if (retryCount >= 2) { // 重试2次，总共发送3次消息
            // 不重试，直接写入消息表！
            log.error("重试次数已到，数据持久化！");
        } else {
            // 重试：再次发生消息！
            // 已重试次数更新
            retryCount++;
            gmallCorrelationData.setRetryCount(retryCount);
            // 更新缓存中的已重试次数
            redisTemplate.opsForValue().set(gmallCorrelationData.getId(),
                    JSON.toJSONString(gmallCorrelationData), 2, TimeUnit.MINUTES);
            // 调用发送消息方法--普通方法
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),
                    gmallCorrelationData.getRoutingKey(),
                    gmallCorrelationData.getMessage(),
                    gmallCorrelationData);
        }
    }
}
