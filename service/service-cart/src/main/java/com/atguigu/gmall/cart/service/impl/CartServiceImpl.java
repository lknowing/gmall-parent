package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/09 10:16
 * @FileName: CartServiceImpl
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        String cartKey = getCartKey(userId);
        CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo != null) {
            // 有这个购物项
            if (cartInfo.getSkuNum() + skuNum > 200) {
                cartInfo.setSkuNum(200);
            } else {
                cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            }
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            if (cartInfo.getIsChecked().intValue() == 0) {
                cartInfo.setIsChecked(1);
            }
            cartInfo.setUpdateTime(new Date());
        } else {
            cartInfo = new CartInfo();
            // 获取skuInfo
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setSkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setUserId(userId);

            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            cartInfo.setCartPrice(productFeignClient.getSkuPrice(skuId));
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setCreateTime(new Date());
            cartInfo.setUpdateTime(new Date());
        }
        // 保存到缓存
        this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
    }

    private String getCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoNoLoginList = new ArrayList<>();
        // 临时用户Id不为空，userId为空
        if (!StringUtils.isEmpty(userTempId)) {
            String cartKey = getCartKey(userTempId);
            cartInfoNoLoginList = this.redisTemplate.opsForHash().values(cartKey);
            if (StringUtils.isEmpty(userId)) {
                if (!CollectionUtils.isEmpty(cartInfoNoLoginList)) {
                    // 排序 时间倒序
                    cartInfoNoLoginList.sort((o1, o2) -> {
                        //  使用时间进行比较
                        return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
                    });
                }
            }
            // 返回未登录购物车数据
            return cartInfoNoLoginList;
        }

        // 声明一个登录购物车集合
        List<CartInfo> cartInfoLoginList = new ArrayList<>();
        // userId不为空 userTempId 为空/不为空
        if (!StringUtils.isEmpty(userId)) {
            String cartKey = getCartKey(userId);
            BoundHashOperations<String, String, CartInfo> boundHashOperations = this.redisTemplate.boundHashOps(cartKey);
            // hget key field = boundHashOperations.get();
            // hvals key = List<CartInfo> values = boundHashOperations.values();
            if (!CollectionUtils.isEmpty(cartInfoNoLoginList)) {
                // 合并购物车
                cartInfoNoLoginList.forEach(cartInfoNoLogin -> {
                    // 找到相同的数据，修改登录购物车的商品数量并写回redis
                    if (boundHashOperations.hasKey(cartInfoNoLogin.getSkuId().toString())) {
                        CartInfo cartInfoLogin = boundHashOperations.get(cartInfoNoLogin.getSkuId().toString());

                        if (cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum() > 200) {
                            cartInfoLogin.setSkuNum(200);
                        } else {
                            cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum());
                        }

                        if (cartInfoNoLogin.getIsChecked().intValue() == 1) {
                            if (cartInfoLogin.getIsChecked().intValue() == 0) {
                                cartInfoLogin.setIsChecked(1);
                            }
                        }

                        cartInfoLogin.setUpdateTime(new Date());
                        // 写回缓存
                        boundHashOperations.put(cartInfoLogin.getSkuId().toString(), cartInfoLogin);
                        // this.redisTemplate.boundHashOps(cartKey).put(cartInfoLogin.getSkuId().toString(), cartInfoLogin);
                    } else {
                        // 未找到相同的数据，添加未登录购物车商品到登录购物车，未登录购物车的userId修改为登录账户
                        cartInfoNoLogin.setUserId(userId);
                        cartInfoNoLogin.setCreateTime(new Date());
                        cartInfoNoLogin.setUpdateTime(new Date());
                        this.redisTemplate.opsForHash().put(cartKey, cartInfoNoLogin.getSkuId().toString(), cartInfoNoLogin);

                    }
                });
                // 相同skuId合并数量，不同skuId添加到userId对应的缓存，之后删除userTempId对应的缓存数据
                this.redisTemplate.delete(this.getCartKey(userTempId));
            }
            // 查询所有：登录+临时购物车
            cartInfoLoginList = boundHashOperations.values();
        }
        if (!CollectionUtils.isEmpty(cartInfoLoginList)) {
            // 排序 时间倒序
            cartInfoLoginList.sort((o1, o2) -> {
                //  使用时间进行比较
                return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
            });
            return cartInfoLoginList;
        }
        return new ArrayList<>();
    }

    @Override
    public void checkCart(Long skuId, String userId, Integer isChecked) {
        String cartKey = this.getCartKey(userId);
        // 获取数据
        CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo != null) {
            // 赋值并写回缓存
            cartInfo.setIsChecked(isChecked);
            this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
        }
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        // hdel key field
        this.redisTemplate.opsForHash().delete(this.getCartKey(userId), skuId.toString());
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        String cartKey = this.getCartKey(userId);
        List<CartInfo> cartInfoList = this.redisTemplate.opsForHash().values(cartKey);
        List<CartInfo> cartInfoCheckedList = cartInfoList.stream().filter(cartInfo -> {
            return cartInfo.getIsChecked() == 1;
        }).collect(Collectors.toList());
        return cartInfoCheckedList;
    }

}
