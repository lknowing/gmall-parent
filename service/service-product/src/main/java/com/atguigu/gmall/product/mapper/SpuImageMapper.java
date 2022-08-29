package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/29 19:46
 * @FileName: SpuImageMapper
 */
@Mapper
@Repository
public interface SpuImageMapper extends BaseMapper<SpuImage> {
}
