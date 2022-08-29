package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/27 19:01
 * @FileName: BaseTrademarkController
 */
@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @GetMapping("{page}/{limit}")
    public Result getBaseTrademarkPage(@PathVariable Long page,
                                       @PathVariable Long limit) {
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.getBaseTrademarkPage(baseTrademarkPage);
        return Result.ok(baseTrademarkIPage);
    }

    // /admin/product/baseTrademark/save
    @PostMapping("save")
    public Result saveTradeMark(@RequestBody BaseTrademark baseTrademark) {
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    // /admin/product/baseTrademark/remove/{id}
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    // /admin/product/baseTrademark/get/{id}
    @GetMapping("get/{id}")
    public Result getBaseTrademark(@PathVariable Long id) {
        BaseTrademark baseTrademark = this.baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    // /admin/product/baseTrademark/update
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark) {
        this.baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }
}
