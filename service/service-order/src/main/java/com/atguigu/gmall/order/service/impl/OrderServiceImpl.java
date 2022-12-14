package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 13:45
 * @FileName: OrderServiceImpl
 */
@Service
@RefreshScope
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
    @Value("${ware.url}")
    private String wareUrl;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public String getTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString();
        // 放入缓存
        String tradeKey = "tradeNo:" + userId;
        this.redisTemplate.opsForValue().set(tradeKey, tradeNo);
        return tradeNo;
    }

    @Override
    public Boolean checkTradeNo(String tradeNo, String userId) {
        String tradeKey = "tradeNo:" + userId;
        String redisTradeNo = (String) this.redisTemplate.opsForValue().get(tradeKey);
        return tradeNo.equals(redisTradeNo);
    }

    @Override
    public void delTradeNo(String userId) {
        String tradeKey = "tradeNo:" + userId;
        this.redisTemplate.delete(tradeKey);
    }

    @Override
    public Boolean checkStock(Long skuId, Integer num) {
        String res = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + num);
        return "1".equals(res);
    }

    @Override
    public IPage<OrderInfo> getOrderPageList(Page<OrderInfo> orderInfoPage, String userId) {
        // orderInfo orderDetail
        IPage<OrderInfo> orderInfoIPage = orderInfoMapper.selectMyOrder(orderInfoPage, userId);
        orderInfoIPage.getRecords().forEach(orderInfo -> {
            String statusName = OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus());
            orderInfo.setOrderStatusName(statusName);
        });
        return orderInfoIPage;
    }

    @Override
    public void execExpiredOrder(Long orderId) {
        // 取消订单，本质就是修改订单状态！
        this.updateOrderStatus(orderId, ProcessStatus.CLOSED);

        this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,
                MqConst.ROUTING_PAYMENT_CLOSE,
                orderId);
    }

    @Override
    public void execExpiredOrder(Long orderId, String flag) {
        // 取消订单，本质就是修改订单状态！
        this.updateOrderStatus(orderId, ProcessStatus.CLOSED);

        if ("1".equals(flag)) {
            return;
        }
        this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_PAYMENT_CLOSE,
                MqConst.ROUTING_PAYMENT_CLOSE,
                orderId);
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo != null) {
            QueryWrapper<OrderDetail> orderDetailQueryWrapper = new QueryWrapper<>();
            orderDetailQueryWrapper.eq("order_id", orderId);
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(orderDetailQueryWrapper);
            orderInfo.setOrderDetailList(orderDetails);
        }
        return orderInfo;
    }

    /**
     * 根据订单Id修改订单状态
     *
     * @param orderId
     * @param processStatus
     */
    public void updateOrderStatus(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        orderInfo.setUpdateTime(new Date());
        this.orderInfoMapper.updateById(orderInfo);
    }

    @Override
    public void sendOrderMsg(Long orderId) {
        // 更改状态
        this.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        // 获取orderInfo 包含orderDetailList
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        Map map = this.wareJson(orderInfo);
        // 发送消息
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_WARE_STOCK,
                MqConst.ROUTING_WARE_STOCK,
                JSON.toJSONString(map));
    }

    public Map wareJson(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("ware_id", orderInfo.getWareId());
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        List<HashMap<String, Object>> detailsList = orderDetailList.stream().map(orderDetail -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("skuId", orderDetail.getSkuId());
            hashMap.put("skuNum", orderDetail.getSkuNum());
            hashMap.put("skuName", orderDetail.getSkuName());
            return hashMap;
        }).collect(Collectors.toList());
        map.put("details", detailsList);

        return map;
    }

    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        OrderInfo orderInfoOrigin = this.getOrderInfo(Long.parseLong(orderId));
        List<Map> mapList = JSON.parseArray(wareSkuMap, Map.class);
        if (!CollectionUtils.isEmpty(mapList)) {
            for (Map map : mapList) {
                String wareId = (String) map.get("wareId");
                List<String> skuIds = (List<String>) map.get("skuIds");
                OrderInfo subOrderInfo = new OrderInfo();
                BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
                subOrderInfo.setId(null);
                subOrderInfo.setParentOrderId(Long.parseLong(orderId));
                subOrderInfo.setWareId(wareId);
                List<OrderDetail> orderDetailList = subOrderInfo.getOrderDetailList().stream().filter(orderDetail -> {
                    return skuIds.contains(orderDetail.getSkuId().toString());
                }).collect(Collectors.toList());
                subOrderInfo.setOrderDetailList(orderDetailList);
                subOrderInfo.sumTotalAmount();
                this.saveOrderInfo(subOrderInfo);
                subOrderInfoList.add(subOrderInfo);
            }
        }
        this.updateOrderStatus(Long.parseLong(orderId), ProcessStatus.SPLIT);
        return subOrderInfoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {
        // total_amount user_id
        orderInfo.sumTotalAmount();
        // order_status
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        // out_trade_no 第三方交易编号 不能重复！！！
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + new Random().nextInt(10000);
        orderInfo.setOutTradeNo(outTradeNo);
        // trade_body
        orderInfo.setTradeBody("国产手机很不错！！！");
        // operate_time
        orderInfo.setOperateTime(new Date());
        // expire_time
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(instance.getTime());
        // process_status
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);

        // 返回订单Id
        Long orderId = orderInfo.getId();
        // 插入订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setOrderId(orderId);
                orderDetailMapper.insert(orderDetail);
            }
        }
        // 发送一个延迟消息
        this.rabbitService.sendDelayMsg(MqConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MqConst.ROUTING_ORDER_CANCEL,
                orderId,
                MqConst.DELAY_TIME);
        return orderId;
    }
}
