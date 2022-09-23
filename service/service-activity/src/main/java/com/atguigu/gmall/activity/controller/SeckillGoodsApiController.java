package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/activity/seckill")
public class SeckillGoodsApiController {
    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 返回全部列表
     *
     * @return
     */
    @GetMapping("findAll")
    public Result findAll() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.findAll();
        return Result.ok(seckillGoodsList);
    }

    /**
     * 获取实体
     *
     * @param skuId
     * @return
     */
    @GetMapping("getSeckillGoods/{skuId}")
    public Result getSeckillGoods(@PathVariable Long skuId) {
        return Result.ok(seckillGoodsService.getSeckillGoods(skuId));
    }

    @GetMapping("auth/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuIdStr(@PathVariable Long skuId,
                                     HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        if (seckillGoods != null) {
            Date currentTime = new Date();
            if (DateUtil.dateCompare(seckillGoods.getStartTime(), currentTime)
                    && DateUtil.dateCompare(currentTime, seckillGoods.getEndTime())) {
                String skuIdStr = MD5.encrypt(userId);
                return Result.ok(skuIdStr);
            }
        }
        return Result.fail().message("获取下单码失败");
    }

    @PostMapping("auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId, HttpServletRequest request) {
        String skuIdStr = request.getParameter("skuIdStr");
        String userId = AuthContextHolder.getUserId(request);

        return this.seckillGoodsService.seckillOrder(skuId, skuIdStr, userId);
    }

    @GetMapping("auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,
                             HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        return this.seckillGoodsService.checkOrder(skuId, userId);
    }

    @GetMapping("auth/trade")
    public Result seckillTrade(HttpServletRequest request) {
        // userAddressList detailArrayList totalNum totalAmount
        String userId = AuthContextHolder.getUserId(request);
        List<UserAddress> userAddressList = this.userFeignClient.findUserAddressListByUserId(userId);
        // 从缓存中得到用户订单
        OrderRecode orderRecode = (OrderRecode) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
        if (orderRecode == null) {
            return Result.fail().message("获取预下单数据失败");
        }
        // 获取秒杀商品
        SeckillGoods seckillGoods = orderRecode.getSeckillGoods();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setOrderPrice(seckillGoods.getCostPrice());
        orderDetail.setSkuName(seckillGoods.getSkuName());
        orderDetail.setImgUrl(seckillGoods.getSkuDefaultImg());
        orderDetail.setSkuNum(orderRecode.getNum());
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        detailArrayList.add(orderDetail);

        HashMap<String, Object> map = new HashMap<>();
        map.put("userAddressList", userAddressList);
        map.put("detailArrayList", detailArrayList);
        map.put("totalNum", "1");
        map.put("totalAmount", seckillGoods.getCostPrice());
        return Result.ok(map);
    }

    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo,
                              HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = this.orderFeignClient.submitSeckillOrder(orderInfo);
        if (orderId == null) {
            return Result.fail().message("下单失败");
        }
        // 将真正的下单保存到缓存中
        this.redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS_USERS, userId, orderId.toString());
        // 删除预下单数据
        this.redisTemplate.opsForHash().delete(RedisConst.SECKILL_ORDERS, userId);
        return Result.ok(orderId);
    }

}
