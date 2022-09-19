package com.atguigu.gmall.payment.service;

public interface AlipayService {
    // 生成二维码！
    String createAliPay(Long orderId);

    /**
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

    Boolean closePay(Long orderId);

    Boolean checkPayment(Long orderId);
}
