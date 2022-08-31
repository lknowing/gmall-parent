package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/31 17:53
 * @FileName: ItemController
 */
@Controller
public class ItemController {
    @Autowired
    private ItemFeignClient itemFeignClient;

    //  http://item.gmall.com/23.html
    @GetMapping("{skuId}.html")
    public String item(@PathVariable Long skuId, Model model) {
        Result<Map> result = itemFeignClient.getItem(skuId);
        model.addAllAttributes(result.getData());
        return "item/item";
    }

}
