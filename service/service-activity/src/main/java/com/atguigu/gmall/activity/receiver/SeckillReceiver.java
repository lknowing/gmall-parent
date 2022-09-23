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
                    // 如果缓存中有数据了，停止添加
                    Boolean exist = this.redisTemplate.opsForHash().hasKey(seckillKey, seckillGoods.getSkuId().toString());
                    if (exist) {
                        continue;
                    }
                    // 存储到缓存 --- 使用 hash 节省空间，修改方便，无需反序列化
                    this.redisTemplate.opsForHash().put(seckillKey, seckillGoods.getSkuId().toString(), seckillGoods);
                    // 商品秒杀数量存储 --- 使用 list 队列数据类型 或者 使用string 的incr/decr 具有原子性的操作！
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
        try {
            if (userRecode != null) {
                // 预下单 保存数据
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
            // 查出要清空的数据
            QueryWrapper<SeckillGoods> seckillGoodsQueryWrapper = new QueryWrapper<>();
            seckillGoodsQueryWrapper.eq("status", 1);
            seckillGoodsQueryWrapper.le("end_time", new Date());
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(seckillGoodsQueryWrapper);
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                this.redisTemplate.delete(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId());
            }
            // 清空秒杀商品
            this.redisTemplate.delete(RedisConst.SECKILL_GOODS);
            // 清空可能存在的预下单数据
            this.redisTemplate.delete(RedisConst.SECKILL_ORDERS);
            // 清空真正下单数据
            this.redisTemplate.delete(RedisConst.SECKILL_ORDERS_USERS);
            //  修改数据库秒杀对象的状态！
            SeckillGoods seckillGoods = new SeckillGoods();
            //  1:表示审核通过 ，2：表示秒杀结束
            seckillGoods.setStatus("2");
            seckillGoodsMapper.update(seckillGoods, seckillGoodsQueryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
