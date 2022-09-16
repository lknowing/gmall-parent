package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/15 18:19
 * @FileName: ConfirmReceiver
 */
@Component
public class ConfirmReceiver {
    @Autowired
    private RedisTemplate redisTemplate;

    // 使用注解进行监听：将交换机及队列都会初始化！
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.confirm", durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = "exchange.confirm"),
            key = {"routing.confirm"}
    ))
    public void getMsg(String msg, Message message, Channel channel) {
        // 如果异常 nack！
        try {
            System.out.println("接收到的消息：\t" + msg);
            System.out.println("接收到的消息：\t" + new String(message.getBody()));
        } catch (Exception e) {
            // 捕获异常
            // 第三个参数表示是否重回队列！
            // 网络异常：channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            // 业务异常：例如数据传输错误，重试没用！无限循环！设置重试次数！次数到了，需要做消息表！将这个消息直接写入表中！
            // 只要有异常！直接进入消息记录表！
            // channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            e.printStackTrace();
        }
        // 签收消息，有异常也进行确认，保证消息队列的消息出队，消费成功与否不管！
        // 第一个参数表示消息标签； 第二个参数表示是否批量签收！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void getMsg2(String msg, Message message, Channel channel) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("Receive queue_dead_2: " + sdf.format(new Date()) + " Delay rece." + msg);
        } catch (Exception e) {
            // 只要有异常！直接进入消息记录表！
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void getMsg3(String msg, Message message, Channel channel) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent("lock", "0", 1, TimeUnit.MINUTES);
        if (!result) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("Receive queue_dead_2: " + sdf.format(new Date()) + " Delay rece." + msg);
        } catch (Exception e) {
            redisTemplate.delete("lock");
            e.printStackTrace();
        }
        // 只要有异常！直接进入消息记录表！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
