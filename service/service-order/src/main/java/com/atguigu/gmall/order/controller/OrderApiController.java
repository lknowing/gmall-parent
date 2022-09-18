package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.config.ThreadPoolExecutorConfig;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
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

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

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
        // 在此需要校验库存和商品价格 使用多线程并发完成
        // 创建集合存放线程
        ArrayList<CompletableFuture> completableFutureArrayList = new ArrayList<>();
        // 创建集合存放提示信息
        ArrayList<String> errorMsgList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Long skuId = orderDetail.getSkuId();
            Integer num = orderDetail.getSkuNum();
            // 创建检验库存的线程
            CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
                Boolean exist = this.orderService.checkStock(skuId, num);
                if (!exist) {
                    errorMsgList.add(skuId + "库存不足!");
                }
            }, threadPoolExecutor);
            // 将循环的库存校验线程存放到线程集合
            completableFutureArrayList.add(stockCompletableFuture);
            // 校验商品价格
            // 创建校验商品价格的线程
            CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                // 商品的实时价格
                BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
                // 价格比较 return xs != ys ? ((xs > ys) ? 1 : -1) : 0;
                if (orderPrice.compareTo(skuPrice) != 0) {
                    String msg = orderPrice.compareTo(skuPrice) > 0 ? "降价" : "涨价";
                    // 变动的价格
                    BigDecimal price = orderPrice.subtract(skuPrice).abs();
                    // 自动更新购物车价格
                    String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                    // hget key field;
                    CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
                    cartInfo.setSkuPrice(skuPrice);
                    // hset key field value;
                    this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
                    // 返回信息提示
                    errorMsgList.add(skuId + "价格" + msg + price);
                }
            }, threadPoolExecutor);
            // 将循环的商品价格校验线程存放到线程集合
            completableFutureArrayList.add(priceCompletableFuture);
        }
        // 多任务组合：所有异步编排对象都在集合中
        CompletableFuture.allOf(
                completableFutureArrayList
                        .toArray(new CompletableFuture[completableFutureArrayList.size()])
        ).join();
        // 判断
        if (errorMsgList.size() > 0) {
            return Result.fail().message(StringUtils.join(errorMsgList, ","));
        }
        // 调用服务层方法存储订单
        Long orderId = this.orderService.saveOrderInfo(orderInfo);
        // 删除流水号
        this.orderService.delTradeNo(userId);
        return Result.ok(orderId);
    }

    @GetMapping("auth/{page}/{limit}")
    public Result getOrderPageList(@PathVariable Long page,
                                   @PathVariable Long limit,
                                   HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        IPage<OrderInfo> orderPageList = this.orderService.getOrderPageList(orderInfoPage, userId);
        return Result.ok(orderPageList);
    }

    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId) {
        return orderService.getOrderInfo(orderId);
    }
}
