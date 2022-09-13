package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:31
 * @FileName: UserAddressService
 */
public interface UserAddressService {
    /**
     * 获取用户收货地址列表
     *
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressListByUserId(String userId);

}
