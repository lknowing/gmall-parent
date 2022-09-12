package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/09 10:11
 * @FileName: CartService
 */
public interface CartService {
    /*
    数据类型根据CartInfo定义
     */
    void addToCart(Long skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId, String userTempId);

    void checkCart(Long skuId, String userId, Integer isChecked);

    void deleteCart(Long skuId, String userId);
}
