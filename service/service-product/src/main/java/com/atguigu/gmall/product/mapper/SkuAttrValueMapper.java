package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/30 13:17
 * @FileName: SkuAttrValueMapper
 */
@Mapper
@Repository
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {
    List<SkuAttrValue> getSkuAttrValueList(@Param("skuId") Long skuId);
}
