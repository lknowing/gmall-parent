package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo(skuId);
        // 更新销售状态
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo(skuId);
        // 更新销售状态
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.eq("id", skuId);
        skuInfoQueryWrapper.select("price");
        SkuInfo skuInfo = skuInfoMapper.selectOne(skuInfoQueryWrapper);
        if (skuInfo != null) {
            return skuInfo.getPrice();
        } else {
            return new BigDecimal("0");
        }
    }

    @Override
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
        // 修改数据需要判断是否为空，新增不需要
        // 插入一条数据之后，能够获取到这条数据对应的主键值！
        // sku_info
        this.skuInfoMapper.insert(skuInfo);
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
