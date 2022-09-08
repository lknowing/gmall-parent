package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/08 10:15
 * @FileName: UserService
 */
public interface UserService {
    // 登录
    UserInfo login(UserInfo userInfo);
}
