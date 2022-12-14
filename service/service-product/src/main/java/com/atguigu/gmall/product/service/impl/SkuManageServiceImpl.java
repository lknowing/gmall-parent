package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/29 22:25
 * @FileName: SkuManageServiceImpl
 */
@Service
public class SkuManageServiceImpl implements SkuManageService {
    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo(skuId);
        // ??????????????????
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
        // ???list????????????????????????
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_GOODS,
                MqConst.ROUTING_GOODS_UPPER, skuId);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo(skuId);
        // ??????????????????
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
        // ??????????????????
        rabbitService.sendMsg(MqConst.EXCHANGE_DIRECT_GOODS,
                MqConst.ROUTING_GOODS_LOWER, skuId);
    }

    @Override
    @GmallCache(prefix = "skuInfo:")
    public SkuInfo getSkuInfo(Long skuId) {
        return getSkuInfoDB(skuId);

        //return getSkuInfoByRedisson(skuId);

        //return getSkuInfoByRedis(skuId);
    }

    private SkuInfo getSkuInfoByRedisson(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            // ?????? ??????
            // ??????redis??????sku???key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // ???????????????????????????
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo == null) {
                // ?????? redisson
                // ????????????key sku: skuId :lock
                String locKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(locKey);
                //            lock.lock();
                //            ????????????
                //            lock.unlock();  ?????????????????????
                // ???????????? ???????????? ????????????
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res) { // tryLock ???????????? try...finally...?????? ???finally?????????????????? unlock() ??????
                    try {
                        // ???????????? ???????????????
                        skuInfo = this.getSkuInfoDB(skuId);
                        // ????????????????????????????????????
                        if (skuInfo == null) {
                            // ????????????????????? ?????????????????????
                            SkuInfo skuInfo1 = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        // ???????????? ????????????
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        // ??????????????????????????????
                        return skuInfo;
                    } finally {
                        lock.unlock();
                    }
                } else {
                    Thread.sleep(500);
                    return getSkuInfo(skuId);
                }
            } else {
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoByRedis(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            // ?????? ??????
            // ??????redis??????sku???key
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // ???????????????????????????
            /*
            ??????string???????????????????????????????????????hash
             */
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo == null) {
                // ?????????????????? ??????????????????????????????????????????????????????????????? ??????
                // ?????? redis
                // ????????????key sku: skuId :lock
                String locKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                // ????????????value ??????Id?????? ?????????
                String uuid = UUID.randomUUID().toString();
                // ??????
                Boolean res = redisTemplate.opsForValue().setIfAbsent(locKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res) {
                    // ???????????? ???????????????
                    skuInfo = this.getSkuInfoDB(skuId);
                    // ????????????????????????????????????
                    if (skuInfo == null) {
                        // ????????????????????? ?????????????????????
                        SkuInfo skuInfo1 = new SkuInfo();
                        redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                        // ????????? ??????lua???????????? ?????? ?????????
                        this.redisScript(locKey, uuid);
                        return skuInfo1;
                    }
                    // ???????????? ????????????
                    redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    // ????????? ??????lua???????????? ?????? ?????????
                    this.redisScript(locKey, uuid);
                    // ??????????????????????????????
                    return skuInfo;
                } else {
                    // ??????????????? ???????????? ??????
                    Thread.sleep(500);
                    return getSkuInfo(skuId);
                }
            } else {
                // ?????????????????????
                return skuInfo;
            }
        } catch (InterruptedException e) {
            // ????????????...
            // ?????????????????????????????????????????????
            e.printStackTrace();
        }
        // ?????????????????? ????????????????????? ???????????????
        return getSkuInfoDB(skuId);
    }

    private void redisScript(String locKey, String uuid) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript redisScript = new DefaultRedisScript<>();
        // ??????lua?????????????????????Long
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(script);
        // ????????????key????????????value
        redisTemplate.execute(redisScript, Arrays.asList(locKey), uuid);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo == null) {
            return null;
        }
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
        skuInfo.setSkuImageList(skuImageList);

        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.getSkuAttrValueList(skuId);
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setAttrName(baseAttrInfoMapper.selectById(skuAttrValue.getAttrId()).getAttrName());
            });
        }
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.getSkuSaleAttrValueList(skuId);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        return skuInfo;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        String locKey = "price:" + skuId;
        RLock lock = redissonClient.getLock(locKey);
        lock.lock();
        try {
            QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
            skuInfoQueryWrapper.eq("id", skuId);
            skuInfoQueryWrapper.select("price");
            SkuInfo skuInfo = skuInfoMapper.selectOne(skuInfoQueryWrapper);
            if (skuInfo != null) {
                return skuInfo.getPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return new BigDecimal("0");
    }

    @Override
    @GmallCache(prefix = "skuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        HashMap<Object, Object> hashMap = new HashMap<>();
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (!CollectionUtils.isEmpty(mapList)) {
            mapList.forEach(map -> {
                hashMap.put(map.get("value_ids"), map.get("sku_id"));
            });
        }
        return hashMap;
    }

    @Override
    public IPage<SkuInfo> getSkuInfoPage(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo) {
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.eq("category3_id", skuInfo.getCategory3Id());
        skuInfoQueryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(skuInfoPage, skuInfoQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        // ??????????????????????????????????????????????????????
        if (skuInfo.getId() != null) {
            // ??????
            this.skuInfoMapper.updateById(skuInfo);
            // ??????sku_id
            Long skuInfoId = skuInfo.getId();
            // sku_image
            QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
            skuImageQueryWrapper.eq("sku_id", skuInfoId);
            skuImageMapper.delete(skuImageQueryWrapper);
            // sku_attr_value
            QueryWrapper<SkuAttrValue> skuAttrValueQueryWrapper = new QueryWrapper<>();
            skuAttrValueQueryWrapper.eq("sku_id", skuInfoId);
            skuAttrValueMapper.delete(skuAttrValueQueryWrapper);
            // sku_sale_attr_value
            QueryWrapper<SkuSaleAttrValue> skuSaleAttrValueQueryWrapper = new QueryWrapper<>();
            skuSaleAttrValueQueryWrapper.eq("sku_id", skuInfoId);
            skuSaleAttrValueMapper.delete(skuSaleAttrValueQueryWrapper);
        } else {
            // ??????
            // ???????????????????????????????????????????????????????????????????????????
            // sku_info
            this.skuInfoMapper.insert(skuInfo);
            //??????????????????
            RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
            bloomFilter.add(skuInfo.getId());
        }
        Long skuInfoId = skuInfo.getId();
        // sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfoId);
                skuImageMapper.insert(skuImage);
            });
        }
        // sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfoId);
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }
        // sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfoId);
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
    }
}
