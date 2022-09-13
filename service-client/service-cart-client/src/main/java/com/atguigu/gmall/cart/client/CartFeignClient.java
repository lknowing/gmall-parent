package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.cart.client.impl.CartDegradeFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:49
 * @FileName: CartFeignClient
 */
@FeignClient(value = "service-cart", fallback = CartDegradeFeignClient.class)
@Repository
public interface CartFeignClient {
    @GetMapping("api/cart/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable String userId);
}
