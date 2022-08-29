package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/26 10:12
 * @FileName: ManageController
 */
@RestController //组合注解 @ResponseBody a. 返回JSON数据 b. 能将数据直接展示到页面上
@RequestMapping("admin/product/")
//@CrossOrigin
public class ManageController {
    @Autowired
    private ManageService manageService;

    /**
     * 查询所有的一级分类信息
     *
     * @return
     */
    @GetMapping("getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> baseCategory1List = this.manageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    /**
     * 根据一级分类Id 查询二级分类数据
     *
     * @param category1Id
     * @return
     */
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id) {
        List<BaseCategory2> baseCategory2List = this.manageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    /**
     * 根据二级分类Id 查询三级分类数据
     *
     * @param category2Id
     * @return
     */
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id) {
        List<BaseCategory3> baseCategory3List = this.manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    /**
     * 根据分类Id 获取平台属性数据
     *
     * @param category2Id
     * @return
     */
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id) {
        List<BaseAttrInfo> baseAttrInfoList = this.manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * 新增平台属性
     *
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        this.manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 根据平台属性id获得属性值集合
     *
     * @return
     */
    // /admin/product/getAttrValueList/{attrId}
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId) {
        //List<BaseAttrValue> baseAttrValueList = this.manageService.getAttrValueList(attrId);
        BaseAttrInfo baseAttrInfo = this.manageService.getAttrInfo(attrId);
        return Result.ok(baseAttrInfo.getAttrValueList());
    }

}
