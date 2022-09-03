package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
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

    @Override
    public Map<String, Object> getItem(Long skuId) {
        // 声明返回数据 map集合
        HashMap<String, Object> map = new HashMap<>();
        //添加布隆过滤
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        boolean contains = bloomFilter.contains(skuId);
        if (!contains) {
            return null;
        }
        // 获取商品的基本信息 + 商品图片列表
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 获取价格
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        // 获取分类数据
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        // 获取海报
        List<SpuPoster> spuPosterList = productFeignClient.getSpuPosterBySpuId(skuInfo.getSpuId());
        // 获取spu销售属性 + 属性值 + 锁定 is_checked
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        // 获取JSON字符串
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        String jsonString = JSON.toJSONString(skuValueIdsMap);
        // 获取商品规格参数--平台属性
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
        //  key 是谁? 应该是页面渲染时需要的key！
        map.put("skuInfo", skuInfo);
        map.put("price", skuPrice);
        map.put("categoryView", categoryView);
        map.put("spuPosterList", spuPosterList);
        map.put("spuSaleAttrList", spuSaleAttrList);
        map.put("valuesSkuJson", jsonString);

        return map;
    }
}
