package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/12 22:06
 * @FileName: CartController
 */
@Controller
public class CartController {
    @Autowired
    private ProductFeignClient productFeignClient;

    @GetMapping("addCart.html")
    public String addCart(HttpServletRequest request) {
        String skuId = request.getParameter("skuId");
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", request.getParameter("skuNum"));
        return "cart/addCart";
    }

    @GetMapping("/cart.html")
    public String cartList() {

        return "cart/index";
    }
}
