package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/29 19:36
 * @FileName: BaseSaleAttrMapper
 */
@Mapper
@Repository
public interface BaseSaleAttrMapper extends BaseMapper<BaseSaleAttr> {
}
