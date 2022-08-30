package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/30 17:32
 * @FileName: ItemServiceImpl
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Override
    public Map<String, Object> getItem(Long skuId) {
        HashMap<String, Object> map = new HashMap<>();

        return map;
    }
}
