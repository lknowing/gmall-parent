package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/09 11:14
 * @FileName: CartApiController
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {
    @Autowired
    private CartService cartService;

    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable Long skuId,
                            @PathVariable Integer skuNum,
                            HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 判断
        if (StringUtils.isEmpty(userId)) {
            // 获取一个临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        // 添加购物车
        cartService.addToCart(skuId, userId, skuNum);
        return Result.ok();
    }

    @GetMapping("cartList")
    public Result getCartList(HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 获取一个临时用户Id
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartService.getCartList(userId, userTempId);
        return Result.ok(cartInfoList);
    }

    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 判断
        if (StringUtils.isEmpty(userId)) {
            // 获取一个临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.checkCart(skuId, userId, isChecked);
        return Result.ok();
    }

    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId,
                             HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 判断
        if (StringUtils.isEmpty(userId)) {
            // 获取一个临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }

}
