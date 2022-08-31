package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SpuManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/27 18:34
 * @FileName: SpuManageServiceImpl
 */
@Service
public class SpuManageServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo> implements SpuManageService {
    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id", spuInfo.getCategory3Id());
        spuInfoQueryWrapper.orderByDesc("id");
        return spuInfoMapper.selectPage(spuInfoPage, spuInfoQueryWrapper);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) //多表操作加事务
    public void saveSpuInfo(SpuInfo spuInfo) {
        if (spuInfo.getId() != null) {
            // 修改
            this.spuInfoMapper.updateById(spuInfo);

            // spu_image
            QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
            spuImageQueryWrapper.eq("spu_id", spuInfo.getId());
            spuImageMapper.delete(spuImageQueryWrapper);
            // spu_poster
            QueryWrapper<SpuPoster> spuPosterQueryWrapper = new QueryWrapper<>();
            spuPosterQueryWrapper.eq("spu_id", spuInfo.getId());
            spuPosterMapper.delete(spuPosterQueryWrapper);
            // spu_sale_attr
            QueryWrapper<SpuSaleAttr> spuSaleAttrQueryWrapper = new QueryWrapper<>();
            spuSaleAttrQueryWrapper.eq("spu_id", spuInfo.getId());
            spuSaleAttrMapper.delete(spuSaleAttrQueryWrapper);
            // spu_sale_attr_value
            QueryWrapper<SpuSaleAttrValue> spuSaleAttrValueQueryWrapper = new QueryWrapper<>();
            spuSaleAttrValueQueryWrapper.eq("spu_id", spuInfo.getId());
            spuSaleAttrValueMapper.delete(spuSaleAttrValueQueryWrapper);
        } else {
            // 新增
            //插入spu_info表
            this.spuInfoMapper.insert(spuInfo);
        }
        Long spuInfoId = spuInfo.getId();
        //插入spu_image表，需要spu_id
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuInfoId);
                spuImageMapper.insert(spuImage);
            });
        }
        //插入spu_poster表，需要spu_id
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            spuPosterList.forEach(spuPoster -> {
                spuPoster.setSpuId(spuInfoId);
                spuPosterMapper.insert(spuPoster);
            });
        }
        //插入spu_sale_attr表，需要spu_id
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfoId);
                spuSaleAttrMapper.insert(spuSaleAttr);
                //插入spu_sale_attr_value表，需要spu_id、sale_attr_name
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        spuSaleAttrValue.setSpuId(spuInfoId);
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });
        }

    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(spuImageQueryWrapper);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrList(spuId);
    }

    @Override
    public SpuInfo getSpuInfo(Long spuId) {
        SpuInfo spuInfo = spuInfoMapper.selectById(spuId);
        // spu_image spu_id
        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id", spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(spuImageQueryWrapper);
        spuInfo.setSpuImageList(spuImageList);
        // spu_poster spu_id
        QueryWrapper<SpuPoster> spuPosterQueryWrapper = new QueryWrapper<>();
        spuPosterQueryWrapper.eq("spu_id", spuId);
        List<SpuPoster> spuPosterList = spuPosterMapper.selectList(spuPosterQueryWrapper);
        spuInfo.setSpuPosterList(spuPosterList);
        // spu_sale_attr spu_sale_attr_value
        List<SpuSaleAttr> spuSaleAttrList = this.getSpuSaleAttrList(spuId);
        spuInfo.setSpuSaleAttrList(spuSaleAttrList);
        return spuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @Override
    public List<SpuPoster> findSpuPosterBySpuId(Long spuId) {
        QueryWrapper<SpuPoster> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("spu_id", spuId);
        return spuPosterMapper.selectList(spuInfoQueryWrapper);
    }

}
