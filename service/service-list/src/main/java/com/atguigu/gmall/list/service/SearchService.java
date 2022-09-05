package com.atguigu.gmall.list.service;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/05 20:45
 * @FileName: SearchService
 */
public interface SearchService {
    // 上架
    void upperGoods(Long skuId);
    // 下架
    void lowerGoods(Long skuId);
}
