package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/26 13:40
 * @FileName: BaseAttrValueMapper
 */
@Mapper
@Repository
public interface BaseAttrValueMapper extends BaseMapper<BaseAttrValue> {
}
