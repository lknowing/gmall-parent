package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/31 17:36
 * @FileName: ItemDegradeFeignClient
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
