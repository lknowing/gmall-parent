package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/30 17:32
 * @FileName: ItemServiceImpl
 */
@Service
public class ItemServiceImpl implements ItemService {

    //  远程调用service-product-client
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getItem(Long skuId) {
        // 声明返回数据 map集合
        HashMap<String, Object> map = new HashMap<>();
        //        //添加布隆过滤
        //        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //        boolean contains = bloomFilter.contains(skuId);
        //        if (!contains) {
        //            return null;
        //        }
        // 获取商品的基本信息 + 商品图片列表
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        // 获取价格
        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price", skuPrice);
        }, threadPoolExecutor);
        // 获取分类数据
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
        }, threadPoolExecutor);
        // 获取海报
        CompletableFuture<Void> spuPosterListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterList = productFeignClient.getSpuPosterBySpuId(skuInfo.getSpuId());
            map.put("spuPosterList", spuPosterList);
        }, threadPoolExecutor);
        // 获取spu销售属性 + 属性值 + 锁定 is_checked
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrList);
        }, threadPoolExecutor);
        // 获取JSON字符串
        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            String jsonString = JSON.toJSONString(skuValueIdsMap);
            map.put("valuesSkuJson", jsonString);
        }, threadPoolExecutor);
        // 获取商品规格参数--平台属性
        CompletableFuture<Void> attrListCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            if (!CollectionUtils.isEmpty(attrList)) {
                List<Object> skuAttrList = attrList.stream().map(baseAttrInfo -> {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    // 这里是将map看做了一个java对象 key作为类的property属性 value作为属性值
                    hashMap.put("attrName", baseAttrInfo.getAttrName());
                    hashMap.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return hashMap;
                }).collect(Collectors.toList());
                //  保存规格参数： 只需要平台属性名称： 平台属性值名称
                map.put("skuAttrList", skuAttrList);
            }
        }, threadPoolExecutor);
        // 异步调用热度排名
        CompletableFuture<Void> hotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);

        //  key 是谁? 应该是页面渲染时需要的key！
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                skuPriceCompletableFuture,
                categoryViewCompletableFuture,
                spuPosterListCompletableFuture,
                spuSaleAttrListCompletableFuture,
                valuesSkuJsonCompletableFuture,
                attrListCompletableFuture,
                hotScoreCompletableFuture
        ).join();

        return map;
    }
}
