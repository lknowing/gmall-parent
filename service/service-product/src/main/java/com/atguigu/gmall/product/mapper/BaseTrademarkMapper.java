package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/27 19:08
 * @FileName: BaseTrademarkMapper
 */
@Mapper
@Repository
public interface BaseTrademarkMapper extends BaseMapper<BaseTrademark> {
}
