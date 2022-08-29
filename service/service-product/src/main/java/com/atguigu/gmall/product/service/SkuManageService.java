package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
}
