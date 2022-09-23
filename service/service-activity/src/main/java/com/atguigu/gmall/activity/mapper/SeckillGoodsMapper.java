package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/20 11:17
 * @FileName: SeckillGoodsMapper
 */
@Mapper
@Repository
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {
}
