package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/06 18:45
 * @FileName: GoodsRepository
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
