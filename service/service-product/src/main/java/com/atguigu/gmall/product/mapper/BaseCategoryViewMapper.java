package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/31 11:05
 * @FileName: BaseCategoryViewMapper
 */
@Mapper
@Repository
public interface BaseCategoryViewMapper extends BaseMapper<BaseCategoryView> {
}
