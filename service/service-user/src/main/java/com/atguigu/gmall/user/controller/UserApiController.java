package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:28
 * @FileName: UserApiController
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {
    @Autowired
    private UserAddressService userAddressService;

    @RequestMapping("inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable String userId) {
        return this.userAddressService.getUserAddressListByUserId(userId);
    }
}
