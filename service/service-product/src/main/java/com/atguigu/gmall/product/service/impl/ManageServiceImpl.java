package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/26 10:55
 * @FileName: ManageServiceImpl
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper
                .selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper
                .selectList(new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        // 调用mapper
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) //事务在异常下回滚
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断修改 新增
        if (baseAttrInfo.getId() != null) {
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);

            QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
            baseAttrValueQueryWrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(baseAttrValueQueryWrapper);
        } else {
            //新增
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            attrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }

    }

    @Override

    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        // wrapper 封装 查询 修改 删除 条件
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id", attrId);
        return baseAttrValueMapper.selectList(baseAttrValueQueryWrapper);
    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = null;
        try {
            baseAttrInfo = this.baseAttrInfoMapper.selectById(attrId);
            baseAttrInfo.setAttrValueList(this.getAttrValueList(attrId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baseAttrInfo;
    }

    @Override
    @GmallCache(prefix = "categoryView:")
    public BaseCategoryView getCategoryView(Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
        return baseCategoryView;
    }

    @Override
    @GmallCache(prefix = "attrList:")
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }

    @Override
    @GmallCache(prefix = "index:")
    public List<JSONObject> getBaseCategoryList() {
        // 创建返回对象list
        ArrayList<JSONObject> list = new ArrayList<>();
        // 通过数据库视图 获取全部一、二、三级分类Id和Name
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        // 按照一级分类Id来分组收集得到一级分类Map，key=一级分类Id、value=相同一级分类下的二级分类数据的集合
        Map<Long, List<BaseCategoryView>> baseCategory1Map =
                baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        // 对一级分类Map进行迭代处理每一个一级分类下的数据，获取iterator迭代器
        Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = baseCategory1Map.entrySet().iterator();
        int index = 1;
        while (iterator1.hasNext()) {
            // 每一次迭代放入一个JSONObject对象
            JSONObject category1 = new JSONObject();
            Map.Entry<Long, List<BaseCategoryView>> entry1 = iterator1.next();
            // key=一级分类Id
            Long category1Id = entry1.getKey();
            // value=相同一级分类下的二级分类数据的集合
            List<BaseCategoryView> categoryViewList1 = entry1.getValue();
            // 获取一级分类名
            String category1Name = categoryViewList1.get(0).getCategory1Name();
            // 封装数据
            category1.put("index", index);
            category1.put("categoryId", category1Id);
            category1.put("categoryName", category1Name);
            index++;

            Map<Long, List<BaseCategoryView>> baseCategory2Map =
                    categoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator2 = baseCategory2Map.entrySet().iterator();
            ArrayList<JSONObject> categoryChild2List = new ArrayList<>();
            while (iterator2.hasNext()) {
                JSONObject category2 = new JSONObject();
                Map.Entry<Long, List<BaseCategoryView>> entry2 = iterator2.next();
                Long category2Id = entry2.getKey();
                List<BaseCategoryView> categoryViewList2 = entry2.getValue();
                String category2Name = categoryViewList2.get(0).getCategory2Name();
                category2.put("categoryId", category2Id);
                category2.put("categoryName", category2Name);
                ArrayList<JSONObject> categoryChild3List = new ArrayList<>();
                categoryViewList2.forEach(baseCategoryView -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", baseCategoryView.getCategory3Id());
                    category3.put("categoryName", baseCategoryView.getCategory3Name());
                    categoryChild3List.add(category3);
                });
                category2.put("categoryChild", categoryChild3List);
                categoryChild2List.add(category2);
            }
            category1.put("categoryChild", categoryChild2List);
            list.add(category1);
        }
        return list;
    }


}
