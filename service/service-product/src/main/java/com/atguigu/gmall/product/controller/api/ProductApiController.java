package com.atguigu.gmall.product.controller.api;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SkuManageService;
import com.atguigu.gmall.product.service.SpuManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/08/31 09:59
 * @FileName: ProductApiController
 */
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private SkuManageService skuManageService;

    @Autowired
    private ManageService manageService;

    @Autowired
    private SpuManageService spuManageService;

    // /api/product/inner/getSkuInfo/{skuId}
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        return skuManageService.getSkuInfo(skuId);
    }

    // /api/product/inner/getCategoryView/{category3Id}
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        return manageService.getCategoryView(category3Id);
    }

    // /api/product/inner/getSkuPrice/{skuId}
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return skuManageService.getSkuPrice(skuId);
    }

    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId) {
        return spuManageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        return skuManageService.getSkuValueIdsMap(spuId);
    }

    @GetMapping("inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> getSpuPosterBySpuId(@PathVariable Long spuId){
        return spuManageService.findSpuPosterBySpuId(spuId);
    }

    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        return manageService.getAttrList(skuId);
    }

}
