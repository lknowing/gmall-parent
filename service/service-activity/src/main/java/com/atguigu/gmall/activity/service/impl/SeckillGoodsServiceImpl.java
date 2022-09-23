package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.sun.javaws.jnl.RContentDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/20 14:10
 * @FileName: SeckillGoodsServiceImpl
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Override
    public List<SeckillGoods> findAll() {
        List<SeckillGoods> seckillGoodsList = this.redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS);
        return seckillGoodsList;
    }

    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        SeckillGoods seckillGoods = (SeckillGoods) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS, skuId.toString());
        return seckillGoods;
    }

    @Override
    public Result seckillOrder(Long skuId, String skuIdStr, String userId) {
        /*
            1. 校验下单码
            2. 校验状态位state
            3. 条件都成立，发送用户和skuId到消息队列
            4. 前端轮询秒杀状态
         */
        if (!skuIdStr.equals(MD5.encrypt(userId))) {
            return Result.fail().message("下单码错误！");
        }
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state)) {
            return Result.fail().message("已售罄！");
        } else if (StringUtils.isEmpty(state)) {
            return Result.fail().message("非法的状态位！");
        } else {
            UserRecode userRecode = new UserRecode();
            userRecode.setSkuId(skuId);
            userRecode.setUserId(userId);
            this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_SECKILL_USER,
                    MqConst.ROUTING_SECKILL_USER
                    , userRecode);
            return Result.ok();
        }
    }

    @Override
    public void seckillOrderUser(UserRecode userRecode) {
        /*
            1. 首先判断状态位，MQ队列有数十万数据，随时可能售罄
            2. 判断用户是否已经下过单了，防止重复下单
                    setnx
            3. 减库存！
                    rpop key
                    出队失败：说明没有库存，需要通知其他兄弟节点
                            publish seckillpush skuid:0
            4. 前3步成功，说明用户获得下单资格，将用户和商品信息放入redis
            5. 更新 redis -- stockCount，MySQL -- stockCount
         */
        Long skuId = userRecode.getSkuId();
        String userId = userRecode.getUserId();
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state) || StringUtils.isEmpty(state)) {
            // 已售罄！
            return;
        }
        // 判断用户是否已经下过单！
        Boolean isExist = this.redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId,
                skuId.toString(), RedisConst.SECKILL__TIMEOUT, TimeUnit.SECONDS);
        if (!isExist) {
            return;
        }
        // 减库存！出队失败则修改状态位，通知redis兄弟节点 publish
        String goodsId = (String) this.redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX + skuId);
        if (StringUtils.isEmpty(goodsId)) {
            // 出队失败，售罄，不能秒杀了
            this.redisTemplate.convertAndSend("seckillpush", skuId + ":0");
            return;
        }
        // 用户获得下单资格，将用户和商品信息放入redis
        OrderRecode orderRecode = new OrderRecode();
        orderRecode.setUserId(userId);
        orderRecode.setNum(1);
        orderRecode.setOrderStr(MD5.encrypt(userId + skuId));
        orderRecode.setSeckillGoods(this.getSeckillGoods(skuId));
        this.redisTemplate.opsForHash().put(RedisConst.SECKILL_ORDERS,
                userId, orderRecode);
        // 更新 redis -- stockCount，MySQL -- stockCount
        this.rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_SECKILL_STOCK,
                MqConst.ROUTING_SECKILL_STOCK, skuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockCount(Long skuId) {
        // hget key field，hset key field value 等同于从数据库获取到数据！
        SeckillGoods seckillGoodsRedis = (SeckillGoods) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_GOODS, skuId.toString());
        // 从redis的list获取剩余库存数 修改数据
        Long count = this.redisTemplate.opsForList().size(RedisConst.SECKILL_STOCK_PREFIX + skuId);
        seckillGoodsRedis.setStockCount(count.intValue());
        seckillGoodsRedis.setUpdateTime(new Date());
        // 更新mysql数据库！
        this.seckillGoodsMapper.updateById(seckillGoodsRedis);
        // 重新放入redis！
        this.redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, skuId.toString(), seckillGoodsRedis);
    }

    @Override
    public Result checkOrder(Long skuId, String userId) {
        /*
            1. 验证缓存中是否有用户Id
            2. 判断用户是否有预下单
            3. 判断用户是否真正的下过订单
            4. 校验一下状态位
         */
        String userKey = RedisConst.SECKILL_USER + userId;
        Boolean userExist = this.redisTemplate.hasKey(userKey);
        if (userExist) {
            Boolean preOrder = this.redisTemplate.opsForHash().hasKey(RedisConst.SECKILL_ORDERS, userId);
            if (preOrder) {
                OrderRecode orderRecode = (OrderRecode) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS, userId);
                return Result.build(orderRecode, ResultCodeEnum.SECKILL_SUCCESS);
            }
        }
        Boolean orderExist = this.redisTemplate.opsForHash().hasKey(RedisConst.SECKILL_ORDERS_USERS, userId);
        if (orderExist) {
            String orderId = (String) this.redisTemplate.opsForHash().get(RedisConst.SECKILL_ORDERS_USERS, userId);
            return Result.build(orderId, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }
        String state = (String) CacheHelper.get(skuId.toString());
        if ("0".equals(state) || StringUtils.isEmpty(state)) {
            // 已售罄！
            return Result.build(null, ResultCodeEnum.SECKILL_FAIL);
        }
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

}
