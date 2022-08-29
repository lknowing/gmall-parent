package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/28 13:21
 * @FileName: BaseCategoryTrademarkController
 */
@RestController
@RequestMapping("admin/product/baseCategoryTrademark")
public class BaseCategoryTrademarkController {
    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;


    // /admin/product/baseCategoryTrademark/findTrademarkList/{category3Id}
    @GetMapping("findTrademarkList/{category3Id}")
    public Result findTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> baseTrademarkList =
                baseCategoryTrademarkService.findTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    // /admin/product/baseCategoryTrademark/remove/{category3Id}/{trademarkId}
    @DeleteMapping("remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable Long category3Id,
                         @PathVariable Long trademarkId) {
        baseCategoryTrademarkService.remove(category3Id, trademarkId);
        return Result.ok();
    }

    // /admin/product/baseCategoryTrademark/findCurrentTrademarkList/{category3Id}
    @GetMapping("findCurrentTrademarkList/{category3Id}")
    public Result findCurrentTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> baseTrademarkList =
                baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    @PostMapping("save")
    public Result save(@RequestBody CategoryTrademarkVo categoryTrademarkVo) {
        baseCategoryTrademarkService.save(categoryTrademarkVo);
        return Result.ok();
    }
}
