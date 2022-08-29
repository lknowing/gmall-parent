package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/29 22:19
 * @FileName: SkuManageController
 */
@RestController
@RequestMapping("admin/product") // /admin/product/list/{page}/{limit}
public class SkuManageController {
    @Autowired
    private SkuManageService skuManageService;

    @GetMapping("list/{page}/{limit}")
    public Result getSkuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SkuInfo skuInfo) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoIPage = skuManageService.getSkuInfoPage(skuInfoPage, skuInfo);
        return Result.ok(skuInfoIPage);
    }
}
