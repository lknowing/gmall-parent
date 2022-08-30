package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/30 17:31
 * @FileName: ItemService
 */
public interface ItemService {
    /**
     * 根据 skuId获取商品详情
     *
     * @param skuId
     * @return
     */
    Map<String, Object> getItem(Long skuId);
}
