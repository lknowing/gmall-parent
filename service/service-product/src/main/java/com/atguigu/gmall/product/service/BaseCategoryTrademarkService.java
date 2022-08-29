package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/28 13:25
 * @FileName: BaseCategoryTrademarkService
 */
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    /**
     * 根据三级分类id查询分类品牌列表
     *
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);

    /**
     * 根据分类id和品牌id删除分类品牌关系
     *
     * @param category3Id
     * @param trademarkId
     */
    void remove(Long category3Id, Long trademarkId);

    /**
     * 保存分类品牌关系
     *
     * @param categoryTrademarkVo
     */
    void save(CategoryTrademarkVo categoryTrademarkVo);

    /**
     * 根据三级分类id查询可选分类品牌列表
     *
     * @param category3Id
     * @return
     */
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);
}
