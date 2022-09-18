package com.atguigu.gmall.payment.service;

public interface AlipayService {
    // 生成二维码！
    String createAliPay(Long orderId);
}
