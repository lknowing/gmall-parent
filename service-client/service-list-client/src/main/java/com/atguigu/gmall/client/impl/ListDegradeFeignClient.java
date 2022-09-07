package com.atguigu.gmall.client.impl;

import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.stereotype.Component;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/06 20:45
 * @FileName: ListDegradeFeignClient
 */
@Component
public class ListDegradeFeignClient implements ListFeignClient {
    @Override
    public Result search(SearchParam searchParam) {
        return null;
    }

    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }

    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }
}
