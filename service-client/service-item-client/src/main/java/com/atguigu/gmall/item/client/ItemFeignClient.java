package com.atguigu.gmall.item.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/31 17:33
 * @FileName: ItemFeignClient
 */
@FeignClient(value = "service-item",fallback = ItemDegradeFeignClient.class)
@Repository
public interface ItemFeignClient {
    @GetMapping("api/item/{skuId}")
    public Result getItem(@PathVariable Long skuId);
}
