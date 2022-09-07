package com.atguigu.gmall.client;

import com.atguigu.gmall.client.impl.ListDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
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
 * @Date 2022/09/06 20:43
 * @FileName: ListFeignClient
 */
@FeignClient(value = "service-list", fallback = ListDegradeFeignClient.class)
@Repository
public interface ListFeignClient {

    @GetMapping("api/list/inner/incrHotScore/{skuId}")
    Result incrHotScore(@PathVariable("skuId") Long skuId);

    /**
     * 搜索商品
     *
     * @param searchParam
     * @return
     */
    @PostMapping("/api/list")
    Result search(@RequestBody SearchParam searchParam);

    /**
     * 上架商品
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId);

    /**
     * 下架商品
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId);

}
