package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/26 13:39
 * @FileName: BaseAttrInfoMapper
 */
@Mapper
@Repository
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    List<BaseAttrInfo> selectBaseAttrInfoList(
            @Param("category1Id") Long category1Id,
            @Param("category2Id") Long category2Id,
            @Param("category3Id") Long category3Id);

    List<BaseAttrInfo> selectBaseAttrInfoListBySkuId(Long skuId);
}
