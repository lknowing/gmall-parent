package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/05 20:45
 * @FileName: SearchService
 */
public interface SearchService {
    // 上架
    void upperGoods(Long skuId);
    // 下架
    void lowerGoods(Long skuId);
    // 更新es热度
    void incrHotScore(Long skuId);

    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
