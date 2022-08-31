package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/29 22:23
 * @FileName: SkuManageService
 */
public interface SkuManageService {
    /**
     * 查询SKU分页列表
     *
     * @param skuInfoPage
     * @param skuInfo
     * @return
     */
    IPage<SkuInfo> getSkuInfoPage(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo);

    /**
     * 保存sku
     *
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 上架
     *
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 下架
     *
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 根据skuId回显数据
     *
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 根据spuId获取map集合属性
     *
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);
}
