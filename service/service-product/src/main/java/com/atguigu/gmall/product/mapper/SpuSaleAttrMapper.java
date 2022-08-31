package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/29 19:47
 * @FileName: SpuSaleAttrMapper
 */
@Mapper
@Repository
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId,
                                                      @Param("spuId") Long spuId);
}
