package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/30 13:16
 * @FileName: SkuImageMapper
 */
@Mapper
@Repository
public interface SkuImageMapper extends BaseMapper<SkuImage> {
}
