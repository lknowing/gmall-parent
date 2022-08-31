package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SkuManageService;
import com.atguigu.gmall.product.service.SpuManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private SpuManageService spuManageService;

    // /admin/product/spuImageList/{spuId}
    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId) {
        List<SpuImage> spuImageList = spuManageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    // /admin/product/spuSaleAttrList/{spuId}
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuManageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    // /admin/product/saveSkuInfo
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @GetMapping("list/{page}/{limit}")
    public Result getSkuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SkuInfo skuInfo) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoIPage = skuManageService.getSkuInfoPage(skuInfoPage, skuInfo);
        return Result.ok(skuInfoIPage);
    }

    // /admin/product/onSale/{skuId}
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId) {
        skuManageService.onSale(skuId);
        return Result.ok();
    }

    // /admin/product/cancelSale/{skuId}
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId) {
        skuManageService.cancelSale(skuId);
        return Result.ok();
    }

    // /admin/product/getSkuInfo/23
    @GetMapping("getSkuInfo/{skuId}")
    public Result getSkuInfo(@PathVariable Long skuId) {
        SkuInfo skuInfo = skuManageService.getSkuInfo(skuId);
        return Result.ok(skuInfo);
    }

    // updateSkuInfo
    @PostMapping("updateSkuInfo")
    public Result updateSkuInfo(@RequestBody SkuInfo skuInfo) {
        skuManageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

}
