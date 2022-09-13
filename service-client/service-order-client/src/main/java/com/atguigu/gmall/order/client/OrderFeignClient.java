package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;

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
    @GetMapping("auth/trade")
    Result trade();
}
