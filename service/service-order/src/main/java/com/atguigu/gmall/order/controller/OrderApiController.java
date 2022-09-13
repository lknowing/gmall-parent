package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:57
 * @FileName: OrderApiController
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private OrderService orderService;

    @GetMapping("auth/trade")
    public Result trade(HttpServletRequest request) {
        HashMap<String, Object> hashMap = new HashMap<>();
        // 获取收货地址列表
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        hashMap.put("userAddressList", userAddressList);
        // 获取订单明细集合 赋值
        AtomicInteger totalNum = new AtomicInteger();

        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        List<OrderDetail> detailArrayList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());

            totalNum.addAndGet(cartInfo.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());
        hashMap.put("detailArrayList", detailArrayList);
        // totalNum 总件数
        hashMap.put("totalNum", totalNum);
        // totalAmount 总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        hashMap.put("totalAmount", orderInfo.getTotalAmount());
        // 存储一个tradeNo
        hashMap.put("tradeNo", this.orderService.getTradeNo(userId));

        return Result.ok(hashMap);
    }

    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 流水号校验
        String tradeNo = request.getParameter("tradeNo");
        Boolean checkTradeNo = this.orderService.checkTradeNo(tradeNo, userId);
        if (!checkTradeNo) {
            return Result.fail().message("不能重复提交订单!");
        }
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = this.orderService.saveOrderInfo(orderInfo);
        // 删除流水号
        this.orderService.delTradeNo(userId);
        return Result.ok(orderId);
    }
}
