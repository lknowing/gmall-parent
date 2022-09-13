package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 13:44
 * @FileName: OrderSerice
 */
public interface OrderService {
    /**
     * 保存订单并返回订单Id
     *
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 生成流水号
     *
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     *
     * @param tradeNo
     * @param userId
     * @return
     */
    Boolean checkTradeNo(String tradeNo, String userId);

    /**
     * 删除缓存流水号
     *
     * @param userId
     */
    void delTradeNo(String userId);

    /**
     * 校验库存系统
     *
     * @param skuId
     * @param num
     * @return
     */
    Boolean checkStock(Long skuId, Integer num);
}
