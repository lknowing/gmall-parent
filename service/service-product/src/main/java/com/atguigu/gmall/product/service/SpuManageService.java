package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/27 18:26
 * @FileName: SpuManageService
 */
public interface SpuManageService extends IService<SpuInfo> {
    /**
     * 根据三级分类id查询spu分页列表对象
     *
     * @param spuInfoPage
     * @param spuInfo
     * @return
     */
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    /**
     * 制作SPU的时候提前获取全部销售属性列表
     *
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存或者修改SPU
     *
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId 获取sku 需要的所有Image图片对象集合
     *
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(Long spuId);

    /**
     * 根据spuId获取销售属性以及销售属性值集合
     *
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * 根据spuId回显spuInfo信息
     *
     * @param spuId
     * @return
     */
    SpuInfo getSpuInfo(Long spuId);

    /**
     * 根据skuId和spuId获取SPU销售属性及销售属性值集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 根据spuId获取海报数据
     *
     * @param spuId
     * @return
     */
    List<SpuPoster> findSpuPosterBySpuId(Long spuId);
}
