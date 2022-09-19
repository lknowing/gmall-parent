package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/17 10:52
 * @FileName: PaymentService
 */
public interface PaymentService {
    /**
     * 保存交易记录
     *
     * @param orderInfo
     * @param paymentType
     */
    void savePaymentInfo(OrderInfo orderInfo, String paymentType);

    /**
     * 获取交易记录
     *
     * @param outTradeNo
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(String outTradeNo, String paymentType);

    /**
     * 更新交易状态
     *
     * @param outTradeNo
     * @param paymentType
     * @param paramsMap
     */
    void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap);

    void updatePaymentInfoStatus(String outTradeNo, String paymentType, PaymentInfo paymentInfo);

    void closePayment(Long orderId);
}
