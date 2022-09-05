package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/05 20:47
 * @FileName: SearchServiceImpl
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ProductFeignClient productFeignClient;

    // 商品上架 --- 将数据封装到Goods，并将Goods保存到ES中
    // ElasticsearchRepository
    @Override
    public void upperGoods(Long skuId) {

    }

    @Override
    public void lowerGoods(Long skuId) {

    }
}
