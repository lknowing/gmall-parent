package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/27 18:26
 * @FileName: SpuManageService
 */
public interface SpuManageService {
    /**
     * 根据三级分类id查询spu分页列表对象
     *
     * @param spuInfoPage
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);
}
