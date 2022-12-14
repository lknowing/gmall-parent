package com.atguigu.gmall.activity.receiver;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/20 11:09
 * @FileName: SeckillReceiver
 */
@Component
public class SeckillReceiver {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_1, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_1}
    ))
    public void importToRedis(Message message, Channel channel) {
        try {
            QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
            seckillGoodsQueryWrapper.eq("date_format(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
            seckillGoodsQueryWrapper.eq("status", "1");
            seckillGoodsQueryWrapper.gt("stock_count", 0);
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
            if (!CollectionUtils.isEmpty(seckillGoodsList)) {
                String seckillKey = RedisConst.SECKILL_GOODS;
                for (SeckillGoods seckillGoods : seckillGoodsList) {
                    // ??????????????????????????????????????????
                    Boolean exist = this.redisTemplate.opsForHash().hasKey(seckillKey, seckillGoods.getSkuId().toString());
                    if (exist) {
                        continue;
                    }
                    // ??????????????? --- ?????? hash ????????????????????????????????????????????????
                    this.redisTemplate.opsForHash().put(seckillKey, seckillGoods.getSkuId().toString(), seckillGoods);
                    // ???????????????????????? --- ?????? list ?????????????????? ?????? ??????string ???incr/decr ???????????????????????????
                    for (Integer i = 0; i < seckillGoods.getStockCount(); i++) {
                        String stockKey = RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId();
                        this.redisTemplate.opsForList().leftPush(stockKey, seckillGoods.getSkuId().toString());
                        // lpop/rpop key
                    }
                    this.redisTemplate.convertAndSend("seckillpush", seckillGoods.getSkuId() + ":1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER}
    ))
    public void seckillOrderUser(UserRecode userRecode, Message message, Channel channel) {
        /*
            1. ????????????????????????MQ?????????????????????????????????????????????
            2. ?????????????????????????????????????????????????????????
                    setnx
            3. ????????????
                    rpop key
                    ??????????????????????????????????????????????????????????????????
                            publish seckillpush skuid:0
            4. ???3???????????????????????????????????????????????????????????????????????????redis
            5. ?????? redis -- stockCount???MySQL -- stockCount
         */
        try {
            if (userRecode != null) {
                // ????????? ????????????
                seckillGoodsService.seckillOrderUser(userRecode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_SECKILL_STOCK, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_STOCK),
            key = {MqConst.ROUTING_SECKILL_STOCK}
    ))
    public void updateStock(Long skuId, Message message, Channel channel) {
        try {
            if (skuId != null) {
                this.seckillGoodsService.updateStockCount(skuId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_18, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_18}
    ))
    public void clearData(Message message, Channel channel) {
        try {
            // ????????????????????????
            QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
            seckillGoodsQueryWrapper.eq("status", 1);
            seckillGoodsQueryWrapper.le("end_time", new Date());
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                this.redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId());
            }
            // ??????????????????
            this.redisTemplate.delete(RedisConst.SECKILL_GOODS);
            // ????????????????????????????????????
            this.redisTemplate.delete(RedisConst.SECKILL_ORDERS);
            // ????????????????????????
            this.redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);
            //  ???????????????????????????????????????
            SeckillGoods seckillGoods = new SeckillGoods();
            //  1:?????????????????? ???2?????????????????????
            seckillGoods.setStatus("2");
            seckillGoodsMapper.update(seckillGoods, seckillGoodsQueryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
