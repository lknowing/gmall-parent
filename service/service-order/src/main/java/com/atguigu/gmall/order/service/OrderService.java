package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 13:44
 * @FileName: OrderSerice
 */
public interface OrderService extends IService<OrderInfo> {
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

    /**
     * 查看我的订单
     *
     * @param orderInfoPage
     * @param userId
     * @return
     */
    IPage<OrderInfo> getOrderPageList(Page<OrderInfo> orderInfoPage, String userId);

    /**
     * 取消订单
     *
     * @param orderId
     */
    void execExpiredOrder(Long orderId);

    void execExpiredOrder(Long orderId,String flag);

    /**
     * 根据订单Id 查询订单信息
     *
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);

    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    void sendOrderMsg(Long orderId);

    Map wareJson(OrderInfo orderInfo);

    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
