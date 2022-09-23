package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 13:23
 * @FileName: OrderFeignClient
 */
@FeignClient(value = "service-order", fallback = OrderDegradeFeignClient.class)
@Repository
public interface OrderFeignClient {
    // request是用来获取userId 但是通过Feign远程调用的时候，不会携带头文件的信息
    @GetMapping("api/order/auth/trade")
    Result trade();

    @GetMapping("/api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable Long orderId);

    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitSeckillOrder(@RequestBody OrderInfo orderInfo);
}
