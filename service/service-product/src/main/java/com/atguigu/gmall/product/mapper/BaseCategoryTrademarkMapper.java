package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/28 13:27
 * @FileName: BaseCategoryTrademarkMapper
 */
@Mapper
@Repository
public interface BaseCategoryTrademarkMapper extends BaseMapper<BaseCategoryTrademark> {
}
