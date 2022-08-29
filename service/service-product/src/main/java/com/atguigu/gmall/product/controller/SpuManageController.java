package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
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
 * @Date 2022/08/27 16:37
 * @FileName: SpuManageController
 */
@RestController
@RequestMapping("admin/product/")
public class SpuManageController {
    @Autowired
    private SpuManageService spuManageService;

    // /admin/product/1/10?category3Id=61
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo) {
        Page<SpuInfo> spuInfoPage = new Page<>(page, limit);
        IPage<SpuInfo> spuInfoIPage = spuManageService.getSpuInfoPage(spuInfoPage, spuInfo);
        return Result.ok(spuInfoIPage);
    }

    // /admin/product/baseSaleAttrList
    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = spuManageService.getBaseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    // /admin/product/saveSpuInfo
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuManageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

}
