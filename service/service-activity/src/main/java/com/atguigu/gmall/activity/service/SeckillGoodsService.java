package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/20 14:09
 * @FileName: SeckillGoodsService
 */
public interface SeckillGoodsService {
    /**
     * 查询所有秒杀列表
     *
     * @return
     */
    List<SeckillGoods> findAll();

    /**
     * 根据skuId查看商品详情
     *
     * @param skuId
     * @return
     */
    SeckillGoods getSeckillGoods(Long skuId);

    /**
     * 预下单
     *
     * @param skuId
     * @param skuIdStr
     * @param userId
     * @return
     */
    Result seckillOrder(Long skuId, String skuIdStr, String userId);

    /**
     * 真正的预下单
     *
     * @param userRecode
     */
    void seckillOrderUser(UserRecode userRecode);

    void updateStockCount(Long skuId);

    /**
     * 检查订单状态
     *
     * @param skuId
     * @param userId
     * @return
     */
    Result checkOrder(Long skuId, String userId);
}
