package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.payment.client.PaymentFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/16 18:13
 * @FileName: OrderReceiver
 */
@Component
public class OrderReceiver {
    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentFeignClient paymentFeignClient;

    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_ORDER_CANCEL)
    public void orderCancel(Long orderId, Message message, Channel channel) {
        try {
            if (orderId != null) {
                OrderInfo orderInfo = orderService.getById(orderId);
                if (orderInfo != null && "UNPAID".equals(orderInfo.getOrderStatus()) && "UNPAID".equals(orderInfo.getProcessStatus())) {
                    PaymentInfo paymentInfo = this.paymentFeignClient.getPaymentInfo(orderInfo.getOutTradeNo());
                    if (paymentInfo != null) {
                        Boolean scan = this.paymentFeignClient.checkPayment(orderId);
                        if (scan) {
                            Boolean closePay = this.paymentFeignClient.closePay(orderId);
                            if (closePay) {
                                orderService.execExpiredOrder(orderId, "2");
                            } else {
                                // 表示已经扫码付款了，走异步通知修改状态！
                            }
                        } else {
                            orderService.execExpiredOrder(orderId, "2");
                        }
                    } else {
                        orderService.execExpiredOrder(orderId, "1");
                    }
                }
            }
        } catch (Exception e) {
            //  写入日志...
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY}
    ))
    public void updateOrderStatus(Long orderId, Message message, Channel channel) {
        try {
            if (orderId != null) {
                OrderInfo orderInfo = orderService.getOrderInfo(orderId);
                // 根据业务字段保证不重复消费！
                //                if ("PAID".equals(orderInfo.getOrderStatus())) {
                //                    return;
                //                }
                if ("UNPAID".equals(orderInfo.getOrderStatus())) {
                    orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
                    // 发送消息给库存
                    orderService.sendOrderMsg(orderId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 手动确认！
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_WARE_ORDER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_WARE_ORDER),
            key = {MqConst.ROUTING_WARE_ORDER}
    ))
    public void wareOrder(String strJson, Message message, Channel channel) {
        try {
            if (!StringUtils.isEmpty(strJson)) {
                Map map = JSON.parseObject(strJson, Map.class);
                String orderId = (String) map.get("orderId");
                String status = (String) map.get("status");
                if ("DEDUCTED".equals(status)) {
                    orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.WAITING_DELEVER);
                } else {
                    orderService.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.STOCK_EXCEPTION);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
