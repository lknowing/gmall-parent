package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 13:24
 * @FileName: OrderDegradeFeignClient
 */
@Component
public class OrderDegradeFeignClient implements OrderFeignClient {
    @Override
    public Result trade() {
        return null;
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return null;
    }

    @Override
    public Long submitSeckillOrder(OrderInfo orderInfo) {
        return null;
    }
}
