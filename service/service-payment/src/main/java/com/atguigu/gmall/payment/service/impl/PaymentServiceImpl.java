package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/17 10:54
 * @FileName: PaymentServiceImpl
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public PaymentInfo getPaymentInfo(String outTradeNo, String paymentType) {
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no", outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type", paymentType);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQueryWrapper);
        if (paymentInfo == null) {
            return null;
        }
        return paymentInfo;
    }

    @Override
    public void paySuccess(String outTradeNo, String paymentType, Map<String, String> paramsMap) {
        PaymentInfo paymentInfoQuery = this.getPaymentInfo(outTradeNo, paymentType);
        if (paymentInfoQuery == null) {
            return;
        }
        try {
            PaymentInfo paymentInfo = new PaymentInfo();
            // trade_no payment_status callback_time callback_content
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(paramsMap.toString());

            this.updatePaymentInfoStatus(outTradeNo, paymentType, paymentInfo);
        } catch (Exception e) {
            redisTemplate.delete(paramsMap.get("notifyId"));
            e.printStackTrace();
        }
        // 发送消息通知订单实现减库存操作
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                MqConst.ROUTING_PAYMENT_PAY, paymentInfoQuery.getOrderId());
    }

    /**
     * 更新交易状态
     *
     * @param outTradeNo
     * @param paymentType
     * @param paymentInfo
     */
    public void updatePaymentInfoStatus(String outTradeNo, String paymentType, PaymentInfo paymentInfo) {
        // 设置更新条件
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("out_trade_no", outTradeNo);
        paymentInfoQueryWrapper.eq("payment_type", paymentType);
        // 更新数据
        paymentInfoMapper.update(paymentInfo, paymentInfoQueryWrapper);
    }

    @Override
    public void closePayment(Long orderId) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.CLOSED.name());
        paymentInfo.setUpdateTime(new Date());
        UpdateWrapper<PaymentInfo> paymentInfoUpdateWrapper = new UpdateWrapper<>();
        paymentInfoUpdateWrapper.eq("order_id", orderId);
        this.paymentInfoMapper.update(paymentInfo, paymentInfoUpdateWrapper);
    }

    @Override
    public void savePaymentInfo(OrderInfo orderInfo, String paymentType) {
        QueryWrapper<PaymentInfo> paymentInfoQueryWrapper = new QueryWrapper<>();
        paymentInfoQueryWrapper.eq("order_id", orderInfo.getId());
        paymentInfoQueryWrapper.eq("payment_type", paymentType);
        Integer count = paymentInfoMapper.selectCount(paymentInfoQueryWrapper);
        if (count > 0) return;
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setUserId(orderInfo.getUserId());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfoMapper.insert(paymentInfo);
    }
}
