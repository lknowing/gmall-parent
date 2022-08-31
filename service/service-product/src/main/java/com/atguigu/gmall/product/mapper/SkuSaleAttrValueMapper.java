package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/30 13:17
 * @FileName: SkuSaleAttrValueMapper
 */
@Mapper
@Repository
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 根据spuId获取销售属性并选中
     *
     * @param spuId
     * @return
     */
    List<Map> selectSaleAttrValuesBySpu(@Param("spuId") Long spuId);

    List<SkuSaleAttrValue> getSkuSaleAttrValueList(@Param("skuId") Long skuId);
}
