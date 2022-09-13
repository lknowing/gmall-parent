package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.impl.UserDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * title:
 *
 * @Author xu
 * @Date 2022/09/13 12:36
 * @FileName: UserFeignClient
 */
@FeignClient(value = "service-user", fallback = UserDegradeFeignClient.class)
@Repository
public interface UserFeignClient {
    @RequestMapping("api/user/inner/findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable String userId);


}
