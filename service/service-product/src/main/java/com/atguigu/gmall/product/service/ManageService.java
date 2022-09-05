package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/26 10:54
 * @FileName: ManageService
 */
public interface ManageService {
    /**
     * 获取一级分类
     *
     * @return
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类id获取二级分类数据
     *
     * @param category1Id
     * @return
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级id获取三级分类数据
     *
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类Id 获取平台属性数据
     *
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存平台属性
     *
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性id获得属性值集合
     *
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 根据属性Id获取属性值集合
     *
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(Long attrId);

    /**
     * 根据spuId获取分类数据
     *
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryView(Long category3Id);

    /**
     * 通过skuId 集合来查询平台属性数据
     *
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getAttrList(Long skuId);

    /**
     * 获取首页分类数据
     *
     * @return
     */
    List<JSONObject> getBaseCategoryList();
}
