package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AlipayService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/18 08:56
 * @FileName: AlipayController
 */
@Controller
@RequestMapping("api/payment/alipay")
public class AlipayController {
    @Autowired
    private AlipayService alipayService;

    @Autowired
    private PaymentService paymentService;

    @Value("${app_id}")
    private String app_id;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("submit/{orderId}")
    @ResponseBody
    public String aliPay(@PathVariable Long orderId) {
        String form = alipayService.createAliPay(orderId);
        return form;
    }

    @GetMapping("callback/return")
    public String callback() {
        // 重定向到web-all模块的控制器 进行同步回调
        return "redirect:" + AlipayConfig.return_order_url;
    }

    @PostMapping("callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> paramsMap) {
        String outTradeNo = paramsMap.get("out_trade_no");
        String totalAmount = paramsMap.get("total_amount");
        String appId = paramsMap.get("app_id");
        String tradeStatus = paramsMap.get("trade_status");
        String notifyId = paramsMap.get("notify_id");
        // Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramsMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (signVerified) {
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(outTradeNo, PaymentType.ALIPAY.name());
            if (paymentInfo == null || new BigDecimal("0.01").compareTo(new BigDecimal(totalAmount)) != 0
                    || !app_id.equals(appId)) {
                return "failure";
            }
            // setnx key value
            Boolean result = redisTemplate.opsForValue().setIfAbsent(notifyId, "0", 1462, TimeUnit.MINUTES);
            if (!result) {
                return "failure";
            }
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                paymentService.paySuccess(outTradeNo, PaymentType.ALIPAY.name(), paramsMap);
                return "success";
            }
        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }
}
