package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/28 13:26
 * @FileName: BaseCategoryTrademarkServiceImpl
 */
@Service
public class BaseCategoryTrademarkServiceImpl
        extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {
    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarks =
                this.baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        if (!CollectionUtils.isEmpty(baseCategoryTrademarks)) {
            List<Long> tmIdList = baseCategoryTrademarks.stream()
                    .map(BaseCategoryTrademark::getTrademarkId)
                    .collect(Collectors.toList());
            return this.baseTrademarkMapper.selectBatchIds(tmIdList);
        }
        return null;
    }

    @Override
    public void remove(Long category3Id, Long trademarkId) {
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        baseCategoryTrademarkQueryWrapper.eq("trademark_id", trademarkId);
        this.baseCategoryTrademarkMapper.delete(baseCategoryTrademarkQueryWrapper);
    }

    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        if (!CollectionUtils.isEmpty(trademarkIdList)) {
            List<BaseCategoryTrademark> baseCategoryTrademarkList = trademarkIdList.stream().map(tmId -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                baseCategoryTrademark.setTrademarkId(tmId);
                return baseCategoryTrademark;
            }).collect(Collectors.toList());
            this.saveBatch(baseCategoryTrademarkList);
        }
    }

    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarks =
                this.baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);

        if (!CollectionUtils.isEmpty(baseCategoryTrademarks)) {
            List<Long> tmIdList = baseCategoryTrademarks.stream()
                    .map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());

            List<BaseTrademark> baseCurrentTrademarkList =
                    this.baseTrademarkMapper.selectList(null).stream()
                            .filter(baseTrademark -> !tmIdList.contains(baseTrademark.getId()))
                            .collect(Collectors.toList());
            return baseCurrentTrademarkList;
        }
        return baseTrademarkMapper.selectList(null);
    }
}
